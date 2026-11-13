package com.narcic.doh.net

import com.narcic.doh.Strings.vpn_address
import java.net.Inet4Address
import java.net.NetworkInterface

object LanAddress {
    /** Best guess of this device's LAN / hotspot IPv4 address, or null when not on a local network. */
    fun find(): String? {
        return try {
            val interfaces = NetworkInterface.getNetworkInterfaces() ?: return null
            for (nif in interfaces) {
                if (!nif.isUp || nif.isLoopback) continue
                if (nif.name.startsWith("tun") || nif.name.startsWith("dummy")) continue
                for (addr in nif.inetAddresses) {
                    if (addr is Inet4Address && addr.isSiteLocalAddress && addr.hostAddress != vpn_address) {
                        return addr.hostAddress
                    }
                }
            }
            null
        } catch (e: Exception) {
            null
        }
    }
}
