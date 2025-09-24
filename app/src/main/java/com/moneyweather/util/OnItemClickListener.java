package com.moneyweather.util;

import android.net.Uri;
import android.os.SystemClock;
import android.view.View;
import android.widget.PopupWindow;

public class OnItemClickListener {

    private static final long MIN_CLICK_INTERVAL = 600;
    private long mLastClickTime;

    public void onClick(View view, int position, Object object) {
    }
    public void onClick(View view, int position, String object) {
    }
    public void onClick(View view, int position) {
    }
    public void onClick(View view, int position, Uri uri) {
    }
    public void onLongClick(View view, int position, Object object) {
    }

    public void onSingleClick(View view, int position, Object object) {
        long currentClickTime = SystemClock.uptimeMillis();
        long elapsedTime = currentClickTime - mLastClickTime;
        mLastClickTime = currentClickTime;
        if (elapsedTime <= MIN_CLICK_INTERVAL) {
            return;
        }
        onClick(view, position, object);
    }

    public void onReplySubMenu(PopupWindow popupWindow, View view, int position, Object object) {}

}
