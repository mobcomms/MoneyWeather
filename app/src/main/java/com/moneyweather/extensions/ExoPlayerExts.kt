package com.moneyweather.extensions

import android.content.Context
import android.net.Uri
import com.google.android.exoplayer2.C.VIDEO_SCALING_MODE_SCALE_TO_FIT
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.Player.REPEAT_MODE_ONE
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.source.DefaultMediaSourceFactory
import com.moneyweather.util.exoplayer.ExoPlayerDataSourceFactory

fun Context.createExoPlayer(): SimpleExoPlayer {
    val cacheFactory = ExoPlayerDataSourceFactory.getDataSourceFactory(this)
    return SimpleExoPlayer.Builder(this)
        .setMediaSourceFactory(DefaultMediaSourceFactory(cacheFactory))
        .build()
        .apply {
            repeatMode = REPEAT_MODE_ONE
            videoScalingMode = VIDEO_SCALING_MODE_SCALE_TO_FIT
            playWhenReady = false
        }
}

fun SimpleExoPlayer.prepareVideo(videoUri: Uri) {
    val mediaItem = MediaItem.fromUri(videoUri)
    setMediaItem(mediaItem)
    prepare()
}

fun SimpleExoPlayer.playVideo() {
    if (!isPlaying) {
        seekTo(0)
        playWhenReady = true
    }
}

fun SimpleExoPlayer.pauseVideo() {
    if (isPlaying) {
        playWhenReady = false
    }
}