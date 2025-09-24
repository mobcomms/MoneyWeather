package com.moneyweather.view.fragment

import android.content.Intent
import android.text.Editable
import android.text.TextWatcher
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.moneyweather.R
import com.moneyweather.base.BaseKotlinFragment
import com.moneyweather.databinding.FragmentCodeInputBinding
import com.moneyweather.model.enums.ActionBarLeftButtonEnum
import com.moneyweather.viewmodel.CodeInputViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class CodeInputFragment : BaseKotlinFragment<FragmentCodeInputBinding, CodeInputViewModel>() {
    override val layoutResourceId: Int get() = R.layout.fragment_code_input
    override val viewModel: CodeInputViewModel by viewModels()

    override fun initStartView() {
        viewDataBinding.vm = viewModel
        initActionBar(viewDataBinding.iActionBar, R.string.empty, ActionBarLeftButtonEnum.BACK_BUTTON)

        viewModel.resultVerify.observe(this, Observer {
            startFragment(
                R.id.fragmentContainer, ResetPwFragment::class.java,
                Intent().putExtra("code", viewDataBinding.editCode.text.toString())
            )
        })


        viewDataBinding.apply {

            editCode.setClearButtonImage(R.drawable.icon_input_check_on)

            editCode.addTextChangedListener(object : TextWatcher {
                override fun afterTextChanged(p0: Editable?) {
                    when (p0.toString().length) {
                        0 -> {
                            editCode.visibleClearButton(false)
                            btnNext.isEnabled = false
                        }

                        else -> {
                            editCode.visibleClearButton(true)
                            btnNext.isEnabled = true
                        }
                    }


                }

                override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
                override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            })



            btnNext.setOnClickListener {
                viewModel.sendCode(editCode.text.toString())
            }


        }

    }


}