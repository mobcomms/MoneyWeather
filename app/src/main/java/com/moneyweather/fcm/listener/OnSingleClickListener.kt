package com.moneyweather.fcm.listener

import android.os.SystemClock
import android.view.View

class OnSingleClickListener(private val clickListener: View.OnClickListener, private val interval: Long = 350) :
    View.OnClickListener {

    private var lastClickTime = 0L

    override fun onClick(view: View?) {
        val currentTime = SystemClock.elapsedRealtime()
        if (currentTime - lastClickTime < interval) {
            // 클릭 무시 (인터벌 내의 중복 클릭 방지)
            return
        }
        lastClickTime = currentTime

        view?.let {
            clickListener.onClick(it)
        }
    }
}

fun View.setOnSingleClickListener(interval: Long = 350, action: (view: View) -> Unit) {
    val listener = View.OnClickListener { action(it) }
    setOnClickListener(OnSingleClickListener(listener, interval))
}