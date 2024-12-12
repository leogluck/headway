package com.leogluck.headway.bookplayer

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Forward10
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Replay5
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.leogluck.headway.R
import com.leogluck.headway.formatSecondsToMMSS
import com.leogluck.headway.getBitmap
import com.leogluck.headway.getString

@Composable
fun BookPlayerScreen(viewModel: BookPlayerViewModel) {

    val screenState by viewModel.uiState.collectAsState()

    Content(screenState) { event: Event -> viewModel.onEvent(event) }
}

@Composable
private fun Content(screenState: ScreenState, onEvent: (Event) -> Unit) {
    Scaffold(
//        snackbarHost = { SnackbarHost() }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            val imageBitmap = getBitmap(screenState.bitmapResourceId)

            Image(
                bitmap = imageBitmap.asImageBitmap(),
                contentDescription = "Book Cover",
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1F)
                    .padding(top = 64.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = String.format(
                    getString(R.string.key_point_of_total),
                    screenState.currentTrackNumber,
                    screenState.totalTracks
                ), style = MaterialTheme.typography.bodyMedium, color = Color.Gray
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = getString(R.string.design_quote),
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(24.dp))

            Row(
                modifier = Modifier
                    .padding(horizontal = 12.dp)
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = formatSecondsToMMSS(screenState.currentPosition),
                    style = MaterialTheme.typography.bodyMedium
                )

                Slider(
                    value = screenState.currentPosition,
                    valueRange = 0F..screenState.totalDuration,
                    onValueChange = { onEvent(Event.Seek(it)) },
                    modifier = Modifier
                        .padding(8.dp)
                        .weight(1f),
                    colors = SliderDefaults.colors(
                        thumbColor = Color.Blue, activeTrackColor = Color.Blue
                    )
                )

                Text(
                    text = formatSecondsToMMSS(screenState.totalDuration),
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = { },
                colors = ButtonDefaults.buttonColors(Color(0xFFE9E9E9)),
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = String.format(
                        getString(R.string.playback_speed), screenState.playbackSpeed
                    ), style = MaterialTheme.typography.bodyMedium, color = Color.Black
                )
            }

            ControlPanel(screenState, onEvent)
        }
    }
}

@Composable
private fun ControlPanel(
    screenState: ScreenState, onEvent: (Event) -> Unit
) {
    Row(
        modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = { onEvent(Event.SkipPreviousClicked) }) {
            Icon(
                modifier = Modifier.size(48.dp),
                imageVector = Icons.Default.SkipPrevious,
                contentDescription = "Play previous"
            )
        }
        IconButton(onClick = { onEvent(Event.SeekBackwardClicked) }) {
            Icon(
                modifier = Modifier.size(48.dp),
                imageVector = Icons.Default.Replay5,
                contentDescription = "Rewind 5 seconds"
            )
        }
        IconButton(onClick = { onEvent(Event.PlayPauseClicked) }) {
            val (icon, description) = if (screenState.isPlaying) {
                Icons.Default.Pause to "Pause"
            } else {
                Icons.Default.PlayArrow to "Play"
            }
            Icon(
                modifier = Modifier.size(48.dp),
                imageVector = icon,
                contentDescription = description
            )
        }
        IconButton(onClick = { onEvent(Event.SeekForwardClicked) }) {
            Icon(
                modifier = Modifier.size(48.dp),
                imageVector = Icons.Default.Forward10,
                contentDescription = "Forward 10 seconds"
            )
        }
        IconButton(onClick = { onEvent(Event.SkipNextClicked) }) {
            Icon(
                modifier = Modifier.size(48.dp),
                imageVector = Icons.Default.SkipNext,
                contentDescription = "Play next"
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun AudioPlayerPreview() {
    Content(screenState = ScreenState(
        isPlaying = false,
        currentPosition = 100f,
        totalDuration = 350.7f,
        currentTrackNumber = 2,
        totalTracks = 10,
        playbackSpeed = 1
    ), onEvent = {})
}
