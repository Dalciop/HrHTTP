package pl.dalcop.tentnohttp

import android.content.Context
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.widget.TextView
import androidx.core.content.ContextCompat.startForegroundService
import pl.dalcop.tentnohttp.HrService.Companion.isForegroundServiceRunning

class HeartBeat {
    object State {
        const val HEARTBEAT_NONE = "Heart rate: -"
        const val NO_SENSORS = "No sensors detected"
        const val REFUSED_PERMISSION = "Refused permission"
    }
    companion object {
        var hr: Int = 0
        lateinit var sensorManager: SensorManager
        var heartRateSensor: Sensor? = null

        fun getHeartBeat(intent: Intent, context: Context, heartRateTextView: TextView): Int {
            val sensorEventListener = object : SensorEventListener {
                override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
                }

                override fun onSensorChanged(event: SensorEvent) {
                    if (event.sensor.type == Sensor.TYPE_HEART_RATE) {
                        hr = event.values[0].toInt()
                        heartRateTextView.text = "Heart rate: $hr"
                        if (!isForegroundServiceRunning(context, HrService::class.java)) {
                            startForegroundService(context, intent)
                        }
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
    }
}