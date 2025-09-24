package com.moneyweather.view.fragment

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.google.firebase.analytics.FirebaseAnalytics
import com.moneyweather.R
import com.moneyweather.base.BaseKotlinFragment
import com.moneyweather.databinding.FragmentFindIdBinding
import com.moneyweather.model.enums.ActionBarLeftButtonEnum
import com.moneyweather.util.FirebaseAnalyticsManager
import com.moneyweather.viewmodel.FindIdViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class FindIdFragment : BaseKotlinFragment<FragmentFindIdBinding, FindIdViewModel>() {
    override val layoutResourceId: Int get() = R.layout.fragment_find_id
    override val viewModel: FindIdViewModel by viewModels()

    override fun initStartView() {
        FirebaseAnalyticsManager.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW, Bundle().apply {
            putString(FirebaseAnalyticsManager.VIEW_NAME, "아이디 찾기")
        })

        viewDataBinding.vm = viewModel
        initActionBar(viewDataBinding.iActionBar, R.string.empty, ActionBarLeftButtonEnum.BACK_BUTTON)

        viewModel.emailResult.observe(this, Observer {

            if (!it.isNullOrEmpty()) {
                startFragment(R.id.fragmentContainer, FindIdCompleteFragment::class.java, Intent().putExtra("email", it.toString()))
            } else {
                Toast.makeText(context, getText(R.string.find_id_error), Toast.LENGTH_SHORT).show()
            }
        })


        viewDataBinding.apply {

            editName.addTextChangedListener(object : TextWatcher {
                override fun afterTextChanged(p0: Editable?) {
                    when (p0.toString().length) {
                        0 -> {
                            editName.visibleClearButton(false)
                        }

                        else -> {
                            editName.visibleClearButton(true)
                            btnNext.isEnabled = if (p0.toString().length > 1 && editPhoneNum.text.length > 9) true else false
                        }
                    }


                }

                override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
                override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            })

            editPhoneNum.addTextChangedListener(object : TextWatcher {
                override fun afterTextChanged(p0: Editable?) {
                    when (p0.toString().length) {
                        0 -> {
                            editPhoneNum.visibleClearButton(false)
                        }

                        else -> {
                            editPhoneNum.visibleClearButton(true)
                            btnNext.isEnabled = if (p0.toString().length > 9 && editName.text.length > 1) true else false
                        }
                    }


                }

                override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
                override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            })


            btnNext.setOnClickListener {
                viewModel.findEmail(editName.text.toString(), editPhoneNum.text.toString())
            }


        }

    }


}