package com.moneyweather.util

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

object EventBus {
    private val _events = MutableSharedFlow<Any>()
    val events = _events.asSharedFlow()
    private val scope = CoroutineScope(Dispatchers.Default)

    fun post(event: Any) {
        scope.launch {
            _events.emit(event)
        }
    }

    //CustomScope이 필요한 경우 사용 ex) service..
    inline fun<reified T> receive(
        scope: CoroutineScope,
        dispatcher: CoroutineDispatcher? = null,
        crossinline action: (t: T) -> Unit) {

        events.filterIsInstance<T>().onEach {
            if (dispatcher == null) {
                action.invoke(it)
            } else {
                withContext(dispatcher) {
                    action.invoke(it)
                }
            }
        }.launchIn(scope)
    }

    inline fun<reified T> Fragment.eventReceive(
        dispatcher: CoroutineDispatcher? = null,
        crossinline action: (t: T) -> Unit) {

        events.filterIsInstance<T>().onEach {
            if (dispatcher == null) {
                action.invoke(it)
            } else {
                withContext(dispatcher) {
                    action.invoke(it)
                }
            }
        }.launchIn(lifecycleScope)
    }

    inline fun<reified T> FragmentActivity.eventReceive(
        dispatcher: CoroutineDispatcher? = null,
        crossinline action: (t: T) -> Unit) {

        events.filterIsInstance<T>().onEach {
            if (dispatcher == null) {
                action.invoke(it)
            } else {
                withContext(dispatcher) {
                    action.invoke(it)
                }
            }
        }.launchIn(lifecycleScope)
    }
}