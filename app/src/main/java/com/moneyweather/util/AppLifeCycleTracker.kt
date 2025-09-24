package com.moneyweather.util

import android.app.Activity
import android.app.Application
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

class AppLifeCycleTracker(
    val startActivity: (activity: AppCompatActivity, activityStack: LinkedHashSet<String>) -> Unit,
    val destroyActivity: (activity: AppCompatActivity, activityStack: LinkedHashSet<String>) -> Unit
) : Application.ActivityLifecycleCallbacks {

    private val activityStack = LinkedHashSet<String>()

    override fun onActivityCreated(activity: Activity, bundle: Bundle?) {
        addActivity(activity)
    }

    override fun onActivityStarted(activity: Activity) {}

    override fun onActivityResumed(activity: Activity) {}

    override fun onActivityPaused(activity: Activity) {}

    override fun onActivityStopped(activity: Activity) {}

    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}

    override fun onActivityDestroyed(activity: Activity) {
        removeActivity(activity)
    }

    private fun addActivity(activity: Activity) {
        activityStack.add(activity::class.java.simpleName)

        if (activity is AppCompatActivity) {
            startActivity(activity, activityStack)
        }
    }

    private fun removeActivity(activity: Activity) {
        activityStack.remove(activity::class.java.simpleName)

        if (activity is AppCompatActivity) {
            destroyActivity(activity, activityStack)
        }
    }

    companion object {
        val TAG: String = AppLifeCycleTracker::class.java.simpleName
    }
}