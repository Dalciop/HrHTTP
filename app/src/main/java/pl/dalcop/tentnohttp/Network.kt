package pl.dalcop.tentnohttp

import android.content.Context
import android.net.ConnectivityManager
import android.net.ConnectivityManager.NetworkCallback
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkInfo
import android.net.NetworkRequest
import android.net.wifi.WifiManager


class Network {
    companion object {
        lateinit var connectivityManager: ConnectivityManager
        lateinit var wifiManager: WifiManager
        var wifiLock: WifiManager.WifiLock? = null
        val port = 5999

        fun getDeviceIPAddress(): String {
            val wifiInfo = wifiManager.connectionInfo
            val ipAddress = wifiInfo.ipAddress

            // Convert IP address from integer to string format
            return ((ipAddress and 0xFF).toString() + "." +
                    ((ipAddress shr 8) and 0xFF) + "." +
                    ((ipAddress shr 16) and 0xFF) + "." +
                    (ipAddress shr 24 and 0xFF))
        }

        fun getActiveNetwork(): Network? {
            return connectivityManager.activeNetwork
        }

        fun getNetworkCapabilities(network: Network?, context: Context) : NetworkCapabilities? {
            val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            return connectivityManager.getNetworkCapabilities(network)
        }
        fun getNetworkState(network: Network?): NetworkInfo.State? {
            return connectivityManager.getNetworkInfo(network)?.state
        }
        fun isWifiConnected(network: Network?, capabilities: NetworkCapabilities?): Boolean {
            return if (network != null) {
                capabilities != null && getNetworkState(network) == NetworkInfo.State.CONNECTED
            } else {
                false
            }
        }

        fun triggerWifiTransport() {
            val request = NetworkRequest.Builder()
                .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
                .build()
            val networkCallback = NetworkCallback()
            connectivityManager.requestNetwork(request, networkCallback)
        }
    }
}