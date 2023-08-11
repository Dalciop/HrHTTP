package pl.dalcop.tentnohttp

import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class SensorPermission {
    companion object {
        fun checkPermissions(context: Context, activity: Activity): Boolean {
            if (ContextCompat.checkSelfPermission(
                    context,
                    "android.permission.BODY_SENSORS"
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    activity,
                    arrayOf("android.permission.BODY_SENSORS"),
                    200
                )
                return false
            }
            return true
        }
    }
}