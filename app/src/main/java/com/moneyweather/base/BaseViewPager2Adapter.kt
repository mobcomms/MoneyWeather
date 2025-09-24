package com.moneyweather.base

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter

class BaseViewPager2Adapter(val fragmentManager: FragmentManager, lifecycle: Lifecycle) :
    FragmentStateAdapter(fragmentManager, lifecycle) {
    val TAG = this::class.java.simpleName

    companion object {
        val VIEW_PAGER_POSITION: String = "BaseViewPager2Adapter.VIEW_PAGER_POSITION"
    }

    private var singleFragment: BaseFragment? = null
    var fragmentClass: Class<out BaseFragment>? = null
    var fragments: ArrayList<BaseFragment>? = null
    var titles: ArrayList<String>? = null

    constructor(
        fragment: Fragment,
        fragments: ArrayList<BaseFragment>
    ) : this(fragment.childFragmentManager, fragment.lifecycle) {
        this.fragments = fragments
    }

    constructor(
        fragment: Fragment,
        fragments: ArrayList<BaseFragment>,
        titles: ArrayList<String>
    ) : this(fragment.childFragmentManager, fragment.lifecycle) {
        this.fragments = fragments
        this.titles = titles
    }

    constructor(
        fragment: Fragment,
        fragments: ArrayList<BaseFragment>,
        titles: List<String>
    ) : this(fragment.childFragmentManager, fragment.lifecycle) {
        this.fragments = fragments
        this.titles = ArrayList(titles)
    }


    constructor(
        fragment: Fragment,
        fragmentClass: Class<out BaseFragment>,
        titles: ArrayList<*>
    ) : this(fragment.childFragmentManager, fragment.lifecycle) {
        this.fragmentClass = fragmentClass
        if (titles[0] is String)
            this.titles = titles as ArrayList<String>

    }


    constructor(
        activity: AppCompatActivity,
        fragmentClass: Class<out BaseFragment>,
        titles: ArrayList<*>
    ) : this(activity.supportFragmentManager, activity.lifecycle) {
        this.fragmentClass = fragmentClass
        if (titles[0] is String)
            this.titles = titles as ArrayList<String>
    }


    constructor(
        activity: AppCompatActivity,
        fragments: ArrayList<BaseFragment>,
        titles: ArrayList<String>
    ) : this(activity.supportFragmentManager, activity.lifecycle) {
        this.fragments = fragments
        this.titles = titles
    }

    constructor(
        activity: AppCompatActivity,
        fragments: ArrayList<BaseFragment>,
        titles: List<String>
    ) : this(activity.supportFragmentManager, activity.lifecycle) {
        this.fragments = fragments
        this.titles = ArrayList(titles)
    }

    fun getTitle(position: Int): String {
        return when {
            titles != null -> titles!![position]
            else -> ""
        }
    }

    override fun getItemCount(): Int {
        if (titles != null)
            return titles!!.size
        else if (fragments != null)
            return fragments!!.size
        throw (NullPointerException("objects is null"))
    }

    override fun createFragment(position: Int): Fragment {
        if (fragmentClass != null) {
            singleFragment = fragmentClass!!.newInstance()
            singleFragment!!.arguments = Bundle().apply {
            }
            return singleFragment!!
        } else if (fragments?.size ?: 0 > 0) {
            return fragments!![position]
        }
        throw (NullPointerException("Fragment is null"))
    }


}