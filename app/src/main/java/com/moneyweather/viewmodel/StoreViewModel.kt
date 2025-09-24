package com.moneyweather.viewmodel

import android.widget.Toast
import androidx.lifecycle.ProcessLifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewModelScope
import com.moneyweather.base.BaseApplication
import com.moneyweather.base.BaseKotlinViewModel
import com.moneyweather.data.remote.model.ApiUserModel
import com.moneyweather.event.EventDelegate
import com.moneyweather.event.store.StoreUiEvent
import com.moneyweather.extensions.throttleFirst
import com.moneyweather.model.AppInfo
import com.moneyweather.model.enums.ResultCode
import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.android.schedulers.AndroidSchedulers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.time.Duration.Companion.milliseconds

@HiltViewModel
class StoreViewModel @Inject constructor(
    val apiUserModel: ApiUserModel
) : BaseKotlinViewModel(), EventDelegate<Unit, StoreUiEvent> by EventDelegate.EventDelegateImpl() {

    companion object {
        const val FETCH_AVAILABLE_POINT_DELAY = 100
    }

    private val _fetchAvailablePointEvents = MutableSharedFlow<Unit>()
    val fetchAvailablePointEvents = _fetchAvailablePointEvents.asSharedFlow()

    init {
        viewModelScope.launch {
            fetchAvailablePointEvents
                .throttleFirst(FETCH_AVAILABLE_POINT_DELAY.milliseconds)
                .collect {
                    connectAvailablePoint()
                }
        }
    }

    private fun fetchAvailablePoint() {
        viewModelScope.launch {
            _fetchAvailablePointEvents.emit(Unit)
        }
    }

    override fun dispatchEvent(event: StoreUiEvent) {
        when (event) {
            is StoreUiEvent.FetchAvailablePoints -> {
                fetchAvailablePoint()
            }
        }
    }

    private fun connectAvailablePoint() {
        addDisposable(
            apiUserModel.pointAvailable()
                .subscribeOn(io.reactivex.schedulers.Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    if (it.result == 0) {
                        AppInfo.setUserAvailablePoint(it.data)
                    }
                }, {
                    val body = throwableCheck(it)
                    body.let {
                        when (it.errorCode) {
                            ResultCode.SESSION_EXPIRED.resultCode -> {
                                ProcessLifecycleOwner.get().lifecycleScope.launch {
                                    delay(200)
                                    connectAvailablePoint()
                                }
                            }

                            else -> Toast.makeText(
                                BaseApplication.appContext(),
                                it.errorMessage,
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                })
        )
    }
}