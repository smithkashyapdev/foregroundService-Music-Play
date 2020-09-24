package com.development.foregroundservice

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.View
import android.view.View.OnClickListener
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.development.foregroundservice.databinding.ActivityMainBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext


class MainActivity : AppCompatActivity() {

    lateinit var mActivityMainBinding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mActivityMainBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(mActivityMainBinding.root)

        updateUI(mActivityMainBinding.btnPlay, mActivityMainBinding.textView)
        mActivityMainBinding.btnPlay.setOnClickListener(OnClickListener {
            onClick()
        })

    }

    fun onClick(){
        if (!isMyServiceRunning()) {
            Log.e("Start", "Start")
            val myService = Intent(this@MainActivity, BackgroundSoundService::class.java)
            startService(myService)
            updateUI(mActivityMainBinding.btnPlay, mActivityMainBinding.textView)
        } else {
            Log.e("stop", "stop")
            val myService = Intent(this@MainActivity, BackgroundSoundService::class.java)
            stopService(myService)
            updateUI(mActivityMainBinding.btnPlay, mActivityMainBinding.textView)
        }
    }

    private fun updateUI(btnPlay: Button, textView: TextView) {
        Handler().postDelayed({
            val isBackgroundSoundServiceRunning: Boolean = isMyServiceRunning()
            btnPlay.setText(if (isBackgroundSoundServiceRunning) "Pause" else "Start")
            textView.text = if (isBackgroundSoundServiceRunning) "Click to stop music in background!" else "Click to play music in background!"
            textView.setTextColor(Color.parseColor(if (isBackgroundSoundServiceRunning) "#FF0000" else "#0000FF"))
        },2000)
    }

    private fun isMyServiceRunning(): Boolean {
        return BackgroundSoundService.isServiceRunning
    }
}