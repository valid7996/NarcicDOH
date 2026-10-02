package com.narcic.doh.net

import android.app.Activity
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat
import com.narcic.doh.Strings.package_name
import com.narcic.doh.Strings.start_action
import com.narcic.doh.Strings.stop_action
import com.narcic.doh.Strings.vpn_address
import java.net.NetworkInterface
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.cancel

class VpnController (private val ctx: Context) {
    private var controllerScope: CoroutineScope? = null

    fun startVpn(){
        controllerScope?.cancel("stop")
        val startServiceIntent = Intent(ctx, VpnService::class.java)
        startServiceIntent.action = start_action
        ContextCompat.startForegroundService(ctx, startServiceIntent)
    }
    fun endVpn(){
        controllerScope?.cancel("stop")
        val stopServiceIntent = Intent(ctx, VpnService::class.java)
        stopServiceIntent.action = stop_action
        ContextCompat.startForegroundService(ctx, stopServiceIntent)
    }
    fun isOn(): Boolean {
        return isVpnInterfaceUp() || isNotificationShowing()
    }

    // The service adds vpn_address (10.0.0.2) to its TUN interface, so that
    // address being present in the system means the tunnel is established.
    private fun isVpnInterfaceUp(): Boolean {
        return try {
            val interfaces = NetworkInterface.getNetworkInterfaces() ?: return false
            while (interfaces.hasMoreElements()) {
                val addresses = interfaces.nextElement().inetAddresses
                while (addresses.hasMoreElements()) {
                    if (addresses.nextElement().hostAddress == vpn_address) {
                        return true
                    }
                }
            }
            false
        } catch (e: Exception) {
            false
        }
    }

    private fun isNotificationShowing(): Boolean {
        var isFound = false
        val notificationManager = (ctx as Activity).getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val notifications = notificationManager.activeNotifications
        notifications?.let {
            for (i in notifications){
                if (package_name == i.packageName){
                    isFound = true
                }
            }
        }
        return isFound
    }
}
