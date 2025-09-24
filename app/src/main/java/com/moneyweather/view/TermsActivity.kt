package com.moneyweather.view

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import com.google.firebase.analytics.FirebaseAnalytics
import com.moneyweather.R
import com.moneyweather.base.BaseKotlinActivity
import com.moneyweather.base.BaseKotlinViewModel
import com.moneyweather.databinding.ActivityTermsBinding
import com.moneyweather.model.enums.ActionBarLeftButtonEnum
import com.moneyweather.model.enums.ActionBarRightButtonEnum
import com.moneyweather.model.enums.TermsType
import com.moneyweather.util.FirebaseAnalyticsManager
import com.moneyweather.util.PrefRepository
import dagger.hilt.android.AndroidEntryPoint
import java.io.Serializable


/**
 * 앱 인트로 화면
 */
@AndroidEntryPoint
class TermsActivity : BaseKotlinActivity<ActivityTermsBinding, BaseKotlinViewModel>(), View.OnClickListener {

    override val layoutResourceId: Int get() = R.layout.activity_terms
    override val viewModel: BaseKotlinViewModel by viewModels()

    private var mType: TermsType? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun initStartView() {
        intent?.let {
            mType = it.intentSerializable("type", TermsType::class.java)
        }

        var screenName = ""
        when (mType) {
            TermsType.SERVICE -> {
                screenName = getString(R.string.terms)
                initActionBar(viewDataBinding.iActionBar, R.string.terms, ActionBarLeftButtonEnum.BACK_BUTTON, ActionBarRightButtonEnum.NONE)
                setContent(PrefRepository.SettingInfo.termsUrl)
            }

            TermsType.PRIVACY -> {
                screenName = getString(R.string.privacy)
                initActionBar(viewDataBinding.iActionBar, R.string.privacy, ActionBarLeftButtonEnum.BACK_BUTTON)
                setContent(PrefRepository.SettingInfo.privacyUrl)
            }

            TermsType.LOCATION -> {
                screenName = getString(R.string.terms_of_location)
                initActionBar(viewDataBinding.iActionBar, R.string.terms_of_location, ActionBarLeftButtonEnum.BACK_BUTTON)
                setContent(PrefRepository.SettingInfo.locationUrl)
            }

            TermsType.COMPANY -> {
                screenName = getString(R.string.business_information)
                initActionBar(viewDataBinding.iActionBar, R.string.business_information, ActionBarLeftButtonEnum.BACK_BUTTON)
                setContent(PrefRepository.SettingInfo.companyUrl)
            }

            else -> {

            }
        }

        FirebaseAnalyticsManager.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW, Bundle().apply {
            putString(FirebaseAnalyticsManager.VIEW_NAME, screenName)
        })
    }

    private fun setContent(content: String) {
        viewDataBinding.webView.loadUrl(content)
    }


    override fun onBackPressed() {
        finish()
    }

    override fun onDestroy() {
        super.onDestroy()
    }


    fun <T : Serializable> Intent.intentSerializable(key: String, clazz: Class<T>): T? {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            this.getSerializableExtra(key, clazz)
        } else {
            this.getSerializableExtra(key) as T?
        }
    }

}