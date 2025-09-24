package com.moneyweather.view.fragment

import android.os.Build
import android.text.Html
import androidx.fragment.app.viewModels
import com.moneyweather.R
import com.moneyweather.base.BaseKotlinFragment
import com.moneyweather.base.BaseKotlinViewModel
import com.moneyweather.databinding.FragmentFaqDetailBinding
import com.moneyweather.model.enums.ActionBarLeftButtonEnum
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class FaqDetailFragment : BaseKotlinFragment<FragmentFaqDetailBinding, BaseKotlinViewModel>() {
    override val layoutResourceId: Int get() = R.layout.fragment_faq_detail
    override val viewModel: BaseKotlinViewModel by viewModels()

    private var title: String = ""
    private var content: String = ""

    override fun initStartView() {
        initActionBar(
            viewDataBinding.iActionBar,
            R.string.faq,
            ActionBarLeftButtonEnum.BACK_BUTTON
        )
        arguments?.let {
            title = it.getString("title")!!
            content = it.getString("content")!!
        }

        viewDataBinding.apply {
            tvSubject.text = title

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
                tvContent.text = Html.fromHtml(content, Html.FROM_HTML_MODE_LEGACY)
            else
                tvContent.text = Html.fromHtml(content)
        }
    }


    override fun onChildResume() {
        super.onChildResume()

    }

}