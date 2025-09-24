package com.moneyweather.adapter

import android.content.Context
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.moneyweather.view.fragment.MainHomeFragment
import com.moneyweather.view.fragment.MyFragment
import com.moneyweather.view.fragment.StoreFragment

class MainBottomAdapter(
    val context: Context,
    private val fragmentManger: FragmentManager,
    lifecycle: Lifecycle
) : FragmentStateAdapter(fragmentManger, lifecycle) {
    override fun getItemCount() = ITEM_COUNT

    override fun createFragment(position: Int): Fragment {
        return getFragment(position)
    }

    fun getItem(position: Int): Fragment {
        return getFragment(position)
    }

    private fun getFragment(position: Int): Fragment {
        val fragmentName = when (position) {
            POS_PAGE_HOME -> MainHomeFragment::class.java.name
            //POS_PAGE_CHARGE -> ChargeFragment::class.java.name
            POS_PAGE_STORE -> StoreFragment::class.java.name
            POS_PAGE_SETTING -> MyFragment::class.java.name
            else -> MainHomeFragment::class.java.name
        }

        return fragmentManger.fragmentFactory.instantiate(context.classLoader, fragmentName)
    }

    companion object {
        const val ITEM_COUNT = 3

        const val POS_PAGE_HOME = 0

        // const val POS_PAGE_CHARGE = 1
        const val POS_PAGE_STORE = 1
        const val POS_PAGE_SETTING = 2

    }
}