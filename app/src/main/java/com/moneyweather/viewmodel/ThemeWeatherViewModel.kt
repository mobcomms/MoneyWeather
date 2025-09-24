package com.moneyweather.viewmodel

import android.text.TextUtils
import androidx.lifecycle.ProcessLifecycleOwner
import androidx.lifecycle.lifecycleScope
import com.moneyweather.base.BaseKotlinViewModel
import com.moneyweather.constants.ThemeConstants
import com.moneyweather.data.remote.model.ApiUserModel
import com.moneyweather.event.EventDelegate
import com.moneyweather.event.theme.ThemeWeatherUiEffect
import com.moneyweather.event.theme.ThemeWeatherUiEvent
import com.moneyweather.model.Weather
import com.moneyweather.model.enums.ResultCode
import com.moneyweather.util.DateUtil
import com.moneyweather.util.Logger
import com.moneyweather.util.PrefRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ThemeWeatherViewModel @Inject constructor(
    val apiUserModel: ApiUserModel,
) : BaseKotlinViewModel(),
    EventDelegate<ThemeWeatherUiEffect, ThemeWeatherUiEvent> by EventDelegate.EventDelegateImpl() {

    private val lastApiCallDate = MutableStateFlow("")

    private val _weatherInfos: MutableStateFlow<List<Weather>?> = MutableStateFlow(PrefRepository.LockQuickInfo.infoTheme?.data?.weatherInfo?.hourWeathers)
    val weatherInfos = _weatherInfos.asStateFlow()

    companion object {
        const val HOURLY_WEATHER_SIZE = 12 // default 7, max 21
    }

    private fun checkLastApiCallDateValidity(): Boolean {
        val lastCall = lastApiCallDate.value
        val isEmpty = TextUtils.isEmpty(lastCall)

        return isEmpty || DateUtil.intervalBetweenDateText(lastCall) >= ThemeConstants.LOAD_THEME_DATA_INTERVAL_MIN
    }


    private fun updateLastApiCallDate() {
        lastApiCallDate.update { DateUtil.getTime() }
    }

    override fun dispatchEvent(event: ThemeWeatherUiEvent) {
        when (event) {
            ThemeWeatherUiEvent.FetchHourlyWeather -> {
                getHourlyWeather(PrefRepository.LocationInfo.latitude, PrefRepository.LocationInfo.longitude)
            }

            ThemeWeatherUiEvent.FetchWeeklyWeather -> {
                getWeeklyWeather(PrefRepository.LocationInfo.latitude, PrefRepository.LocationInfo.longitude)
            }
        }
    }

    private fun getHourlyWeather(lat: String, lon: String) {
        // LOAD_THEME_DATA_INTERVAL_MIN(1분) 이내에 api 재요청 시 무시
        if (!checkLastApiCallDateValidity()) return
        updateLastApiCallDate()

        addDisposable(
            apiUserModel.hourlyWeather(lat, lon, HOURLY_WEATHER_SIZE)
                .subscribeOn(Schedulers.io())
                .doOnSubscribe { showIndicator() }
                .doOnSuccess { dismissIndicator() }
                .doOnError { dismissIndicator() }
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    _weatherInfos.value = it.data.weathers
                }, {
                    val body = throwableCheck(it)
                    body?.let {
                        when (it.errorCode) {
                            ResultCode.SESSION_EXPIRED.resultCode -> {
                                ProcessLifecycleOwner.get().lifecycleScope.launch {
                                    delay(200)
                                    getHourlyWeather(lat, lon)
                                }
                            }

                            else -> {
                                Logger.e(it.errorMessage)
                            }
                        }
                    }
                })
        )
    }

    private fun getWeeklyWeather(lat: String, lon: String) {
        addDisposable(
            apiUserModel.weeklyWeather(lat, lon)
                .subscribeOn(Schedulers.io())
                .doOnSubscribe { showIndicator() }
                .doOnSuccess { dismissIndicator() }
                .doOnError { dismissIndicator() }
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    _weatherInfos.value = it.data.weathers
                }, {
                    val body = throwableCheck(it)
                    body?.let {
                        when (it.errorCode) {
                            ResultCode.SESSION_EXPIRED.resultCode -> {
                                ProcessLifecycleOwner.get().lifecycleScope.launch {
                                    delay(200)
                                    getWeeklyWeather(lat, lon)
                                }
                            }

                            else -> {
                                Logger.e(it.errorMessage)
                            }
                        }
                    }
                })
        )
    }
}
