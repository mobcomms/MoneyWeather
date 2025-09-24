package com.moneyweather.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.moneyweather.view.fragment.HourlyWeatherFragment
import com.moneyweather.view.fragment.WeeklyWeatherFragment

class FragmentAdapter(fm: FragmentManager, lifecycle: Lifecycle) : FragmentStateAdapter(fm, lifecycle) {

    override fun getItemCount(): Int {
        return 2
    }

    override fun createFragment(position: Int): Fragment {
        val fragment = when (position) {
            0 -> HourlyWeatherFragment()
            1 -> WeeklyWeatherFragment()
            else -> HourlyWeatherFragment()
        }
        return fragment
    }

}