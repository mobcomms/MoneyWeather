package com.moneyweather.util

import android.content.Context
import android.widget.Toast

object CustomToast {

    private var toast: Toast? = null

    fun showToast(context: Context, message: String) {
        try {
            toast?.cancel()
            toast = Toast.makeText(context, message, Toast.LENGTH_SHORT)
            toast?.show()
        } catch (e: Exception){
            Logger.e(e.message)
        }
    }

    fun showToast(context: Context, resId: Int) {
        try {
            toast?.cancel()
            toast = Toast.makeText(context, context.getString(resId), Toast.LENGTH_SHORT)
            toast?.show()
        } catch (e: Exception){
            Logger.e(e.message)
        }
    }
}