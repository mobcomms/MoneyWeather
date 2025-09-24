package com.moneyweather.base;

import android.content.Intent;
import android.content.res.Configuration;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;

import androidx.annotation.LayoutRes;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.moneyweather.util.LogPrint;

import org.json.JSONObject;

public class BaseActivity extends AppCompatActivity {
    protected static final Interpolator INTERPOLATOR = new DecelerateInterpolator();

    public BaseActivity() {
    }

    public void setContentView(View view) {
        super.setContentView(view);
    }

    public void setContentView(@LayoutRes int layoutResID) {
        super.setContentView(layoutResID);
    }

    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (newConfig.orientation == 1) {
            LogPrint.d("세로화면 전환");
            this.onConfigurationChanged(true);
        } else {
            LogPrint.d("가로화면 전환");
            this.onConfigurationChanged(false);
        }

    }

    public void onConfigurationChanged(boolean portrait) {
    }

    protected void onDestroy() {
        super.onDestroy();


    }

    public void startActivity(Class c) {
        Intent i = new Intent(this, c);
        this.startActivity(i);
    }

    public void startActivity(Class c, boolean aniFlag) {
        Intent i = new Intent(this, c);
        if (aniFlag) {
            this.startActivity(i);
        } else {
            super.startActivity(i);
        }

    }

    public void startActivity(Intent intent) {
        this.startActivity(intent, true);
    }

    public void startActivity(Intent intent, boolean aniFlag) {
        super.startActivity(intent);
    }

    public void startActivity(JSONObject json, Class c) {
        Intent i = new Intent(this, c);
        i.putExtra("json", json.toString());
        this.startActivity(i, true);
    }

    public void startActivity(String jsonString, Class c) {
        Intent i = new Intent(this, c);
        i.putExtra("json", jsonString);
        this.startActivity(i, true);
    }

    public void startActivityForResult(Class c, int requestCode) {
        Intent i = new Intent(this, c);
        i.putExtra("RequestCode", requestCode);
        this.startActivityForResult(i, requestCode);
    }

    public void startActivityForResult(String jsonString, Class c, int requestCode) {
        Intent i = new Intent(this, c);
        i.putExtra("json", jsonString);
        i.putExtra("RequestCode", requestCode);
        this.startActivityForResult(i, requestCode);
    }

    public void startActivityForResult(Intent intent, int requestCode) {
        this.startActivityForResult(intent, requestCode, true);
    }

    public void startActivityForResult(Intent intent, int requestCode, boolean aniFlag) {
        super.startActivityForResult(intent, requestCode);
    }

    public void finish() {
        super.finish();
    }

    public void finish(boolean aniFlag) {
        super.finish();
    }

    public void onBackPressed() {
        super.onBackPressed();
    }

    protected void onResume() {
        super.onResume();
       // SpManager.setBoolean("appRunState", true);
    }

    protected void onPause() {
        super.onPause();
       // SpManager.setBoolean("appRunState", false);
    }

    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    protected int getDisplayWidth() {
        DisplayMetrics dm = this.getResources().getDisplayMetrics();
        return dm.widthPixels;
    }

    protected int getDisplayHeight() {
        DisplayMetrics dm = this.getResources().getDisplayMetrics();
        return dm.heightPixels;
    }

    protected float getDensityDpi() {
        DisplayMetrics dm = this.getResources().getDisplayMetrics();
        return (float)dm.densityDpi;
    }

    protected float dpToPx(float dp) {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, this.getResources().getDisplayMetrics());
    }

    protected float pxToDp(float px) {
        float density = this.getResources().getDisplayMetrics().density;
        if ((double)density == 1.0) {
            density = (float)((double)density * 4.0);
        } else if ((double)density == 1.5) {
            density *= 2.0F;
        } else if ((double)density == 2.0) {
            density = (float)((double)density * 2.0);
        }

        return px / density;
    }

    protected int getMeasureWidth(View view) {
        view.measure(0, 0);
        return view.getMeasuredWidth();
    }

    protected int getMeasureHeight(View view) {
        view.measure(0, 0);
        return view.getMeasuredHeight();
    }

    protected void startFragment(int containerId, Class<? extends BaseFragment> fragmentClass) {
        BaseFragment fragment = null;

        try {
            fragment = (BaseFragment)fragmentClass.newInstance();
        } catch (Fragment.InstantiationException var5) {
            var5.printStackTrace();
        } catch (IllegalAccessException var6) {
            var6.printStackTrace();
        } catch (InstantiationException var7) {
            var7.printStackTrace();
        }

        if (fragment == null) {
            throw new IllegalStateException("cannot start fragment. " + fragmentClass.getName());
        } else {
            this.getSupportFragmentManager().beginTransaction().add(containerId, fragment).addToBackStack((String)null).commit();
        }
    }

    protected void startFragment(int containerId, Class<? extends BaseFragment> fragmentClass, Intent i) {
        BaseFragment fragment = null;

        try {
            fragment = (BaseFragment)fragmentClass.newInstance();
            if (i != null) {
                fragment.setArguments(i.getExtras());
            }
        } catch (Fragment.InstantiationException var6) {
            var6.printStackTrace();
        } catch (IllegalAccessException var7) {
            var7.printStackTrace();
        } catch (InstantiationException var8) {
            var8.printStackTrace();
        }

        if (fragment == null) {
            throw new IllegalStateException("cannot start fragment. " + fragmentClass.getName());
        } else {
            this.getSupportFragmentManager().beginTransaction().add(containerId, fragment).addToBackStack((String)null).commit();
        }
    }

    protected void replaceFragment(int containerId, Class<? extends BaseFragment> fragmentClass) {
        this.replaceFragment(containerId, fragmentClass, (Intent)null);
    }

    protected void replaceFragment(int containerId, Class<? extends BaseFragment> fragmentClass, Intent i) {
        Fragment fragment = this.getSupportFragmentManager().findFragmentByTag(fragmentClass.getName());
        if (fragment == null) {
            try {
                fragment = (Fragment)fragmentClass.newInstance();
                if (i != null) {
                    fragment.setArguments(i.getExtras());
                }
            } catch (Fragment.InstantiationException var6) {
                var6.printStackTrace();
            } catch (IllegalAccessException var7) {
                var7.printStackTrace();
            } catch (InstantiationException var8) {
                var8.printStackTrace();
            }

            FragmentTransaction fragmentTransaction = this.getSupportFragmentManager().beginTransaction();
            fragmentTransaction.setCustomAnimations(0, 0, 0, 0);
            fragmentTransaction.replace(containerId, fragment, fragmentClass.getName());
            fragmentTransaction.commit();
        } else {
            if (i != null) {
                fragment.setArguments(i.getExtras());
                ((BaseFragment)fragment).onChildResume();
            }

            LogPrint.d(fragment.getTag() + "already loaded");
        }

    }

    protected void finishFragment() {
        this.getFragmentManager().popBackStack();
    }
}

