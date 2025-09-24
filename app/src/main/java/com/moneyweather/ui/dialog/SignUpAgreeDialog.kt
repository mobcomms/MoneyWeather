package com.moneyweather.ui.dialog

import android.animation.Animator
import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.Paint
import android.graphics.drawable.ColorDrawable
import android.view.View
import android.view.Window
import com.moneyweather.databinding.DialogSignUpBinding
import com.moneyweather.listener.OnClickDialogButtonListener
import com.moneyweather.util.CommonUtils

class SignUpAgreeDialog(context: Context, builder: Builder) : Dialog(context), View.OnClickListener {

    private lateinit var binding: DialogSignUpBinding

    val title: String
    private val onClickEventListener: OnClickDialogButtonListener?

    class Builder {
        var onClickEventListener: OnClickDialogButtonListener? = null
        var title: String = ""
        var ownerActivity: Activity? = null

        fun setTitle(s: String): Builder {
            this.title = s
            return this
        }

        fun setOnClickEventListener(listener: OnClickDialogButtonListener?): Builder {
            this.onClickEventListener = listener
            return this
        }

        fun setOwnerActivity(activity: Activity): Builder {
            this.ownerActivity = activity
            return this
        }

        fun build(context: Context): SignUpAgreeDialog {
            return SignUpAgreeDialog(context, this)
        }
    }

    init {
        title = builder.title
        onClickEventListener = builder.onClickEventListener
        builder.ownerActivity?.let { setOwnerActivity(it) }

        initView()
    }

    private fun initView() {
        binding = DialogSignUpBinding.inflate(layoutInflater)

        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(binding.root)

        window?.apply {
            setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            attributes.width = CommonUtils.getDisplayWidth(context)
        }

        binding.apply {
            agreeLayout.setOnClickListener(this@SignUpAgreeDialog)
            item1.setOnClickListener(this@SignUpAgreeDialog)
            item2.setOnClickListener(this@SignUpAgreeDialog)
            item3.setOnClickListener(this@SignUpAgreeDialog)
            item4.setOnClickListener(this@SignUpAgreeDialog)
            item5.setOnClickListener(this@SignUpAgreeDialog)
            terms.setOnClickListener(this@SignUpAgreeDialog)
            terms.paintFlags = Paint.UNDERLINE_TEXT_FLAG
            privacy.setOnClickListener(this@SignUpAgreeDialog)
            privacy.paintFlags = Paint.UNDERLINE_TEXT_FLAG
            nextBtn.setOnClickListener(this@SignUpAgreeDialog)
            prevBtn.setOnClickListener(this@SignUpAgreeDialog)
        }
    }

    private fun slideAnimation(state: Boolean) {
        val movingY = (CommonUtils.getDisplayHeight(context)).toFloat()
        if (state) {
            binding.clPopupContainer.translationY = movingY
            binding.clPopupContainer.animate().translationY(0f).setDuration(300).start()
        } else {
            binding.clPopupContainer.animate().translationY(movingY).setDuration(300)
                .setListener(object : Animator.AnimatorListener {

                    override fun onAnimationStart(p0: Animator) {
                    }

                    override fun onAnimationEnd(p0: Animator) {
                        dismiss()
                    }

                    override fun onAnimationCancel(p0: Animator) {
                    }

                    override fun onAnimationRepeat(p0: Animator) {
                    }
                }).start()
        }
    }


    override fun onBackPressed() {
        slideAnimation(false)
//        super.onBackPressed()
    }

    private fun isSelectAll(): Boolean = with(binding) { checkbox1.isSelected && checkbox2.isSelected && checkbox3.isSelected }

    override fun onClick(v: View) {
        binding.apply {
            when (v) {
                agreeLayout -> {
                    if (isSelectAll()) {
                        checkbox.isSelected = false
                        checkbox1.isSelected = false
                        checkbox2.isSelected = false
                        checkbox3.isSelected = false
                        checkbox4.isSelected = false
                        checkbox5.isSelected = false
                    } else {
                        checkbox.isSelected = true
                        checkbox1.isSelected = true
                        checkbox2.isSelected = true
                        checkbox3.isSelected = true
                        checkbox4.isSelected = true
                        checkbox5.isSelected = true
                    }
                }

                item1 -> {
                    checkbox1.isSelected = !checkbox1.isSelected
                    checkbox.isSelected = isSelectAll()
                }

                item2 -> {
                    checkbox2.isSelected = !checkbox2.isSelected
                    checkbox.isSelected = isSelectAll()
                }

                item3 -> {
                    checkbox3.isSelected = !checkbox3.isSelected
                    checkbox.isSelected = isSelectAll()
                }

                item4 -> checkbox4.isSelected = !checkbox4.isSelected
                item5 -> checkbox5.isSelected = !checkbox5.isSelected

                prevBtn -> {
                    onClickEventListener?.onClickClose()
                    slideAnimation(false)
                    dismiss()
                }

                nextBtn -> {
                    onClickEventListener?.onClickConfirm()
                    slideAnimation(false)
                    dismiss()
                }

                terms -> {
                    onClickEventListener?.onClickTerm()
                }

                privacy -> {
                    onClickEventListener?.onClickPrivacy()
                }

            }

            nextBtn.isEnabled = isSelectAll()
        }
    }
}
