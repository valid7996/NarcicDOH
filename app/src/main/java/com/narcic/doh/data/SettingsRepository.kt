package com.narcic.doh.data

import android.content.Context
import com.narcic.doh.Core

object Modes {
    const val VPN = "vpn"
    const val PROXY = "proxy"

    const val APPS_ALL = "all"        // every app uses the service
    const val APPS_BYPASS = "bypass"  // selected apps are excluded
    const val APPS_ONLY = "only"      // only selected apps use the service

    const val DEFAULT_PORT = 4525
    const val MIN_PORT = 1024
    const val MAX_PORT = 65535
}

data class ProxyConfig(
    val mode: String = Modes.VPN,
    val port: Int = Modes.DEFAULT_PORT,
    val lanSharing: Boolean = false,
    val appMode: String = Modes.APPS_ALL,
    val bypassApps: Set<String> = emptySet(),
    val onlyApps: Set<String> = emptySet()
) {
    val selectedApps: Set<String>
        get() = when (appMode) {
            Modes.APPS_BYPASS -> bypassApps
            Modes.APPS_ONLY -> onlyApps
            else -> emptySet()
        }
}

class SettingsRepository (ctx:Context) {
    private val core = Core(ctx)

    suspend fun getGreetingsState():Boolean{
        core.load("greeting_passed").let {
            return if (it != null) {
                return (it == "true")
            } else {
                false
            }
        }
    }
    suspend fun setGreetingsState(state:Boolean){
        core.save("greeting_passed",state.toString())
    }

    suspend fun loadConfig(): ProxyConfig {
        val mode = core.load("mode").takeIf { it == Modes.PROXY } ?: Modes.VPN
        val port = core.load("port")?.toIntOrNull()
            ?.takeIf { it in Modes.MIN_PORT..Modes.MAX_PORT } ?: Modes.DEFAULT_PORT
        val lan = core.load("lan_sharing") == "true"
        val appMode = core.load("app_mode").takeIf {
            it == Modes.APPS_BYPASS || it == Modes.APPS_ONLY
        } ?: Modes.APPS_ALL
        return ProxyConfig(
            mode = mode,
            port = port,
            lanSharing = lan,
            appMode = appMode,
            bypassApps = loadApps("apps_bypass"),
            onlyApps = loadApps("apps_only")
        )
    }

    suspend fun saveMode(mode: String) = core.save("mode", mode)
    suspend fun savePort(port: Int) = core.save("port", port.toString())
    suspend fun saveLanSharing(enabled: Boolean) = core.save("lan_sharing", enabled.toString())
    suspend fun saveAppMode(appMode: String) = core.save("app_mode", appMode)
    suspend fun saveBypassApps(apps: Set<String>) = core.save("apps_bypass", apps.joinToString(","))
    suspend fun saveOnlyApps(apps: Set<String>) = core.save("apps_only", apps.joinToString(","))

    private suspend fun loadApps(key: String): Set<String> {
        return core.load(key)?.split(",")?.filter { it.isNotBlank() }?.toSet() ?: emptySet()
    }
}
