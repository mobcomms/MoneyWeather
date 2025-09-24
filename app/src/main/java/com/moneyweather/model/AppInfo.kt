package com.moneyweather.model

import androidx.lifecycle.MutableLiveData
import com.moneyweather.R
import com.moneyweather.base.BaseApplication
import com.moneyweather.data.remote.response.AvailablePointResponse
import com.moneyweather.data.remote.response.UserPointResponse
import com.moneyweather.util.CommonUtils
import timber.log.Timber

class AppInfo {
    companion object {

        val TAG = this::class.java.simpleName

        @JvmStatic
        val appVersion = String.format(BaseApplication.appContext().getString(R.string.app_version), CommonUtils.getAppVersion())

        @JvmStatic
        private val _userInfo: MutableLiveData<User> = MutableLiveData()

        @JvmStatic
        private val _pointInfo: MutableLiveData<UserPointResponse.Data> = MutableLiveData()

        @JvmStatic
        private val _availablePointInfo: MutableLiveData<AvailablePointResponse.Data> = MutableLiveData()

        @JvmStatic
        private val _currentWeatherInfo: MutableLiveData<Weather> = MutableLiveData()

        @JvmStatic
        private val _regionInfo: MutableLiveData<Region> = MutableLiveData()


        @JvmStatic
        val userInfo: MutableLiveData<User>?
            get() = _userInfo

        @JvmStatic
        val pointInfo: MutableLiveData<UserPointResponse.Data>?
            get() = _pointInfo

        @JvmStatic
        val availablePointInfo: MutableLiveData<AvailablePointResponse.Data>?
            get() = _availablePointInfo

        @JvmStatic
        val currentWeatherInfo: MutableLiveData<Weather>?
            get() = _currentWeatherInfo

        @JvmStatic
        val regionInfo: MutableLiveData<Region>?
            get() = _regionInfo

        @JvmStatic
        var signInInfo: MutableLiveData<User> = MutableLiveData()

        fun setUserInfo(m: User) {
            Timber.d("user info refresh $m")
            _userInfo.postValue(m)
        }

        fun setUserPoint(m: UserPointResponse.Data) {
            _pointInfo.postValue(m)
        }

        fun setUserAvailablePoint(m: AvailablePointResponse.Data) {
            _availablePointInfo.postValue(m)
        }

        fun setCurrentWeather(m: Weather) {
            _currentWeatherInfo.postValue(m)
        }

        fun setRegion(m: Region) {
            _regionInfo.postValue(m)
        }
    }
}