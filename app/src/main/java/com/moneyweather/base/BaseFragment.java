package com.moneyweather.base;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.moneyweather.util.LogPrint;

import org.json.JSONObject;

public class BaseFragment extends Fragment implements View.OnClickListener {
    public BaseFragment() {
    }

    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    public void startActivity(Intent intent) {
        this.startActivity(intent, true);
    }

    public void startActivity(Class c) {
        Intent i = new Intent(this.getActivity(), c);
        this.startActivity(i, true);
    }

    public void startActivity(JSONObject json, Class c) {
        Intent i = new Intent(this.getActivity(), c);
        i.putExtra("json", json.toString());
        this.startActivity(i, true);
    }

    public void startActivity(String jsonString, Class c) {
        Intent i = new Intent(this.getActivity(), c);
        i.putExtra("json", jsonString);
        this.startActivity(i, true);
    }

    public void startActivity(Intent intent, boolean aniFlag) {
        super.startActivity(intent);
    }

    public void startActivityForResult(Class c, int requestCode) {
        Intent i = new Intent(this.getActivity(), c);
        this.startActivityForResult(i, requestCode);
    }

    public void startActivityForResult(Intent intent, int requestCode) {
        this.startActivityForResult(intent, requestCode, true);
    }

    public void startActivityForResult(Intent intent, int requestCode, boolean aniFlag) {
        super.startActivityForResult(intent, requestCode);
    }

    public void startActivityForResult(String jsonString, Class c, int requestCode) {
        Intent i = new Intent(this.getActivity(), c);
        i.putExtra("json", jsonString);
        i.putExtra("RequestCode", requestCode);
        this.startActivityForResult(i, requestCode);
    }

    protected void startFragment(int containerId, Class<? extends BaseFragment> fragmentClass) {
        BaseFragment fragment = null;

        try {
            fragment = (BaseFragment) fragmentClass.newInstance();
        } catch (Fragment.InstantiationException var5) {
            var5.printStackTrace();
        } catch (java.lang.InstantiationException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }

        if (fragment == null) {
            throw new IllegalStateException("cannot start fragment. " + fragmentClass.getName());
        } else {
            this.getActivity().getSupportFragmentManager().beginTransaction().add(containerId, fragment).addToBackStack((String) null).commit();
        }
    }

    protected void startFragment(int containerId, Class<? extends BaseFragment> fragmentClass, Intent i) {
        BaseFragment fragment = null;

        try {
            fragment = (BaseFragment) fragmentClass.newInstance();
            if (i != null) {
                fragment.setArguments(i.getExtras());
            }
        } catch (Fragment.InstantiationException var6) {
            var6.printStackTrace();
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        } catch (java.lang.InstantiationException e) {
            throw new RuntimeException(e);
        }

        if (fragment == null) {
            throw new IllegalStateException("cannot start fragment. " + fragmentClass.getName());
        } else {
            this.getActivity().getSupportFragmentManager().beginTransaction().add(containerId, fragment).addToBackStack((String) null).commit();
        }
    }

    protected void replaceFragment(int containerId, Class<? extends BaseFragment> fragmentClass) {
        this.replaceFragment(containerId, fragmentClass, (Intent) null);
    }

    protected void replaceFragment(int containerId, Class<? extends BaseFragment> fragmentClass, Intent i) {
        Fragment fragment = this.getActivity().getSupportFragmentManager().findFragmentByTag(fragmentClass.getName());
        if (fragment == null) {
            try {
                fragment = (Fragment) fragmentClass.newInstance();
                if (i != null) {
                    fragment.setArguments(i.getExtras());
                }
            } catch (Fragment.InstantiationException var6) {
                var6.printStackTrace();
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            } catch (java.lang.InstantiationException e) {
                throw new RuntimeException(e);
            }

            FragmentTransaction fragmentTransaction = this.getActivity().getSupportFragmentManager().beginTransaction();
            fragmentTransaction.replace(containerId, fragment, fragmentClass.getName());
            fragmentTransaction.commit();
        } else {
            LogPrint.d(fragment.getTag() + "already loaded");
        }

    }

    protected void removeFragment(int containerId, Class<? extends BaseFragment> fragmentClass) {
        FragmentTransaction ft = this.getFragmentManager().beginTransaction();
        Fragment fragment = this.getActivity().getSupportFragmentManager().findFragmentByTag(fragmentClass.getName());
        if (fragment != null && fragment.isAdded()) {
            ft.remove(fragment);
        }

        ft.commit();
    }

    protected void finishFragment() {
        this.getFragmentManager().popBackStack();
    }

    public void onPressedBackkey() {
        this.finishFragment();
    }

    public void onClick(View view) {
    }

    public void onChildResume() {
    }
}
