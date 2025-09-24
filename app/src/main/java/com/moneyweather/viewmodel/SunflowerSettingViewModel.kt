package com.moneyweather.viewmodel

import com.moneyweather.base.BaseKotlinViewModel
import com.moneyweather.data.remote.model.ApiUserModel
import com.moneyweather.data.remote.request.PointSoundVibrateRequest
import com.moneyweather.event.EventDelegate
import com.moneyweather.event.sunflowersetting.SettingSunflowerUiEffect
import com.moneyweather.event.sunflowersetting.SettingSunflowerUiEvent
import com.moneyweather.util.PrefRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.flow.MutableStateFlow
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class SunflowerSettingViewModel @Inject constructor(
    val apiUserModel: ApiUserModel
) : BaseKotlinViewModel(),
    EventDelegate<SettingSunflowerUiEffect, SettingSunflowerUiEvent> by EventDelegate.EventDelegateImpl() {

    private val _savePointSound: MutableStateFlow<Boolean> = MutableStateFlow(PrefRepository.SettingInfo.useSavePointSound)
    val savePointSound: MutableStateFlow<Boolean> = _savePointSound

    private val _savePointVibrate: MutableStateFlow<Boolean> = MutableStateFlow(PrefRepository.SettingInfo.useSavePointVibration)
    val savePointVibrate: MutableStateFlow<Boolean> = _savePointVibrate

    override fun dispatchEvent(event: SettingSunflowerUiEvent) {
        when (event) {
            is SettingSunflowerUiEvent.FetchSettingInfo -> {
                getPointSoundVibrate()
            }

            is SettingSunflowerUiEvent.CheckSettingSound -> {
                fetchPointSoundVibrate(
                    PointSoundVibrateRequest(
                        useSound = !savePointSound.value,
                        useVibrate = savePointVibrate.value
                    )
                )
            }

            is SettingSunflowerUiEvent.CheckSettingVibrate -> {
                fetchPointSoundVibrate(
                    PointSoundVibrateRequest(
                        useSound = savePointSound.value,
                        useVibrate = !savePointVibrate.value
                    )
                )
            }
        }
    }

    private fun getPointSoundVibrate() {
        addDisposable(
            apiUserModel.getPointSoundVibrate()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    if (it.isSuccessful) {
                        it.body()?.let { result ->
                            updatePointSoundVibrate(
                                useSound = result.data.useSound,
                                useVibrate = result.data.useVibrate
                            )
                        }
                    }
                }, {
                    throwableCheck(it).let {
                        Timber.tag(TAG).e("${it.errorMessage}")
                    }
                })
        )
    }

    private fun fetchPointSoundVibrate(request: PointSoundVibrateRequest) {
        addDisposable(
            apiUserModel.updatePointSoundVibrate(request)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    if (it.isSuccessful) {
                        it.body()?.let {
                            updatePointSoundVibrate(
                                useSound = request.useSound,
                                useVibrate = request.useVibrate
                            )
                        }
                    }

                }, {
                    throwableCheck(it).let {
                        Timber.tag(TAG).e("${it.errorMessage}")
                    }
                })
        )
    }

    private fun updatePointSoundVibrate(useSound: Boolean, useVibrate: Boolean) {
        _savePointSound.value = useSound
        _savePointVibrate.value = useVibrate

        PrefRepository.SettingInfo.useSavePointSound = useSound
        PrefRepository.SettingInfo.useSavePointVibration = useVibrate
    }
}
