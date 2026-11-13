package com.narcic.doh.net

import android.content.Intent
import android.content.pm.PackageManager
import android.net.ProxyInfo
import android.net.VpnService
import android.os.IBinder
import android.os.ParcelFileDescriptor
import android.util.Log
import com.narcic.doh.Strings.app_name
import com.narcic.doh.Strings.home_text_on
import com.narcic.doh.Strings.package_name
import com.narcic.doh.Strings.stop_action
import com.narcic.doh.Strings.vpn_address
import com.narcic.doh.data.Modes
import com.narcic.doh.data.SettingsRepository
import kotlinx.coroutines.runBlocking
import java.io.IOException
import kotlin.system.exitProcess


class VpnService : VpnService() {

    private val vpnAdapter = VpnAdapter()
    private var vpnInterface: ParcelFileDescriptor? = null

    override fun onCreate() {
        super.onCreate()
        startVpn()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent != null){
            intent.action?.let {
                if (it == stop_action){
                    stopVpn()
                }
            }
        }
        return super.onStartCommand(intent, flags, startId)
    }
    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onDestroy() {
        super.onDestroy()
        stopVpn()
    }

    private fun stopVpn(){
        vpnAdapter.stop()
        try {
            vpnInterface?.close()
            vpnInterface = null
        } catch (e: IOException) {
            Log.e("Sml","parcelFileDescriptor.close()", e)
        }
        try {
            stopForeground(STOP_FOREGROUND_REMOVE)
        } catch (_: Exception) { }
        try {
            stopSelf()
        } catch (_: Exception) { }

    }
    private fun startVpn(){
        try {
            if (vpnInterface == null) {
                val cfg = runBlocking { SettingsRepository(applicationContext).loadConfig() }

                startForeground(1, ServiceNotification.build(this, home_text_on))
                vpnAdapter.start(cfg.port, cfg.lanSharing)

                val builder = Builder()
                builder.setUnderlyingNetworks(null)
                builder.setMetered(false)
                applyAppRules(builder, cfg.appMode, cfg.selectedApps)
                builder.setHttpProxy(ProxyInfo.buildDirectProxy("127.0.0.1", cfg.port))
                builder.addAddress(vpn_address, 32)
                builder.addDnsServer("8.8.8.8")

                vpnInterface = builder.setSession(app_name).establish()
            }
        } catch (e: Exception) {
            Log.e("Sml", "error", e)
            exitProcess(0)
        }
    }

    /**
     * Android does not allow mixing allowed and disallowed apps on one VPN,
     * so each mode uses exactly one of the two lists.
     * The app itself is always kept out so its own DoH requests go out directly.
     */
    private fun applyAppRules(builder: android.net.VpnService.Builder, appMode: String, apps: Set<String>) {
        val others = apps.filter { it != package_name }
        if (appMode == Modes.APPS_ONLY && others.isNotEmpty()) {
            others.forEach {
                try {
                    builder.addAllowedApplication(it)
                } catch (_: PackageManager.NameNotFoundException) { }
            }
        } else {
            builder.addDisallowedApplication(package_name)
            if (appMode == Modes.APPS_BYPASS) {
                others.forEach {
                    try {
                        builder.addDisallowedApplication(it)
                    } catch (_: PackageManager.NameNotFoundException) { }
                }
            }
        }
    }

    fun isOn(): Boolean {
        return vpnAdapter.isOn()
    }

}
