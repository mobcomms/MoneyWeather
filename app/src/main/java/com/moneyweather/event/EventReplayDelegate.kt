package com.moneyweather.event

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow

data class ConsumableEvent<T>(val data: T, var consumed: Boolean = false) {
    fun markConsumed() {
        consumed = true
    }
}

interface EventReplayDelegate<EF, EV> {
    val replayEffect: SharedFlow<ConsumableEvent<EF>>

    suspend fun emitReplayEffect(effect: EF)

    fun dispatchReplayEvent(event: EV)

    class EventReplayDelegateImpl<EF, EV> : EventReplayDelegate<EF, EV> {
        private val _replayEffect = MutableSharedFlow<ConsumableEvent<EF>>(replay = 1, extraBufferCapacity = 1)
        override val replayEffect = _replayEffect.asSharedFlow()

        override suspend fun emitReplayEffect(effect: EF) {
            _replayEffect.emit(ConsumableEvent(effect))
        }

        override fun dispatchReplayEvent(event: EV) {}
    }
}
