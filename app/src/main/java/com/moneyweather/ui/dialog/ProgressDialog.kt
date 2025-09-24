package com.moneyweather.ui.dialog

import android.animation.ObjectAnimator
import android.animation.PropertyValuesHolder
import android.animation.ValueAnimator
import android.app.Dialog
import android.content.Context
import android.text.SpannableStringBuilder
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import com.moneyweather.R

class ProgressDialog(context: Context) {

    val dialog: Dialog
    private var onClickListener: OnClickListener? = null
    private var btnClose: CardView? = null
    private var tvTitle: TextView

    init {
        val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_progress, null)

        val dots = listOf(
            dialogView.findViewById<View>(R.id.dot1),
            dialogView.findViewById<View>(R.id.dot2),
            dialogView.findViewById<View>(R.id.dot3),
            dialogView.findViewById<View>(R.id.dot4)
        )

        dots.forEachIndexed { index, dot ->
            val scaleUp = ObjectAnimator.ofPropertyValuesHolder(
                dot,
                PropertyValuesHolder.ofFloat("scaleX", 1f, 0.6f),
                PropertyValuesHolder.ofFloat("scaleY", 1f, 0.6f)
            ).apply {
                duration = 500
                repeatCount = ValueAnimator.INFINITE
                repeatMode = ValueAnimator.REVERSE
                startDelay = index * 200L
                start()
            }
        }

        dialog = Dialog(context)
        dialog.setContentView(dialogView)
        dialog.setCancelable(false)
        dialog.window?.apply {
            setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
            setBackgroundDrawableResource(android.R.color.transparent)
        }

        tvTitle = dialogView.findViewById(R.id.tvTitle)
        btnClose = dialogView.findViewById(R.id.btnClose)

        btnClose?.setOnClickListener {
            onClickListener?.onClose()
        }
    }

    fun setText(text: String) {
        tvTitle.text = text
    }

    fun setText(text: SpannableStringBuilder) {
        tvTitle.text = text
    }

    fun showDialog() {
        dialog.show()
    }

    fun dismissDialog() {
        dialog.dismiss()
    }

    fun setOnClickListener(onClickListener: OnClickListener): ProgressDialog {
        this.onClickListener = onClickListener
        return this
    }

    interface OnClickListener {
        fun onClose()
    }
}