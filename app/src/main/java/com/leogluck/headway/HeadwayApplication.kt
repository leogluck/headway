package com.leogluck.headway

import android.app.Application
import android.app.NotificationManager
import com.leogluck.headway.notification.createNotificationChannel
import com.leogluck.headway.notification.shouldRegisterNotificationChannel
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class HeadwayApplication : Application() {

    @Inject
    lateinit var notificationManager: NotificationManager

    override fun onCreate() {
        super.onCreate()

        if (shouldRegisterNotificationChannel(notificationManager)) {
            createNotificationChannel(this, notificationManager)
        }
    }
}
