package pl.dalcop.tentnohttp

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.hardware.Sensor
import android.hardware.SensorManager
import android.net.ConnectivityManager
import android.net.wifi.WifiManager
import android.os.Bundle
import android.os.Handler
import android.view.View.GONE
import android.widget.Button
import android.widget.TextView
import pl.dalcop.tentnohttp.HeartBeat.Companion.getHeartBeat
import pl.dalcop.tentnohttp.HeartBeat.Companion.heartRateSensor
import pl.dalcop.tentnohttp.HeartBeat.Companion.sensorManager
import pl.dalcop.tentnohttp.HttpServer.Companion.isRunning
import pl.dalcop.tentnohttp.HttpServer.Companion.serveHTTP
import pl.dalcop.tentnohttp.Network.Companion.connectivityManager
import pl.dalcop.tentnohttp.Network.Companion.getActiveNetwork
import pl.dalcop.tentnohttp.Network.Companion.getDeviceIPAddress
import pl.dalcop.tentnohttp.Network.Companion.getNetworkCapabilities
import pl.dalcop.tentnohttp.Network.Companion.isWifiConnected
import pl.dalcop.tentnohttp.Network.Companion.port
import pl.dalcop.tentnohttp.Network.Companion.triggerWifiTransport
import pl.dalcop.tentnohttp.Network.Companion.wifiLock
import pl.dalcop.tentnohttp.Network.Companion.wifiManager
import pl.dalcop.tentnohttp.SensorPermission.Companion.checkPermissions
import pl.dalcop.tentnohttp.Utils.Companion.navigateToHomeScreen

class MainActivity : Activity() {
    private lateinit var serverInfoTextView: TextView
    private lateinit var heartRateTextView: TextView
    private lateinit var permTextView: TextView
    private lateinit var backgroundButton: Button
    private val handler = Handler()
    var perm: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        wifiManager = applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager

        wifiLock = wifiManager.createWifiLock(WifiManager.WIFI_MODE_FULL, "MyWifiLockTag")
        wifiLock?.acquire()

        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        heartRateSensor = sensorManager.getDefaultSensor(Sensor.TYPE_HEART_RATE)

        serverInfoTextView = findViewById(R.id.server_info_text_view)
        heartRateTextView = findViewById(R.id.heart_rate_text_view)
        permTextView = findViewById(R.id.permtext)
        backgroundButton = findViewById(R.id.button)

        permTextView.setTextColor(Color.RED)
        heartRateTextView.text = HeartBeat.State.HEARTBEAT_NONE

        perm = checkPermissions(this, this)

        if (heartRateSensor == null) {
            permTextView.text = HeartBeat.State.NO_SENSORS
        }

        if (heartRateSensor != null) {
            serverInfoTextView.text = "Service stopped"
        }

        val ipAddress = getDeviceIPAddress()

        triggerWifiTransport()

        if (isWifiConnected(getActiveNetwork(), getNetworkCapabilities(getActiveNetwork(), this)) && getDeviceIPAddress() != "0.0.0.0" && !isRunning()) {
            val intent = Intent(this, HrService::class.java)
            val httpServer = serveHTTP(this, intent, serverInfoTextView, heartRateTextView)
            if (httpServer or !isRunning()) {
                serverInfoTextView.text = "Server Address: $ipAddress:$port"
                startForegroundService(intent)
            }
        }
        else {
            if (isRunning()) {
                serverInfoTextView.text = "Server Address: $ipAddress:$port"
            }
            else {
                serverInfoTextView.text = "Server Address: offline"
                permTextView.text = "Not connected to Wi-Fi"
                backgroundButton.visibility = GONE
            }
        }
        handler.postDelayed(object : Runnable {
            override fun run() {
                if (heartRateSensor != null && perm) {
                    getHeartBeat(intent, this@MainActivity, heartRateTextView)
                    triggerWifiTransport()
                }
                handler.postDelayed(this, 1000)
            }
        }, 0)

        backgroundButton.setOnClickListener {
            navigateToHomeScreen(this)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        if (requestCode == 200) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                perm = true
                permTextView.text = ""
            } else {
                perm = false
                permTextView.text = HeartBeat.State.REFUSED_PERMISSION
            }
        }
    }
}
