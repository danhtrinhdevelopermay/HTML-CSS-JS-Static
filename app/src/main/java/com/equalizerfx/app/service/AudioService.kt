package com.equalizerfx.app.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.equalizerfx.app.R

class AudioService : Service() {
    private val binder = AudioServiceBinder()
    
    companion object {
        private const val CHANNEL_ID = "AudioServiceChannel"
        private const val NOTIFICATION_ID = 1
    }
    
    inner class AudioServiceBinder : Binder() {
        fun getService(): AudioService = this@AudioService
    }
    
    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        startForeground(NOTIFICATION_ID, createNotification())
    }
    
    override fun onBind(intent: Intent?): IBinder {
        return binder
    }
    
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Audio Service",
                NotificationManager.IMPORTANCE_LOW
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }
    
    private fun createNotification(): Notification {
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Equalizer FX")
            .setContentText("Audio effects are active")
            .setSmallIcon(R.mipmap.ic_launcher)
            .build()
    }
}
