package com.leogluck.headway.bookplayer

import android.Manifest
import android.content.ComponentName
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.leogluck.headway.Launcher
import com.leogluck.headway.audioplayer.AudioPlayerService
import com.leogluck.headway.bookplayer.Event.SetData
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class BookPlayerActivity : ComponentActivity() {

    private val viewModel: BookPlayerViewModel by viewModels()
    private var audioPlayerService: AudioPlayerService? = null
    private var isBound = false

    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            handleServiceConnected(service)
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            handleServiceDisconnected()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        handleUserPermissions()
        observeEffects()

        viewModel.onEvent(SetData(intent.getStringExtra(EXTRA_BOOK_ID) ?: DEFAULT_BOOK_ID))

        enableEdgeToEdge()
        setContent {
            BookPlayerScreen(viewModel)
        }
    }

    override fun onStart() {
        super.onStart()
        Launcher.bindAudioPlayerService(this, serviceConnection)
    }

    override fun onStop() {
        if (isBound) {
            Launcher.unbindAudioPlayerService(this, serviceConnection)
            isBound = false
        }
        super.onStop()
    }

    private fun handleEffect(effect: Effect) {
        when (effect) {
            is Effect.Prepare -> audioPlayerService?.prepare(effect.uri)
            Effect.Play -> audioPlayerService?.play()
            Effect.Pause -> audioPlayerService?.pause()
            Effect.SeekForward -> audioPlayerService?.seekForward()
            Effect.SeekBackward -> audioPlayerService?.seekBackward()
            Effect.SkipNext -> audioPlayerService?.skipToNext()
            Effect.SkipPrevious -> audioPlayerService?.skipToPrevious()
            is Effect.Seek -> audioPlayerService?.seekToPosition(effect.position)
        }
    }

    private fun handleServiceConnected(service: IBinder?) {
        val binder = service as AudioPlayerService.AudioBinder
        audioPlayerService = binder.getService().also { subscribeAudioCallbacks(it) }
        isBound = true
        viewModel.onEvent(Event.BindAudioPlayer)
    }

    private fun subscribeAudioCallbacks(audioPlayerService: AudioPlayerService) {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                audioPlayerService.playbackState.collect { state ->
                    viewModel.onEvent(Event.PlaybackStateChanged(state))
                }
            }
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                audioPlayerService.errors.collect { error ->
                    viewModel.onEvent(Event.PlaybackError(error))
                }
            }
        }
    }

    private fun handleServiceDisconnected() {
        isBound = false
    }

    private fun observeEffects() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.CREATED) {
                viewModel.effects.collect {
                    handleEffect(it)
                }
            }
        }
    }

    private fun handleUserPermissions() {
        if (!allPermissionsGranted()) {
            ActivityCompat.requestPermissions(
                this, requiredPermissions, REQUEST_CODE_PERMISSIONS
            )
        }
    }

    private fun allPermissionsGranted() = requiredPermissions.all {
        ContextCompat.checkSelfPermission(baseContext, it) == PackageManager.PERMISSION_GRANTED
    }

    private val requiredPermissions = buildList {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            add(Manifest.permission.FOREGROUND_SERVICE)
            add(Manifest.permission.POST_NOTIFICATIONS)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            add(Manifest.permission.FOREGROUND_SERVICE_MEDIA_PLAYBACK)
        }
    }.toTypedArray()

    companion object {
        private const val REQUEST_CODE_PERMISSIONS = 123
        private const val EXTRA_BOOK_ID = "EXTRA_BOOK_ID"
        private const val DEFAULT_BOOK_ID = "some_book_id"
    }
}