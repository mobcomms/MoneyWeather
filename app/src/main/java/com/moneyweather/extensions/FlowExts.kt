package com.moneyweather.extensions

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.collectLatest
import kotlin.time.Duration

fun <T> Flow<T>.throttleFirst(windowDuration: Duration): Flow<T> = channelFlow {
    var lastEmissionTime = 0L

    collectLatest { value ->
        val currentTime = System.currentTimeMillis()

        if (currentTime - lastEmissionTime >= windowDuration.inWholeMilliseconds) {
            lastEmissionTime = currentTime
            send(value)
        }
    }
}