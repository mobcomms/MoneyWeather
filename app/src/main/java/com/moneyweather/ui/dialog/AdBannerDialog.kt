package com.moneyweather.ui.dialog

import android.app.Dialog
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import com.moneyweather.databinding.DialogAdBannerBinding
import com.moneyweather.extensions.toPx


class AdBannerDialog(
    context: Context,
    bannerWidth: Int,
    bannerHeight: Int
) {

    private val dialog: Dialog
    private var onClickListener: OnClickListener? = null
    private val binding: DialogAdBannerBinding = DialogAdBannerBinding.inflate(LayoutInflater.from(context))

    init {
        dialog = Dialog(context).apply {
            setContentView(binding.root)
            setCancelable(false)

            window?.apply {
                setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
                setBackgroundDrawableResource(android.R.color.transparent)
                addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
                setDimAmount(0.5f)
            }
        }
        with(binding) {
            bannerContainer.layoutParams = bannerContainer.layoutParams.apply {
                width = bannerWidth.toPx()
                height = bannerHeight.toPx()
            }

            ivClose.setOnClickListener {
                onClickListener?.onClose()
            }
        }
    }

    fun addView(view: View) {
        binding.bannerContainer.addView(view)
    }

    fun showDialog() {
        dialog.show()
    }

    fun dismissDialog() {
        dialog.dismiss()
    }

    fun setOnClickListener(onClickListener: OnClickListener) {
        this.onClickListener = onClickListener
    }

    interface OnClickListener {
        fun onClose()
    }
}