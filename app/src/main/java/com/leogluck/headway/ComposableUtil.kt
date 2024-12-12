package com.leogluck.headway

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.platform.LocalContext

@Composable
@ReadOnlyComposable
fun getString(@StringRes stringResId: Int) = getContext().getString(stringResId)

@Composable
@ReadOnlyComposable
fun getBitmap(@DrawableRes drawableResId: Int): Bitmap =
    BitmapFactory.decodeResource(getContext().resources, drawableResId)

@Composable
fun formatSecondsToMMSS(seconds: Float): String {
    val totalSeconds = seconds.toInt()
    val minutes = totalSeconds / 60
    val remainingSeconds = totalSeconds % 60

    return String.format(getString(R.string.mm_ss), minutes, remainingSeconds)
}

@Composable
@ReadOnlyComposable
private fun getContext() = LocalContext.current