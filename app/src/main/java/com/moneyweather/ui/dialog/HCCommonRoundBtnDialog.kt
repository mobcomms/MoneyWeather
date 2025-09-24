package com.moneyweather.ui.dialog

import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import com.moneyweather.R
import com.moneyweather.databinding.DialogHcCommonRoundBtnBinding
import com.moneyweather.model.enums.DialogType

/**
 * COMMON DIALOG
 */
class HCCommonRoundBtnDialog : Dialog {
    var mContext: Context

    private lateinit var mBinding: DialogHcCommonRoundBtnBinding
    private var onDismissListener: OnDismissListener? = null

    constructor(context: Context) : super(context) {
        mContext = context
        window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        mBinding = DataBindingUtil.inflate(LayoutInflater.from(context), R.layout.dialog_hc_common_round_btn,null,false)
        initEvent()
        setContentView(mBinding.root)
    }

    constructor(context: Context, themeResId: Int) : super(context, themeResId) {
        mContext = context
    }

    protected constructor(context: Context, cancelable: Boolean, cancelListener: DialogInterface.OnCancelListener?) : super(context, cancelable, cancelListener) {
        mContext = context
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    public override fun onStart() {
        val metrics = context.resources.displayMetrics
        val width = (metrics.widthPixels * 0.8).toInt()
        window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        window!!.setLayout(width, ViewGroup.LayoutParams.WRAP_CONTENT)
        super.onStart()
    }

    /**
     * 다이얼로그 제목 설정
     *
     * @param title 제목 String
     */
    fun setDialogTitle(title: String?): HCCommonRoundBtnDialog {
        mBinding.txtTitle.visibility = View.VISIBLE
        mBinding.txtTitle.text = title
        return this
    }

    /**
     * 다이얼로그 제목 설정
     *
     * @param strResId 제목 Resource ID
     */
    fun setDialogTitle(strResId: Int): HCCommonRoundBtnDialog {
        mBinding.txtTitle.visibility = View.VISIBLE
        mBinding.txtTitle.setText(strResId)
        return this
    }

    /**
     * 다이얼로그 내용 설정
     *
     * @param content 내용 String
     */
    fun setContent(content: String?): HCCommonRoundBtnDialog {
        mBinding.txtContent.text = content
        return this
    }

    fun setContent(content: String?, point: String): HCCommonRoundBtnDialog {
        mBinding.txtContent.text = content
       // mBinding.txtContent.changePartialColor(point, Color.parseColor("#eb3a3a"))
        return this
    }

    /**
     * 다이얼로그 내용 설정
     *
     * @param strResId 내용 Resource ID
     */
    fun setContent(strResId: Int): HCCommonRoundBtnDialog {
        mBinding.txtContent.setText(strResId)
        return this
    }

    /**
     * 다이얼로그 확인 버튼 설정
     *
     * @param content 확인 버튼 String (default : '확인')
     */
    fun setConfirmButtonText(content: String?): HCCommonRoundBtnDialog {
        mBinding.btnConfirm.text = content
        return this
    }

    /**
     * 다이얼로그 확인 버튼 설정
     *
     * @param strResId 확인 버튼 Resource ID (default : '확인')
     */
    fun setConfirmButtonText(strResId: Int): HCCommonRoundBtnDialog {
        mBinding.btnConfirm.setText(strResId)
        return this
    }

    /**
     * 다이얼로그 버튼 클릭 시 리스너
     */
    fun setOnDismissListener(onDismissListener: OnDismissListener): HCCommonRoundBtnDialog {
        this.onDismissListener = onDismissListener
        return this
    }

    private fun initEvent() {
        mBinding.run {
            imgClose.setOnClickListener { dismiss() }
            btnConfirm.setOnClickListener {
                onDismissListener?.onDismiss(DialogType.BUTTON_CONFIRM.ordinal)
                dismiss()
            }
        }
    }

    interface OnDismissListener {
        fun onDismiss(menuId: Int)
    }

    companion object {
        val TAG = HCCommonRoundBtnDialog::class.java.simpleName
    }
}