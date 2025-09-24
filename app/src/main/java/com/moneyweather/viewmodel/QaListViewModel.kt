package com.moneyweather.viewmodel

import android.widget.Toast
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ProcessLifecycleOwner
import androidx.lifecycle.lifecycleScope
import com.moneyweather.adapter.QaListAdapter
import com.moneyweather.base.BaseApplication
import com.moneyweather.base.BaseKotlinViewModel
import com.moneyweather.data.remote.model.ApiUserModel
import com.moneyweather.model.Qa
import com.moneyweather.model.enums.ResultCode
import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class QaListViewModel @Inject constructor(
    val api: ApiUserModel,
    val mQaAdapter: QaListAdapter
) : BaseKotlinViewModel() {

    val listAdapter: MutableLiveData<QaListAdapter> = MutableLiveData()
    val clickListNum: MutableLiveData<Int> = MutableLiveData()
    val listEmpty: MutableLiveData<Boolean> = MutableLiveData()

    override fun onCreate() {
        super.onCreate()
        listAdapter.value = mQaAdapter

        mQaAdapter.onItemClickListener = object : QaListAdapter.OnItemClickListener {
            override fun onItemClick(m: Qa, position: Int) {
                clickListNum.value = m.inquiryId
            }
        }
    }

    fun connectQaList() {
        mQaAdapter.submitList(null)

        addDisposable(
            api.qaListInfo()
                .subscribeOn(Schedulers.io())
                .doOnSubscribe { showIndicator() }
                .doOnSuccess { dismissIndicator() }
                .doOnError { dismissIndicator() }
                .observeOn(AndroidSchedulers.mainThread()).subscribe({
                    it.run {
                        dismissIndicator()
                        commonResultLiveData.value = it.data.list

                        if (it.data.list.size > 0) {
                            listAdapter.value?.submitList(it.data.list)
                            listEmpty.value = false
                        } else
                            listEmpty.value = true
                    }
                }, {
                    val body = throwableCheck(it)
                    body?.let {
                        when (it.errorCode) {
                            ResultCode.SESSION_EXPIRED.resultCode -> {
                                ProcessLifecycleOwner.get().lifecycleScope.launch {
                                    delay(200)
                                    connectQaList()
                                }
                            }

                            else -> {
                                Toast.makeText(
                                    BaseApplication.appContext(),
                                    it.errorMessage,
                                    Toast.LENGTH_SHORT
                                ).show()
                                commonResultLiveData.value = listOf(it.errorMessage)
                                listEmpty.value = true
                            }
                        }
                    }

                })
        )


    }


}
