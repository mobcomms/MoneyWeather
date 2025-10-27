package com.moneyweather.ui.dialog

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import com.moneyweather.R
import com.moneyweather.databinding.DialogAppEndBinding
import androidx.core.graphics.drawable.toDrawable
import com.enliple.banner.MobSDK
import com.enliple.banner.common.MobConstant
import com.enliple.banner.daro.AdUnit

class AppEndDialog(context: Context,
                   themeResId: Int = R.style.Theme_CommonDialog,
                   var finishButtonUnit: ()->Unit
) : Dialog(context, themeResId) {

    private val binder = DialogAppEndBinding.inflate(layoutInflater)

    init {
        window?.apply {
            setBackgroundDrawable(Color.TRANSPARENT.toDrawable())
            setDimAmount(0.5f)
        }
        setContentView(binder.root)

        initView()
    }

    private fun initView() {

//        MobSDK.loadDaroView(context, MobConstant.DARO_AD_TYPE_MREC, MobConstant.DONSEE_INAPP_MAIN_SPLASH, binder.daroAdContainer, null)

        var daroView = MobSDK.getDaroView(
            context,
            MobConstant.DARO_AD_TYPE_MREC,
            AdUnit.DONSEE_APP_END_POPUP_BANNER,
            null)
        binder.daroAdContainer.removeAllViews()
        binder.daroAdContainer.addView(daroView)

        binder.btnFinish.setOnClickListener {
            finishButtonUnit()
        }
        binder.btnCancel.setOnClickListener {
            dismiss()
        }
    }

}