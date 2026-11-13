package com.narcic.doh.net

import android.app.Activity
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat
import com.narcic.doh.Strings.package_name
import com.narcic.doh.Strings.start_action
import com.narcic.doh.Strings.vpn_address
import com.narcic.doh.data.Modes
import java.net.NetworkInterface

class VpnController (private val ctx: Context) {

    fun startVpn(mode: String = Modes.VPN){
        val target = if (mode == Modes.PROXY) ProxyService::class.java else VpnService::class.java
        val startServiceIntent = Intent(ctx, target)
        startServiceIntent.action = start_action
        ContextCompat.startForegroundService(ctx, startServiceIntent)
    }
    fun endVpn(){
        // stopService does nothing when a service isn't running, so stopping both is safe
        ctx.stopService(Intent(ctx, VpnService::class.java))
        ctx.stopService(Intent(ctx, ProxyService::class.java))
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
