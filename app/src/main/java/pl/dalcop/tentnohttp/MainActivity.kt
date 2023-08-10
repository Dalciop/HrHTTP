package pl.dalcop.tentnohttp

import android.app.Activity
import android.os.Bundle
import android.os.Handler
import android.widget.TextView
import android.util.Log
import fi.iki.elonen.NanoHTTPD
import android.content.Context
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.net.wifi.WifiManager
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

object state {
    val HEARTRATE_NONE = "Heart rate: -"
    val NO_SENSORS = "No sensors detected"
    val REFUSED_PERMISSION = "Refused permission"
}

class MainActivity : Activity() {
    var hr: Int = 0
    private lateinit var httpServer: NanoHTTPD

    private lateinit var sensorManager: SensorManager
    private var heartRateSensor: Sensor? = null

    private lateinit var serverInfoTextView: TextView
    private lateinit var heartRateTextView: TextView
    private lateinit var permTextView: TextView
    private val handler = Handler()
    var perm: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val port = 5999

        serverInfoTextView = findViewById(R.id.server_info_text_view)
        heartRateTextView = findViewById(R.id.heart_rate_text_view)
        permTextView = findViewById(R.id.permtext)

        heartRateTextView.text = state.HEARTRATE_NONE

        perm = checkPermissions()

        httpServer = object : NanoHTTPD("0.0.0.0", port) {
            override fun serve(session: NanoHTTPD.IHTTPSession): NanoHTTPD.Response {
                if ("/hr" == session.uri) {
                    val heartRateValue = getHeartRateValue()
                    return newFixedLengthResponse("$heartRateValue")
                }
                return newFixedLengthResponse("Invalid endpoint")
            }
        }

        try {
            httpServer.start()
        } catch (e: Exception) {
            serverInfoTextView.text = "Error starting HTTP server"
            Log.e(TAG, "Error starting HTTP server", e)
        }

        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        heartRateSensor = sensorManager.getDefaultSensor(Sensor.TYPE_HEART_RATE)

        if (heartRateSensor == null) {
            permTextView.text = state.NO_SENSORS
        }

        val ipAddress = getDeviceIPAddress()
        serverInfoTextView.text = "Server Address: $ipAddress:$port"

        handler.postDelayed(object : Runnable {
            override fun run() {
                if (heartRateSensor != null && perm) {
                    getHeartRateValue()
                }
                handler.postDelayed(this, 1000)
            }
        }, 0)
    }

    override fun onDestroy() {
        super.onDestroy()
        httpServer.stop()
    }

    private fun getDeviceIPAddress(): String {
        val wifiManager = applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
        val wifiInfo = wifiManager.connectionInfo
        val ipAddress = wifiInfo.ipAddress

        // Convert IP address from integer to string format
        return ((ipAddress and 0xFF).toString() + "." +
                ((ipAddress shr 8) and 0xFF) + "." +
                ((ipAddress shr 16) and 0xFF) + "." +
                (ipAddress shr 24 and 0xFF))
    }

    private fun getHeartRateValue(): Int {
        val sensorEventListener = object : SensorEventListener {
            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
            }

            override fun onSensorChanged(event: SensorEvent) {
                if (event.sensor.type == Sensor.TYPE_HEART_RATE && perm) {
                    hr = event.values[0].toInt()
                    heartRateTextView.text = "Heart rate: $hr"
                    sensorManager.unregisterListener(this)
                }
            }
        }
        sensorManager.registerListener(
            sensorEventListener,
            heartRateSensor,
            SensorManager.SENSOR_DELAY_NORMAL
        )
        return hr
    }

    companion object {
        private const val TAG = "HeartRateServerService"
    }

    private fun checkPermissions(): Boolean {
        if (ContextCompat.checkSelfPermission(
                this,
                "android.permission.BODY_SENSORS"
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf("android.permission.BODY_SENSORS"),
                200
                )
            return false
        }
        return true
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
                permTextView.text = state.REFUSED_PERMISSION
            }
        }
    }
}
