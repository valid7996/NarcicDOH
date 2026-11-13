package com.narcic.doh.ui

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.narcic.doh.data.Modes
import com.narcic.doh.data.SettingsRepository
import com.narcic.doh.net.LanAddress
import com.narcic.doh.net.VpnController
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class InstalledApp(val label: String, val packageName: String)

class MainViewModel : ViewModel() {
    var pageState :Int? by mutableStateOf(null) //0=greeting,1=home,2=settings,3=app picker
    private var checkPageStateJob: Job? = null
    private var greetingsDoneJob: Job? = null
    private var vpnController : VpnController? = null

    var vpnState :Boolean by mutableStateOf(false)
            private set

    // ---- settings state ----
    var mode: String by mutableStateOf(Modes.VPN)
        private set
    var portText: String by mutableStateOf(Modes.DEFAULT_PORT.toString())
        private set
    var portValid: Boolean by mutableStateOf(true)
        private set
    var lanSharing: Boolean by mutableStateOf(false)
        private set
    var appMode: String by mutableStateOf(Modes.APPS_ALL)
        private set
    var bypassApps: Set<String> by mutableStateOf(emptySet())
        private set
    var onlyApps: Set<String> by mutableStateOf(emptySet())
        private set
    var installedApps: List<InstalledApp> by mutableStateOf(emptyList())
        private set
    var lanAddress: String? by mutableStateOf(null)
        private set
    var message: String? by mutableStateOf(null)

    val port: Int
        get() = portText.toIntOrNull() ?: Modes.DEFAULT_PORT

    fun loadSettings(ctx: Context) {
        viewModelScope.launch {
            val cfg = SettingsRepository(ctx.applicationContext).loadConfig()
            mode = cfg.mode
            portText = cfg.port.toString()
            portValid = true
            lanSharing = cfg.lanSharing
            appMode = cfg.appMode
            bypassApps = cfg.bypassApps
            onlyApps = cfg.onlyApps
            refreshLanAddress()
        }
    }

    fun refreshLanAddress() {
        lanAddress = LanAddress.find()
    }

    fun setMode(ctx: Context, value: String) {
        mode = value
        viewModelScope.launch { SettingsRepository(ctx.applicationContext).saveMode(value) }
    }

    fun setPortText(ctx: Context, text: String) {
        val digits = text.filter { it.isDigit() }.take(5)
        portText = digits
        val p = digits.toIntOrNull()
        portValid = p != null && p in Modes.MIN_PORT..Modes.MAX_PORT
        if (portValid && p != null) {
            viewModelScope.launch { SettingsRepository(ctx.applicationContext).savePort(p) }
        }
    }

    fun setLanSharing(ctx: Context, enabled: Boolean) {
        lanSharing = enabled
        refreshLanAddress()
        viewModelScope.launch { SettingsRepository(ctx.applicationContext).saveLanSharing(enabled) }
    }

    fun setAppMode(ctx: Context, value: String) {
        appMode = value
        viewModelScope.launch { SettingsRepository(ctx.applicationContext).saveAppMode(value) }
    }

    fun selectedForCurrentMode(): Set<String> =
        if (appMode == Modes.APPS_ONLY) onlyApps else bypassApps

    fun toggleApp(ctx: Context, packageName: String) {
        val repo = SettingsRepository(ctx.applicationContext)
        if (appMode == Modes.APPS_ONLY) {
            onlyApps = if (packageName in onlyApps) onlyApps - packageName else onlyApps + packageName
            val snapshot = onlyApps
            viewModelScope.launch { repo.saveOnlyApps(snapshot) }
        } else {
            bypassApps = if (packageName in bypassApps) bypassApps - packageName else bypassApps + packageName
            val snapshot = bypassApps
            viewModelScope.launch { repo.saveBypassApps(snapshot) }
        }
    }

    fun loadInstalledApps(ctx: Context) {
        if (installedApps.isNotEmpty()) return
        viewModelScope.launch {
            installedApps = withContext(Dispatchers.Default) {
                val pm = ctx.packageManager
                val launcher = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)
                pm.queryIntentActivities(launcher, 0)
                    .map { InstalledApp(it.loadLabel(pm).toString(), it.activityInfo.packageName) }
                    .filter { it.packageName != ctx.packageName }
                    .distinctBy { it.packageName }
                    .sortedBy { it.label.lowercase() }
            }
        }
    }

    // ---- service control ----
    fun setVpnState(ctx:Context){
        if (vpnController == null) {
            vpnController = VpnController(ctx)
        }
        if (vpnState) {
            vpnController?.endVpn()
            vpnState = false
            return
        }
        if (!portValid) {
            message = "Enter a valid port (${Modes.MIN_PORT}-${Modes.MAX_PORT}) in Settings first."
            return
        }
        if (mode == Modes.VPN && appMode == Modes.APPS_ONLY && onlyApps.isEmpty()) {
            message = "Per-app mode is on but no apps are selected. Choose apps in Settings."
            return
        }
        if (mode == Modes.PROXY) {
            // proxy mode needs no VPN permission
            vpnController?.startVpn(Modes.PROXY)
            vpnState = true
            refreshLanAddress()
            return
        }
        val prepared = android.net.VpnService.prepare(ctx)
        if (prepared == null) {
            vpnController?.startVpn(Modes.VPN)
            vpnState = true
            refreshLanAddress()
        }else{
            (ctx as Activity).startActivityForResult(prepared,0x0F)
        }
    }
    fun checkVpnState(ctx:Context){
        viewModelScope.launch {
            if(vpnController == null){
                vpnController = VpnController(ctx)
            }
            vpnController?.let {
                vpnState = it.isOn()
            }
        }
    }

    fun checkPageState(ctx:Context){
        checkPageStateJob?.cancel()
        checkPageStateJob = viewModelScope.launch{
            pageState = if (SettingsRepository(ctx).getGreetingsState()){
                1
            }else{
                0
            }

        }
    }
    fun greetingsDone(ctx:Context){
        greetingsDoneJob?.cancel()
        greetingsDoneJob = viewModelScope.launch{
            SettingsRepository(ctx).setGreetingsState(true)
            checkPageState(ctx)
        }
    }
}
