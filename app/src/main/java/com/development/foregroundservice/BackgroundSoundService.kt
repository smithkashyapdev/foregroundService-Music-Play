package com.development.foregroundservice
import android.app.*
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.media.MediaPlayer
import android.os.Build
import android.os.IBinder
import android.os.PowerManager
import android.os.SystemClock
import android.util.Log
import android.widget.Toast
import androidx.core.app.NotificationCompat
import kotlinx.coroutines.*


/* Add declaration of this service into the AndroidManifest.xml inside application tag*/
class BackgroundSoundService : Service() {
    var player: MediaPlayer? = null
    private var wakeLock: PowerManager.WakeLock? = null
    var job:Job?=null
    override fun onBind(arg0: Intent): IBinder? {
        Log.i(TAG, "onBind()")
        return null
    }

    override fun onCreate() {
        super.onCreate()
        Log.i(TAG, "onCreate()")
        isServiceRunning=true
        player = MediaPlayer.create(this, R.raw.song)
        player!!.isLooping = true // Set looping
        player!!.setVolume(100f, 100f)
        Toast.makeText(this, "Service started...", Toast.LENGTH_SHORT).show()
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        Log.i(TAG, "onStartCommand()")
        wakeLock =
            (getSystemService(Context.POWER_SERVICE) as PowerManager).run {
                newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "EndlessService::lock").apply {
                    acquire()
                }
            }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val NOTIFICATION_CHANNEL_ID = "com.jorgesys.musicbackground"
            val channelName = "My BackgroundSound Service"
            val chan = NotificationChannel(
                NOTIFICATION_CHANNEL_ID,
                channelName,
                NotificationManager.IMPORTANCE_NONE
            )
            chan.lightColor = Color.BLUE
            chan.lockscreenVisibility = Notification.VISIBILITY_PRIVATE
            val manager = (getSystemService(NOTIFICATION_SERVICE) as NotificationManager)
            manager.createNotificationChannel(chan)
            val builder = Notification.Builder(this, NOTIFICATION_CHANNEL_ID)
                .setContentTitle(getString(R.string.app_name))
                .setContentText("BackgroundSoundService Running")
                .setSmallIcon(R.mipmap.ic_launcher)
                .setAutoCancel(true)
            val notification = builder.build()
            startForeground(FOREGROUND_SERVICE_ID, notification)
            Log.e(TAG, "startForeground >= Build.VERSION_CODES.O")
        } else {
            val builder = NotificationCompat.Builder(this)
                .setContentTitle(getString(R.string.app_name))
                .setContentText("BackgroundSoundService Running")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setAutoCancel(true)
            val notification = builder.build()
            startForeground(FOREGROUND_SERVICE_ID, notification)
            Log.e(TAG, "startForeground < Build.VERSION_CODES.O")
        }
        if (Preferences.getMediaPosition(applicationContext) > 0) {
            Log.i(
                TAG,
                "onStartCommand(), position stored, continue from position : " + Preferences.getMediaPosition(
                    applicationContext
                )
            )
            player!!.start()
            player!!.seekTo(Preferences.getMediaPosition(applicationContext))
        } else {
            Log.i(TAG, "onStartCommand() Start!...")
            player!!.start()
        }


        job=GlobalScope.launch(Dispatchers.IO) {
            var i=0;
            while (isServiceRunning) {
                launch(Dispatchers.Main) {
                    i++
                    Toast.makeText(this@BackgroundSoundService,"$i",Toast.LENGTH_SHORT).show()
                }
                delay(3000)
            }
            Log.i(TAG,"End of the loop for the service")
        }

        //re-create the service if it is killed.
        return START_STICKY
    }

    fun onUnBind(arg0: Intent?): IBinder? {
        Log.i(TAG, "onUnBind()")
        return null
    }

    fun onStop() {
        Log.i(TAG, "onStop()")
        Preferences.setMediaPosition(applicationContext, player!!.currentPosition)
    }

    fun onPause() {
        Log.i(TAG, "onPause()")
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.i(TAG, "onDestroy() , service stopped! Media position: " + player!!.currentPosition)
        //Save current position before destruction.
        Preferences.setMediaPosition(applicationContext, player!!.currentPosition)
        player!!.pause()
        player!!.release()
        isServiceRunning=false
    }

    override fun onLowMemory() {
        Log.i(TAG, "onLowMemory()")
        Preferences.setMediaPosition(applicationContext, player!!.currentPosition)
    }

    //Inside AndroidManifest.xml add android:stopWithTask="false" to the Service definition.
    override fun onTaskRemoved(rootIntent: Intent) {
        Log.i(TAG, "onTaskRemoved(), save current position: " + player!!.currentPosition)
        //instead of stop service, save the current position.
        //stopSelf();
        Preferences.setMediaPosition(applicationContext, player!!.currentPosition)

        job?.let {
            it.cancel("serviceStop")
        }


        val restartServiceIntent = Intent(applicationContext, BackgroundSoundService::class.java).also {
            it.setPackage(packageName)
        };
        val restartServicePendingIntent: PendingIntent = PendingIntent.getService(this, 1, restartServiceIntent, PendingIntent.FLAG_ONE_SHOT);
        applicationContext.getSystemService(Context.ALARM_SERVICE);
        val alarmService: AlarmManager = applicationContext.getSystemService(Context.ALARM_SERVICE) as AlarmManager;
        alarmService.set(AlarmManager.ELAPSED_REALTIME, SystemClock.elapsedRealtime() + 1000, restartServicePendingIntent);


    }

    companion object {
        private const val FOREGROUND_SERVICE_ID = 111
        private const val TAG = "BackgroundSoundService"
        public var isServiceRunning:Boolean = false
    }
}
