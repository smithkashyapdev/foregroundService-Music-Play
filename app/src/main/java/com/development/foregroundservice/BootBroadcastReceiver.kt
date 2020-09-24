package com.development.foregroundservice

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import android.widget.Toast


class BootBroadcastReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action.toString()
        Log.i(TAG, "onReceive($action)")

        if(BackgroundSoundService.isServiceRunning){
            return
        }
        // u can start your service here
        Toast.makeText(context, "boot completed action has got", Toast.LENGTH_LONG).show()
        val myService = Intent(context, BackgroundSoundService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(myService)
        } else {
            context.startService(myService)
        }
    }

    companion object {
        private const val TAG = "BootBroadcastReceiver"
    }
}
