package com.moneyweather.viewmodel

import android.widget.Toast
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ProcessLifecycleOwner
import androidx.lifecycle.lifecycleScope
import com.moneyweather.base.BaseApplication
import com.moneyweather.base.BaseKotlinViewModel
import com.moneyweather.data.remote.model.ApiUserModel
import com.moneyweather.model.ShopCategoryItem
import com.moneyweather.model.ShopProductItem
import com.moneyweather.model.enums.ResultCode
import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ShopViewModel @Inject constructor(
    val api: ApiUserModel
) : BaseKotlinViewModel() {

    var resultCategoryList: MutableLiveData<List<ShopCategoryItem>> = MutableLiveData()
    var resultProductCount: MutableLiveData<Int> = MutableLiveData()
    var resultProductList: MutableLiveData<List<ShopProductItem>> = MutableLiveData()

    fun connectCategoryList() {

        addDisposable(
            api.getCategoryList()
                .subscribeOn(Schedulers.io())
                .doOnSubscribe { showIndicator() }
                .doOnSuccess { dismissIndicator() }
                .doOnError { dismissIndicator() }
                .observeOn(AndroidSchedulers.mainThread()).subscribe({
                    it.run {
                        dismissIndicator()
                        if (it.result == 0) {
                            resultCategoryList.value = it.data.categories
                        }
                    }
                }, {
                    val body = throwableCheck(it)
                    body?.let {
                        when (it.errorCode) {
                            ResultCode.SESSION_EXPIRED.resultCode -> {
                                ProcessLifecycleOwner.get().lifecycleScope.launch {
                                    delay(200)
                                    connectCategoryList()
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

    fun connectSearchProduct(category: String, affiiliate: String, search: String, page: Int, type: Boolean) {

        addDisposable(
            api.getProductList(category, affiiliate, search, 30, page, type)
                .subscribeOn(Schedulers.io())
                .doOnSubscribe { showIndicator() }
                .doOnSuccess { dismissIndicator() }
                .doOnError { dismissIndicator() }
                .observeOn(AndroidSchedulers.mainThread()).subscribe({
                    it.run {
                        dismissIndicator()
                        if (it.result == 0) {
                            resultProductCount.value = it.data.pageInfo.totalCount
                            resultProductList.value = it.data.list

                        }
                    }
                }, {
                    val body = throwableCheck(it)
                    body?.let {
                        when (it.errorCode) {
                            ResultCode.SESSION_EXPIRED.resultCode -> {
                                ProcessLifecycleOwner.get().lifecycleScope.launch {
                                    delay(200)
                                    connectSearchProduct(category, affiiliate, search, page, type)
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
