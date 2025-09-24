package com.moneyweather.viewmodel

import android.annotation.SuppressLint
import android.widget.Toast
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ProcessLifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewModelScope
import com.moneyweather.adapter.PointHistoryAdapter
import com.moneyweather.base.BaseApplication
import com.moneyweather.base.BaseKotlinViewModel
import com.moneyweather.data.remote.model.ApiUserModel
import com.moneyweather.data.remote.response.PointHistoryResponse
import com.moneyweather.event.EventDelegate
import com.moneyweather.event.mypoint.MyPointUiEvent
import com.moneyweather.extensions.throttleFirst
import com.moneyweather.model.AppInfo
import com.moneyweather.model.PointHistoryItem
import com.moneyweather.model.enums.ResultCode
import com.moneyweather.view.StoreActivity
import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.time.Duration.Companion.milliseconds

@HiltViewModel
class MyPointViewModel @Inject constructor(
    val api: ApiUserModel,
    val mHistoryAdapter: PointHistoryAdapter
) : BaseKotlinViewModel(), EventDelegate<Unit, MyPointUiEvent> by EventDelegate.EventDelegateImpl() {

    companion object {
        const val FETCH_AVAILABLE_POINT_DELAY = 100
    }

    val listAdapter: MutableLiveData<PointHistoryAdapter> = MutableLiveData()
    var resultList: MutableLiveData<PointHistoryResponse> = MutableLiveData()
    var mType: Int = 0
    var mPage: Int = 1
    var mHistoryList: ArrayList<PointHistoryItem> = ArrayList()

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

    override fun onCreate() {
        super.onCreate()
        listAdapter.value = mHistoryAdapter

        mHistoryAdapter.onItemLastListener = object : PointHistoryAdapter.OnItemLastListener {
            override fun onItemLast() {
                mPage += 1
                connectList(mType, mPage)
            }
        }
    }

    private fun fetchAvailablePoint() {
        viewModelScope.launch {
            _fetchAvailablePointEvents.emit(Unit)
        }
    }

    override fun dispatchEvent(event: MyPointUiEvent) {
        when (event) {
            is MyPointUiEvent.FetchAvailablePoints -> {
                fetchAvailablePoint()
            }

            is MyPointUiEvent.RefreshData -> {
                fetchAvailablePoint()
                connectList(type = event.type, page = event.page)
            }
        }
    }

    @SuppressLint("SuspiciousIndentation")
    fun connectList(type: Int, page: Int) {
        if (mType != type || page == 1) {
            mHistoryList.clear()
        }

        mType = type
        mPage = page

        addDisposable(
            api.pointHistory(page, 30, type)
                .subscribeOn(Schedulers.io())
                .doOnSubscribe { showIndicator() }
                .doOnSuccess { dismissIndicator() }
                .doOnError { dismissIndicator() }
                .observeOn(AndroidSchedulers.mainThread()).subscribe({
                    it.run {
                        dismissIndicator()
                        if (page == 1) {
                            mHistoryList = it.data.list
                            resultList.value = it
                        } else
                            mHistoryList.addAll(it.data.list)

                        listAdapter.value?.submitList(mHistoryList)
                    }
                }, {
                    val body = throwableCheck(it)
                    body?.let {
                        when (it.errorCode) {
                            ResultCode.SESSION_EXPIRED.resultCode -> {
                                ProcessLifecycleOwner.get().lifecycleScope.launch {
                                    delay(200)
                                    connectList(type, page)
                                }
                            }

                            else -> {
                                Toast.makeText(
                                    BaseApplication.appContext(),
                                    it.errorMessage,
                                    Toast.LENGTH_SHORT
                                ).show()
                                commonResultLiveData.value = listOf(it.errorMessage)
                            }
                        }
                    }

                })
        )
    }

    fun onClickStore() {
        startActivity(StoreActivity::class.java)
    }

    private fun connectAvailablePoint() {
        addDisposable(
            api.pointAvailable()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ response ->
                    if (response.result == 0) {
                        AppInfo.setUserAvailablePoint(response.data)
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
