package com.moneyweather.viewmodel

import android.widget.Toast
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ProcessLifecycleOwner
import androidx.lifecycle.lifecycleScope
import com.moneyweather.adapter.FaqAdapter
import com.moneyweather.base.BaseApplication
import com.moneyweather.base.BaseKotlinViewModel
import com.moneyweather.data.remote.model.ApiUserModel
import com.moneyweather.model.Faq
import com.moneyweather.model.enums.ResultCode
import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FaqViewModel @Inject constructor(
    val api: ApiUserModel,
    val mFaqAdapter: FaqAdapter
) : BaseKotlinViewModel() {

    val listAdapter: MutableLiveData<FaqAdapter> = MutableLiveData()
    val clickListNum: MutableLiveData<Faq> = MutableLiveData()
    val categoryName: MutableLiveData<String> = MutableLiveData()

    override fun onCreate() {
        super.onCreate()
        listAdapter.value = mFaqAdapter

        mFaqAdapter.onItemClickListener = object : FaqAdapter.OnItemClickListener {
            override fun onItemClick(m: Faq) {
                clickListNum.value = m
            }
        }
    }

    fun connectFaqList(category: String) {
        mFaqAdapter.submitList(null)

        addDisposable(
            api.faqListInfo(category)
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
                                    connectFaqList(category)
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
