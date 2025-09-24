package com.moneyweather.viewmodel

import androidx.lifecycle.viewModelScope
import com.moneyweather.base.BaseKotlinViewModel
import com.moneyweather.data.remote.model.ApiUserModel
import com.moneyweather.data.remote.response.AppVersionResponse
import com.moneyweather.event.EventReplayDelegate
import com.moneyweather.event.lockscreen.LockScreenUiEffect
import com.moneyweather.event.lockscreen.LockScreenUiEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LockScreenViewModel @Inject constructor(
    val apiUserModel: ApiUserModel,
) : BaseKotlinViewModel(),
    EventReplayDelegate<LockScreenUiEffect, LockScreenUiEvent> by EventReplayDelegate.EventReplayDelegateImpl() {

    private val _resultAppVersion = MutableSharedFlow<AppVersionResponse.Data>(replay = 1)
    val resultAppVersion: SharedFlow<AppVersionResponse.Data> get() = _resultAppVersion

    override fun dispatchReplayEvent(event: LockScreenUiEvent) {
        when (event) {
            is LockScreenUiEvent.RefreshData -> {
                refreshData(fromCreated = event.fromCreated, isThemeChange = event.isThemeChange)
            }

            is LockScreenUiEvent.AppUpdateCheck -> {
                requestAppVersion()
            }
        }
    }

    private fun refreshData(fromCreated: Boolean, isThemeChange: Boolean) {
        viewModelScope.launch {
            emitReplayEffect(LockScreenUiEffect.RefreshData(fromCreated = fromCreated, isThemeChange = isThemeChange))
        }
    }

    private fun requestAppVersion() {
        addDisposable(
            apiUserModel.appVersion()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ response ->
                    viewModelScope.launch {
                        _resultAppVersion.emit(response.data)
                    }
                }, {
                    throwableCheck(it)
                })
        )
    }
}
