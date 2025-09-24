package com.moneyweather.util;

import android.annotation.SuppressLint;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import com.moneyweather.base.BaseApplication;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class SoftKeyboard implements View.OnFocusChangeListener {
    private static final int CLEAR_FOCUS = 0;
    private ViewGroup layout;
    private int layoutBottom;
    private InputMethodManager im;
    private int[] coords;
    private boolean isKeyboardShow;
    private SoftKeyboardChangesThread softKeyboardThread;
    private List<EditText> editTextList;
    private View tempView;
    private final Handler mHandler = new Handler() {
        public void handleMessage(Message m) {
            switch (m.what) {
                case 0:
                    if (SoftKeyboard.this.tempView != null) {
                        SoftKeyboard.this.tempView.clearFocus();
                        SoftKeyboard.this.tempView = null;
                    }
                default:
            }
        }
    };

    @SuppressLint("WrongConstant")
    public static void hide(View v) {
         InputMethodManager inputMethodManager = (InputMethodManager) BaseApplication.Companion.appContext().getSystemService("input_method");
        inputMethodManager.hideSoftInputFromWindow(v.getWindowToken(), 0);
    }

    @SuppressLint("WrongConstant")
    public static void show(View v) {
        @SuppressLint("WrongConstant") InputMethodManager inputMethodManager = (InputMethodManager)BaseApplication.Companion.appContext().getSystemService("input_method");
        inputMethodManager.showSoftInput(v, 0);
    }

    public SoftKeyboard(ViewGroup layout, InputMethodManager im) {
        this.layout = layout;
        this.keyboardHideByDefault();
        this.initEditTexts(layout);
        this.im = im;
        this.coords = new int[2];
        this.isKeyboardShow = false;
        this.softKeyboardThread = new SoftKeyboardChangesThread();
        this.softKeyboardThread.start();
    }

    public void openSoftKeyboard() {
        if (!this.isKeyboardShow) {
            this.layoutBottom = this.getLayoutCoordinates();
            this.im.toggleSoftInput(0, 1);
            this.softKeyboardThread.keyboardOpened();
            this.isKeyboardShow = true;
        }

    }

    public void closeSoftKeyboard() {
        if (this.isKeyboardShow) {
            this.im.toggleSoftInput(1, 0);
            this.isKeyboardShow = false;
        }

    }

    public void setSoftKeyboardCallback(SoftKeyboardChanged mCallback) {
        this.softKeyboardThread.setCallback(mCallback);
    }

    public void unRegisterSoftKeyboardCallback() {
        this.softKeyboardThread.stopThread();
    }

    private int getLayoutCoordinates() {
        this.layout.getLocationOnScreen(this.coords);
        return this.coords[1] + this.layout.getHeight();
    }

    private void keyboardHideByDefault() {
        this.layout.setFocusable(true);
        this.layout.setFocusableInTouchMode(true);
    }

    private void initEditTexts(ViewGroup viewgroup) {
        if (this.editTextList == null) {
            this.editTextList = new ArrayList();
        }

        int childCount = viewgroup.getChildCount();

        for(int i = 0; i <= childCount - 1; ++i) {
            View v = viewgroup.getChildAt(i);
            if (v instanceof ViewGroup) {
                this.initEditTexts((ViewGroup)v);
            }

            if (v instanceof EditText) {
                EditText editText = (EditText)v;
                editText.setOnFocusChangeListener(this);
                editText.setCursorVisible(true);
                this.editTextList.add(editText);
            }
        }

    }

    public void onFocusChange(View v, boolean hasFocus) {
        if (hasFocus) {
            this.tempView = v;
            if (!this.isKeyboardShow) {
                this.layoutBottom = this.getLayoutCoordinates();
                this.softKeyboardThread.keyboardOpened();
                this.isKeyboardShow = true;
            }
        }

    }

    private class SoftKeyboardChangesThread extends Thread {
        private AtomicBoolean started = new AtomicBoolean(true);
        private SoftKeyboardChanged mCallback;

        public SoftKeyboardChangesThread() {
        }

        public void setCallback(SoftKeyboardChanged mCallback) {
            this.mCallback = mCallback;
        }

        public void run() {
            while(this.started.get()) {
                synchronized(this) {
                    try {
                        this.wait();
                    } catch (InterruptedException var7) {
                        var7.printStackTrace();
                    }
                }

                int currentBottomLocation;
                for(currentBottomLocation = SoftKeyboard.this.getLayoutCoordinates(); currentBottomLocation == SoftKeyboard.this.layoutBottom && this.started.get(); currentBottomLocation = SoftKeyboard.this.getLayoutCoordinates()) {
                }

                if (this.started.get()) {
                    this.mCallback.onSoftKeyboardShow();
                }

                while(currentBottomLocation >= SoftKeyboard.this.layoutBottom && this.started.get()) {
                    currentBottomLocation = SoftKeyboard.this.getLayoutCoordinates();
                }

                for(; currentBottomLocation != SoftKeyboard.this.layoutBottom && this.started.get(); currentBottomLocation = SoftKeyboard.this.getLayoutCoordinates()) {
                    synchronized(this) {
                        try {
                            this.wait(500L);
                        } catch (InterruptedException var5) {
                            var5.printStackTrace();
                        }
                    }
                }

                if (this.started.get()) {
                    this.mCallback.onSoftKeyboardHide();
                }

                if (SoftKeyboard.this.isKeyboardShow && this.started.get()) {
                    SoftKeyboard.this.isKeyboardShow = false;
                }

                if (this.started.get()) {
                    SoftKeyboard.this.mHandler.obtainMessage(0).sendToTarget();
                }
            }

        }

        public void keyboardOpened() {
            synchronized(this) {
                this.notify();
            }
        }

        public void stopThread() {
            synchronized(this) {
                this.started.set(false);
                this.notify();
            }
        }
    }

    public interface SoftKeyboardChanged {
        void onSoftKeyboardHide();

        void onSoftKeyboardShow();
    }
}

