package com.moneyweather.view.fragment

import android.os.Build
import android.os.Bundle
import android.text.Html
import android.text.method.LinkMovementMethod
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.viewModels
import com.google.firebase.analytics.FirebaseAnalytics
import com.moneyweather.R
import com.moneyweather.base.BaseKotlinFragment
import com.moneyweather.base.BaseKotlinViewModel
import com.moneyweather.databinding.FragmentNoticeDetailBinding
import com.moneyweather.model.enums.ActionBarLeftButtonEnum
import com.moneyweather.util.CommonUtils
import com.moneyweather.util.FirebaseAnalyticsManager
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class NoticeDetailFragment : BaseKotlinFragment<FragmentNoticeDetailBinding, BaseKotlinViewModel>() {
    override val layoutResourceId: Int get() = R.layout.fragment_notice_detail
    override val viewModel: BaseKotlinViewModel by viewModels()

    private var title: String? = ""
    private var content: String? = ""
    private var createdAt: String? = ""

    override fun initStartView() {
        FirebaseAnalyticsManager.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW, Bundle().apply {
            putString(FirebaseAnalyticsManager.VIEW_NAME, "공지 상세")
        })

        initActionBar(
            viewDataBinding.iActionBar,
            R.string.notice,
            ActionBarLeftButtonEnum.BACK_BUTTON
        )
        arguments?.let {
            it?.let {
                if (it != null) {
                    title = it.getString("title")!!
                    content = it.getString("content")!!
                    createdAt = it.getString("createdAt")!!
                }
            }
        }

        viewDataBinding.apply {
            tvSubject.text = title

            try {
                CommonUtils.newDateFormat(createdAt!!)
            } catch (e: Exception) {
            }

            tvContent.apply {
                text = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    Html.fromHtml(content, Html.FROM_HTML_MODE_LEGACY)
                } else {
                    Html.fromHtml(content)
                }
                movementMethod = LinkMovementMethod.getInstance()
            }
        }
    }

    override fun onResume() {
        super.onResume()

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, onBackPressedCallback)
    }

    private val onBackPressedCallback: OnBackPressedCallback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            onPressedBackkey()
        }
    }

    override fun onClickActionBarLeftButton() {
        super.onClickActionBarLeftButton()
        onPressedBackkey()
    }

    override fun onChildResume() {
        super.onChildResume()

    }

}