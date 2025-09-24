package com.moneyweather.ui.carouselview.helper

import android.content.res.Resources



object ViewHelper {

    /**
     * @return screen width size
     */
    fun getScreenWidth(): Int {
        return Resources.getSystem().displayMetrics.widthPixels
    }

    /**
     * @return screen height size
     */
    fun getScreenHeight(): Int {
        return Resources.getSystem().displayMetrics.heightPixels
    }

}