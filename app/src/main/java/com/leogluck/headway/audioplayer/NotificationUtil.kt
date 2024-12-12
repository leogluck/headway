package com.leogluck.headway.audioplayer

import android.app.Notification
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.leogluck.headway.CHANNEL_ID
import com.leogluck.headway.R
import com.leogluck.headway.audioplayer.AudioPlayerService.Companion.ACTION_PAUSE
import com.leogluck.headway.audioplayer.AudioPlayerService.Companion.ACTION_PLAY
import com.leogluck.headway.audioplayer.AudioPlayerService.Companion.ACTION_SKIP_TO_NEXT
import com.leogluck.headway.audioplayer.AudioPlayerService.Companion.ACTION_SKIP_TO_PREVIOUS
import com.leogluck.headway.audioplayer.AudioPlayerService.Companion.ACTION_STOP_SERVICE
import com.leogluck.headway.bookplayer.BookPlayerActivity

fun Context.createNotification(isPlaying: Boolean): Notification {
    val playPauseAction = if (isPlaying) {
        getPauseAction(this)
    } else {
        getPlayAction(this)
    }

    return NotificationCompat.Builder(this, CHANNEL_ID).setSmallIcon(R.drawable.ic_music_note)
        .setContentTitle(getString(R.string.notification_description))
        .setContentText(getString(R.string.notification_description))
        .setContentIntent(getPendingIntent(this)).addAction(getSkipPreviousAction(this))
        .addAction(playPauseAction).addAction(getSkipNextAction(this))
        .setStyle(androidx.media.app.NotificationCompat.MediaStyle())
        .setDeleteIntent(getDeleteIntent(this)).setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
        .build()
}

fun getPendingIntent(context: Context): PendingIntent? {
    val activityIntent = Intent(context, BookPlayerActivity::class.java).apply {
        flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
    }
    return PendingIntent.getActivity(context, 0, activityIntent, PendingIntent.FLAG_IMMUTABLE)
}

fun getDeleteIntent(context: Context): PendingIntent {
    val deleteIntent = Intent(context, AudioPlayerService::class.java).apply {
        action = ACTION_STOP_SERVICE
    }

    return PendingIntent.getService(context, 0, deleteIntent, PendingIntent.FLAG_IMMUTABLE)
}

private fun getSkipNextAction(context: Context) = NotificationCompat.Action.Builder(
    R.drawable.ic_skip_next, context.getString(R.string.skip_next), getPendingIntent(
        context, ACTION_SKIP_TO_NEXT
    )
).build()

private fun getSkipPreviousAction(context: Context) = NotificationCompat.Action.Builder(
    R.drawable.ic_skip_previous, context.getString(R.string.skip_previous), getPendingIntent(
        context, ACTION_SKIP_TO_PREVIOUS
    )
).build()

private fun getPlayAction(context: Context) = NotificationCompat.Action.Builder(
    R.drawable.ic_play, context.getString(R.string.play), getPendingIntent(context, ACTION_PLAY)
).build()

private fun getPauseAction(context: Context) = NotificationCompat.Action.Builder(
    R.drawable.ic_pause, context.getString(R.string.pause), getPendingIntent(context, ACTION_PAUSE)
).build()

private fun getPendingIntent(context: Context, action: String): PendingIntent {
    val intent = Intent(context, AudioPlayerService::class.java).apply {
        this.action = action
    }
    return PendingIntent.getService(context, 0, intent, PendingIntent.FLAG_MUTABLE)
}

