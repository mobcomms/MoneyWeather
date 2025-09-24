package com.moneyweather.util.fagmentFactory

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentFactory

class MainFragmentFactory : FragmentFactory() {
    override fun instantiate(classLoader: ClassLoader, className: String): Fragment {
        return super.instantiate(classLoader, className)
    }
}