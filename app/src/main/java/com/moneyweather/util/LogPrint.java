package com.moneyweather.util;

import android.util.Log;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.json.JSONObject;

public class LogPrint {
    public static String TAG = "";
    public static boolean isDebugMode = true;

    public LogPrint() {
    }

    public static void init(boolean debug, String tag) {
        isDebugMode = debug;
        TAG = tag;
    }

    public static void logLargeString(String tag, String str) {
        if (isDebugMode) {
            if (str.length() > 3000) {
                Log.i(tag, str.substring(0, 3000));
                logLargeString(tag, str.substring(3000));
            } else {
                Log.i(tag, str);
            }
        }

    }

    public static void d(Class c, String msgKey, int msgValue) {
        if (isDebugMode) {
            Log.d(c.getSimpleName(), "     ###" + msgKey + " >>> " + msgValue);
        }

    }

    public static void d(Class c, String msgKey, String msgValue) {
        if (isDebugMode) {
            Log.d(c.getSimpleName(), "     ###" + msgKey + " >>> " + msgValue);
        }

    }

    public static void d(String msgKey, String msgValue) {
        if (isDebugMode) {
            Log.d(TAG, "     ###" + msgKey + " >>> " + msgValue);
        }

    }

    public static void d(String tag, String msgKey, boolean msgValue) {
        d(tag, msgKey, String.valueOf(msgValue));
    }

    public static void d(String tag, String msgKey, int msgValue) {
        d(tag, msgKey, String.valueOf(msgValue));
    }

    public static void d(String tag, String msgKey, float msgValue) {
        d(tag, msgKey, String.valueOf(msgValue));
    }

    public static void d(String tag, String msgKey, long msgValue) {
        d(tag, msgKey, String.valueOf(msgValue));
    }

    public static void d(String tag, String msgKey, double msgValue) {
        d(tag, msgKey, String.valueOf(msgValue));
    }

    public static void d(String tag, String msgKey, String msgValue) {
        if (isDebugMode) {
            Log.d(tag, "     ###" + msgKey + " >>> " + msgValue);
        }

    }

    public static void d(String msgKey, int msgValue) {
        d(msgKey, String.valueOf(msgValue));
    }

    public static void d(String msgKey, float msgValue) {
        d(msgKey, String.valueOf(msgValue));
    }

    public static void d(String msgKey, double msgValue) {
        d(msgKey, String.valueOf(msgValue));
    }

    public static void d(String msgKey, long msgValue) {
        d(msgKey, String.valueOf(msgValue));
    }

    public static void d(String msgKey, boolean msgValue) {
        d(msgKey, String.valueOf(msgValue));
    }

    public static void d(String msg) {
        if (isDebugMode) {
            Log.d(TAG, "     ###" + msg);
        }

    }

    public static void d(Class c, String msg) {
        if (isDebugMode) {
            Log.d("     ###" + c.getName(), msg);
        }

    }

    public static void d(JSONObject json) {
        Gson gson = (new GsonBuilder()).setPrettyPrinting().create();
        d(gson.toJson(json));
    }

    public static void d(String key, JSONObject json) {
        Gson gson = (new GsonBuilder()).setPrettyPrinting().create();
        d(key, gson.toJson(json));
    }

    public static void d(int msg) {
        d(String.valueOf(msg));
    }

    public static void d(boolean msg) {
        d(String.valueOf(msg));
    }

    public static void d(float msg) {
        d(String.valueOf(msg));
    }

    public static void d(double msg) {
        d(String.valueOf(msg));
    }

    public static void i(String msg) {
        if (isDebugMode) {
            Log.i(TAG, "     ###" + msg);
        }

    }

    public static void i(String tag, String msg) {
        if (isDebugMode) {
            Log.i(tag, "     ###" + msg);
        }

    }

    public static void i(String tag, String msgKey, int msgValue) {
        i(tag, msgKey, String.valueOf(msgValue));
    }

    public static void i(String tag, String msgKey, String msgValue) {
        if (isDebugMode) {
            Log.i(tag, "     ###" + msgKey + " >>> " + msgValue);
        }

    }

    public static void i(int msg) {
        i(String.valueOf(msg));
    }

    public static void i(boolean msg) {
        i(String.valueOf(msg));
    }

    public static void i(float msg) {
        i(String.valueOf(msg));
    }

    public static void i(double msg) {
        i(String.valueOf(msg));
    }

    public static void i(String key, JSONObject json) {
        Gson gson = (new GsonBuilder()).setPrettyPrinting().create();
        i(key, gson.toJson(json));
    }

    public static void e(String msg) {
        if (isDebugMode) {
            Log.e(TAG, "     ###" + msg);
        }

    }

    public static void e(int msg) {
        e(String.valueOf(msg));
    }

    public static void e(boolean msg) {
        e(String.valueOf(msg));
    }

    public static void e(float msg) {
        e(String.valueOf(msg));
    }

    public static void e(double msg) {
        e(String.valueOf(msg));
    }

    public static void e(String tag, String msgKey, boolean msgValue) {
        e(tag, msgKey, String.valueOf(msgValue));
    }

    public static void e(String tag, String msgKey, int msgValue) {
        e(tag, msgKey, String.valueOf(msgValue));
    }

    public static void e(String tag, String msgKey, float msgValue) {
        e(tag, msgKey, String.valueOf(msgValue));
    }

    public static void e(String tag, String msgKey, long msgValue) {
        e(tag, msgKey, String.valueOf(msgValue));
    }

    public static void e(String tag, String msgKey, double msgValue) {
        e(tag, msgKey, String.valueOf(msgValue));
    }

    public static void e(String tag, String msgKey, String msgValue) {
        if (isDebugMode) {
            Log.e(tag, "     ###" + msgKey + " >>> " + msgValue);
        }

    }

    public static void w(String msg) {
        w(String.valueOf(msg));
    }

    public static void w(int msg) {
        w(String.valueOf(msg));
    }

    public static void w(boolean msg) {
        w(String.valueOf(msg));
    }

    public static void w(float msg) {
        w(String.valueOf(msg));
    }

    public static void w(double msg) {
        w(String.valueOf(msg));
    }

    public static void t(Throwable t) {
        if (isDebugMode) {
            Log.wtf(TAG, t);
        }

    }

    public static void t(String msgKey, Throwable t) {
        if (isDebugMode) {
            Log.wtf(TAG, "      ###" + msgKey + " >>> " + t);
        }

    }

    public static void line() {
        if (isDebugMode) {
            Log.d(TAG, "###  --------------------------------------------");
        }

    }

    public static void lineTitleStart(String title) {
        if (isDebugMode) {
            Log.d(TAG, "###  -------------------- [START " + title + " ]------------------------");
        }

    }

    public static void lineTitleEnd(String title) {
        if (isDebugMode) {
            Log.d(TAG, "###  -------------------- [END " + title + " ]------------------------");
        }

    }

    public static void saveLog(String logStr) {
        Log.d(TAG, "      ### > " + logStr);
    }

    public static void saveLog(String logName, String logValue) {
        Log.d(TAG, "      ### > " + logName + " : " + logValue);
    }

    public static void e(String logStr, Throwable t) {
        if (isDebugMode) {
            Log.e(TAG, "### ---------------------------------------------------------------------------------------------");
            Log.e(TAG, logStr, t);
            Log.e(TAG, "### ---------------------------------------------------------------------------------------------");
        }

    }

    public static void e(String logName, long logStr) {
        e(logName, String.valueOf(logStr));
    }

    public static void e(String logName, Object logStr) {
        e(logName, String.valueOf(logStr));
    }

    public static void e(String logName, String logStr) {
        if (isDebugMode) {
            Log.e(TAG, "### ---------------------------------------------------------------------------------------------");
            Log.e(TAG, "### > " + logName + " : " + logStr);
            Log.e(TAG, "### ---------------------------------------------------------------------------------------------");
        }

    }
}
