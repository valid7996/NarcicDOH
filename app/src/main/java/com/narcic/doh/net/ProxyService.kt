package com.narcic.doh.net

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log
import com.narcic.doh.Strings.home_text_on
import com.narcic.doh.Strings.stop_action
import com.narcic.doh.data.SettingsRepository
import kotlinx.coroutines.runBlocking

/**
 * Proxy mode: runs only the local HTTP proxy (optionally shared on the LAN),
 * without creating a VPN interface. Apps / other devices point their proxy
 * settings at <ip>:<port> manually.
 */
class ProxyService : Service() {

    private val adapter = VpnAdapter()
    private var running = false

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == stop_action) {
            stopProxy()
            return START_NOT_STICKY
        }
        if (!running) {
            running = true
            startForeground(1, ServiceNotification.build(this, home_text_on))
            try {
                val cfg = runBlocking { SettingsRepository(applicationContext).loadConfig() }
                adapter.start(cfg.port, cfg.lanSharing)
            } catch (e: Exception) {
                Log.e("Sml", "proxy start", e)
                stopProxy()
            }
        }
        return START_NOT_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        adapter.stop()
        running = false
    }

    private fun stopProxy() {
        adapter.stop()
        running = false
        try {
            stopForeground(STOP_FOREGROUND_REMOVE)
        } catch (_: Exception) { }
        stopSelf()
    }
}
