package com.moneyweather.viewmodel

import android.widget.Toast
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ProcessLifecycleOwner
import androidx.lifecycle.lifecycleScope
import com.moneyweather.adapter.NoticeAdapter
import com.moneyweather.base.BaseApplication
import com.moneyweather.base.BaseKotlinViewModel
import com.moneyweather.data.remote.model.ApiUserModel
import com.moneyweather.model.Notice
import com.moneyweather.model.enums.ResultCode
import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NoticeViewModel @Inject constructor(
    val api: ApiUserModel,
    val mNoticeAdapter: NoticeAdapter
) : BaseKotlinViewModel() {

    val listAdapter: MutableLiveData<NoticeAdapter> = MutableLiveData()
    val clickListNum: MutableLiveData<Notice> = MutableLiveData()

    override fun onCreate() {
        super.onCreate()
        listAdapter.value = mNoticeAdapter

        mNoticeAdapter.onItemClickListener = object : NoticeAdapter.OnItemClickListener {
            override fun onItemClick(m: Notice, position: Int) {
                clickListNum.value = m
            }
        }
    }

    fun connectNoticeList() {
        mNoticeAdapter.submitList(null)

        addDisposable(
            api.noticeListInfo(1, 1000)
                .subscribeOn(Schedulers.io())
                .doOnSubscribe { showIndicator() }
                .doOnSuccess { dismissIndicator() }
                .doOnError { dismissIndicator() }
                .observeOn(AndroidSchedulers.mainThread()).subscribe({
                    it.run {
                        dismissIndicator()
                        commonResultLiveData.value = it.data.list

                        if (it.result == ResultCode.SUCCESS.resultCode) {
                            listAdapter.value?.submitList(it.data.list)
                        }
                    }
                }, {
                    val body = throwableCheck(it)
                    body?.let {
                        when (it.errorCode) {
                            ResultCode.SESSION_EXPIRED.resultCode -> {
                                ProcessLifecycleOwner.get().lifecycleScope.launch {
                                    delay(200)
                                    connectNoticeList()
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


}
