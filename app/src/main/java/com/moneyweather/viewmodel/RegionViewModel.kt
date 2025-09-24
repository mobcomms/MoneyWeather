package com.moneyweather.viewmodel

import android.widget.Toast
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ProcessLifecycleOwner
import androidx.lifecycle.lifecycleScope
import com.moneyweather.adapter.RegionAdapter
import com.moneyweather.base.BaseApplication
import com.moneyweather.base.BaseKotlinViewModel
import com.moneyweather.data.remote.model.ApiUserModel
import com.moneyweather.model.enums.ResultCode
import com.moneyweather.util.PrefRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RegionViewModel @Inject constructor(
    val api: ApiUserModel,
    val mRegionAdapter: RegionAdapter
) : BaseKotlinViewModel() {

    val listAdapter: MutableLiveData<RegionAdapter> = MutableLiveData()
    var noPermission: MutableLiveData<Boolean> = MutableLiveData()

    override fun onCreate() {
        super.onCreate()
        listAdapter.value = mRegionAdapter
        noPermission.value = false
    }

    fun connectRegionList() {
        mRegionAdapter.submitList(null)

        addDisposable(
            api.regionListInfo(
                PrefRepository.LocationInfo.latitude,
                PrefRepository.LocationInfo.longitude
            )
                .subscribeOn(Schedulers.io())
                .doOnSubscribe { showIndicator() }
                .doOnSuccess { dismissIndicator() }
                .doOnError { dismissIndicator() }
                .observeOn(AndroidSchedulers.mainThread()).subscribe({
                    it.run {
                        dismissIndicator()
                        if (it.result == 0) {
                            if (!it.data.isEmpty()) {
                                if (noPermission.value == true)
                                    it.data.removeAt(0)
                                it.data[0].isPermission = noPermission.value!!
                                listAdapter.value?.submitList(it.data)
                            }

                        }
                    }
                }, {
                    val body = throwableCheck(it)
                    body?.let {
                        when (it.errorCode) {
                            ResultCode.SESSION_EXPIRED.resultCode -> {
                                ProcessLifecycleOwner.get().lifecycleScope.launch {
                                    delay(200)
                                    connectRegionList()
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
