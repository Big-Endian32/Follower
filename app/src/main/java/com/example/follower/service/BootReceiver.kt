package com.example.follower.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.datastore.preferences.core.booleanPreferencesKey
import com.example.follower.FollowerApplication
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

/**
 * Receiver to auto-start scanning service on device boot.
 */
class BootReceiver : BroadcastReceiver() {

    companion object {
        private const val TAG = "BootReceiver"
    }

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED) return

        Log.d(TAG, "Boot completed, checking auto-start setting")

        // Check if auto-start is enabled
        val shouldAutoStart = runBlocking {
            try {
                val app = context.applicationContext as FollowerApplication
                val prefs = app.dataStore.data.first()
                prefs[booleanPreferencesKey("auto_start_on_boot")] ?: false
            } catch (e: Exception) {
                Log.e(TAG, "Error reading preferences", e)
                false
            }
        }

        if (shouldAutoStart) {
            Log.i(TAG, "Auto-starting scanning service")

            val serviceIntent = Intent(context, ScanningService::class.java)

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(serviceIntent)
            } else {
                context.startService(serviceIntent)
            }
        }
    }
}
