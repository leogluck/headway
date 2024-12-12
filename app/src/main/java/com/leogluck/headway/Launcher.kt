package com.leogluck.headway

import android.app.Notification
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import com.leogluck.headway.audioplayer.AudioPlayerService

object Launcher {

    fun bindAudioPlayerService(context: Context, serviceConnection: ServiceConnection) {
        val intent = Intent(context, AudioPlayerService::class.java)
        context.startService(intent)
        context.bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)
    }

    fun unbindAudioPlayerService(context: Context, serviceConnection: ServiceConnection) {
        context.unbindService(serviceConnection)
    }

    fun startForegroundService(service: Service, notification: Notification) {
        service.startForeground(NOTIFICATION_ID, notification)
    }
}