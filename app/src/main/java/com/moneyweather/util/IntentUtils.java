package com.moneyweather.util;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;

public class IntentUtils {

    public static Boolean isIntent(String url) {
        return url.startsWith("intent:");
    }

    public static Boolean isMarket(String url) {
        return url.matches("^market://\\S+$");
    }


    public static void gotoMarket(Context context, Intent intent) {
        context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + intent.getPackage())));
    }

    public static boolean isInnerIntent(Context context, Intent intent) {
        try {
            if(intent != null && intent.getPackage() != null && context.getPackageManager().getPackageInfo(intent.getPackage(), PackageManager.GET_ACTIVITIES) != null) {
                return true;
            }
            else {
                return false;
            }
        }
        catch (Exception e) {
            Logger.d("intent package : " + intent.getPackage());
            e.printStackTrace();
            return false;
        }
    }

    public static boolean isExistPackage(Context context, Intent intent) {
        try {
            return intent != null && intent.getPackage() != null && context.getPackageManager().getLaunchIntentForPackage(intent.getPackage()) != null;
        } catch(Exception e) {
            Logger.d("intent package : " + intent.getPackage());
            e.printStackTrace();
            return false;
        }
    }

    public static Intent parse(String url) {
        try {
            return Intent.parseUri(url, Intent.URI_INTENT_SCHEME);
        }catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static void openBrowser(Context context, String url) {

    }

}
