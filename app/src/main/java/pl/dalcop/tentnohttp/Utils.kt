package pl.dalcop.tentnohttp

import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat.startActivity

class Utils {
    companion object {
        fun navigateToHomeScreen(context: Context) {
            val intent = Intent(Intent.ACTION_MAIN)
            intent.addCategory(Intent.CATEGORY_HOME)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(context, intent, null)
        }
    }
}