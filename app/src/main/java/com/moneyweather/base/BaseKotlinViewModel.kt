package com.moneyweather.base

import android.content.Context
import android.content.Intent
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.gson.Gson
import com.moneyweather.R
import com.moneyweather.data.remote.response.AvailablePointResponse
import com.moneyweather.data.remote.response.UserPointResponse
import com.moneyweather.listener.LifeCycleListener
import com.moneyweather.listener.SoftKeyboardStatusListener
import com.moneyweather.model.AppInfo
import com.moneyweather.model.ErrorBody
import com.moneyweather.model.Region
import com.moneyweather.model.User
import com.moneyweather.model.Weather
import com.moneyweather.util.CustomToast
import com.moneyweather.util.NetworkUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import retrofit2.HttpException
import java.net.UnknownHostException
import javax.inject.Inject

@HiltViewModel
open class BaseKotlinViewModel @Inject constructor() : ViewModel(), LifecycleEventObserver, LifeCycleListener,
    SoftKeyboardStatusListener {
    val TAG = this::class.java.simpleName

    val commonResultLiveData: MutableLiveData<List<*>?> = MutableLiveData()

    val activityFinish: MutableLiveData<Boolean> = MutableLiveData(false)
    val finishAffinity: MutableLiveData<Boolean> = MutableLiveData(false)

    var startActivityListener: StartActivityListener? = null
    var user: MutableLiveData<User>?
        get() = AppInfo.userInfo
        set(_) {}

    var userPoint: MutableLiveData<UserPointResponse.Data>?
        get() = AppInfo.pointInfo
        set(_) {}

    var userAvailablePoint: MutableLiveData<AvailablePointResponse.Data>?
        get() = AppInfo.availablePointInfo
        set(_) {}

    var currentWeather: MutableLiveData<Weather>?
        get() = AppInfo.currentWeatherInfo
        set(_) {}

    var region: MutableLiveData<Region>?
        get() = AppInfo.regionInfo
        set(_) {}

    var signInInfo: MutableLiveData<User>?
        get() = AppInfo.signInInfo
        set(_) {}


    /**
     * RxJava 의 observing을 위한 부분.
     * addDisposable을 이용하여 추가하기만 하면 된다
     */
    private val compositeDisposable = CompositeDisposable()

    fun addDisposable(disposable: Disposable) {
        compositeDisposable.add(disposable)
    }

    override fun onCleared() {
        compositeDisposable.clear()

        super.onCleared()
    }

    fun throwableCheck(throwable: Throwable?): ErrorBody {
        throwable.let {
            when (it) {
                is UnknownHostException -> {
                    if (NetworkUtils.checkNetworkState(getContext())) {

                    } else {
                        CustomToast.showToast(
                            getContext(),
                            getString(R.string.network_connection_check)
                        )
                    }
                }

                is HttpException -> {
                    val error = throwable as HttpException

                    val errorBody: ErrorBody? = try {
                        error.response()?.errorBody()?.string()?.let { jsonString ->
                            Gson().fromJson(jsonString, ErrorBody::class.java)
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                        FirebaseCrashlytics.getInstance().recordException(e)

                        val errorMessage = e.message ?: "Unknown error"
                        val errorBodyString =
                            error.response()?.errorBody()?.string() ?: "No error body"

                        FirebaseCrashlytics.getInstance().log("$errorMessage : $errorBodyString")

                        null
                    }

                    if (errorBody != null) {
                        return errorBody
                    }
                }

                else -> {}
            }
        }
        return ErrorBody()
    }


    fun getContext(): Context {
        return BaseApplication.appContext()
    }

    fun getString(strId: Int): String {
        return getContext().getString(strId)
    }

    override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
        when (event) {
            Lifecycle.Event.ON_CREATE -> {
                onCreate()
            }

            else -> {
            }
        }
    }

    override fun onCreate() {
    }

    override fun onSoftKeyboardStatus(status: Boolean) {
    }

    fun startActivity(c: Class<*>?) {
        startActivityListener?.onStartActivity(Intent(getContext(), c))
    }

    fun startActivity(intent: Intent) {
        startActivityListener?.onStartActivity(intent)
    }

    fun startActivity(c: Class<*>?, any: Any) {
        var i = Intent(getContext(), c)
        i.putExtra("json", Gson().toJson(any))
        startActivityListener?.onStartActivity(i)
    }

    fun onStartActivityForResult(intent: Intent, requestCode: Int) {
        startActivityListener?.onStartActivityForResult(intent, requestCode)
    }

    fun onStartActivityForResult(c: Class<*>?, requestCode: Int) {
        startActivityListener?.onStartActivityForResult(Intent(getContext(), c), requestCode)
    }

    interface StartActivityListener {
        fun onStartActivity(intent: Intent)

        fun onStartActivityForResult(intent: Intent, requestCode: Int)
    }


    fun showIndicator() {
        BaseApplication.lockScreen()
    }

    fun dismissIndicator() {
        BaseApplication.unlockScreen()
    }


}