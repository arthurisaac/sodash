package com.arthurisaac.application.services

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.util.Log
import com.arthurisaac.application.R

class MyForegroundService : Service() {
    override fun onBind(intent: Intent?): IBinder? {
        return null;
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Thread( Runnable {
            this.run {
                while (true) {
                    // Log.i("AI", "Foreground Service is running")
                    try {
                        Thread.sleep(2000)
                    } catch (e: InterruptedException) {
                        e.printStackTrace()
                    }
                }
            }
        }).start()

        //TODO
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val CHANNEL_ID = "foreground service id"
            val channel = NotificationChannel(CHANNEL_ID, CHANNEL_ID, NotificationManager.IMPORTANCE_LOW)

            getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
            val notification = Notification.Builder(this, CHANNEL_ID)
                .setContentText("Service is running")
                .setContentTitle("Service is enabled")
                .setSmallIcon(R.drawable.ic_launcher_background)

            startForeground(1001, notification.build())
        }
        return super.onStartCommand(intent, flags, startId)
    }


}