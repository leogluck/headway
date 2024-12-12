package com.leogluck.headway

import java.util.concurrent.TimeUnit

fun Float.positionToMillis() = TimeUnit.SECONDS.toMillis(this.toLong())

fun Long.millisToPosition() = TimeUnit.MILLISECONDS.toSeconds(this).toFloat()