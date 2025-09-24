package com.moneyweather.view.fragment

import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.moneyweather.R
import com.moneyweather.base.BaseKotlinFragment
import com.moneyweather.databinding.FragmentResetPwBinding
import com.moneyweather.model.enums.ActionBarLeftButtonEnum
import com.moneyweather.util.CommonUtils
import com.moneyweather.viewmodel.ResetPwViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ResetPwFragment : BaseKotlinFragment<FragmentResetPwBinding, ResetPwViewModel>() {
    override val layoutResourceId: Int get() = R.layout.fragment_reset_pw
    override val viewModel: ResetPwViewModel by viewModels()

    private var resetCode: String? = ""

    override fun initStartView() {
        viewDataBinding.vm = viewModel

        arguments?.let {
            resetCode = it.getString("code")
        }

        initActionBar(viewDataBinding.iActionBar, R.string.empty, ActionBarLeftButtonEnum.BACK_BUTTON)

        viewModel.resetResult.observe(this, Observer {
            replaceFragment(R.id.fragmentContainer, NewPasswordCompleteFragment::class.java)
        })


        viewDataBinding.apply {

            editPassword.addTextChangedListener(object : TextWatcher {
                override fun afterTextChanged(p0: Editable?) {
                    when (p0.toString().length) {
                        0 -> {
                            editPassword.visibleClearButton(false)
                        }

                        else -> {
                            editPassword.visibleClearButton(true)
                            btnNext.isEnabled = CommonUtils.isValidPassword(p0.toString()) && TextUtils.equals(
                                editPassword.text.toString(),
                                editPasswordConfirm.text.toString()
                            )
                        }
                    }


                }

                override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
                override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            })

            editPasswordConfirm.addTextChangedListener(object : TextWatcher {
                override fun afterTextChanged(p0: Editable?) {
                    when (p0.toString().length) {
                        0 -> {
                            editPasswordConfirm.visibleClearButton(false)
                        }

                        else -> {
                            editPasswordConfirm.visibleClearButton(true)
                            btnNext.isEnabled = CommonUtils.isValidPassword(p0.toString()) && TextUtils.equals(
                                editPassword.text.toString(),
                                editPasswordConfirm.text.toString()
                            )
                        }
                    }


                }

                override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
                override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            })


            btnNext.setOnClickListener {

                if (!TextUtils.equals(editPassword.text.toString(), editPasswordConfirm.text.toString())) {
                    Toast.makeText(context, getText(R.string.not_same_pw), Toast.LENGTH_SHORT).show()
                } else {
                    resetCode?.let {
                        viewModel.sendNewPass(it, editPassword.text.toString())
                    }
                }
            }
        }

    }


}