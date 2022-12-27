package com.arthurisaac.application.services

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log

class MyBackgroundService : Service() {
    override fun onBind(intent: Intent?): IBinder? {
        return null;
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Thread( Runnable {
            this.run {
                while (true) {
                    Log.d("AI", "Service is running")
                    try {
                        Thread.sleep(2000)
                    } catch (e: InterruptedException) {
                        e.printStackTrace()
                    }
                }
            }
        }).start()
        return super.onStartCommand(intent, flags, startId)
    }
}