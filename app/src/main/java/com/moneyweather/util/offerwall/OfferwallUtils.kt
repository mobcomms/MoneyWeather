package com.moneyweather.util.offerwall

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import com.moneyweather.util.LogPrint
import java.net.URISyntaxException

object OfferwallUtils {
    fun callApp(activity: Activity, url: String): Boolean {
        var intent: Intent? = null

        try {
            intent = Intent.parseUri(url, Intent.URI_INTENT_SCHEME)
            LogPrint.d("intent getScheme +++===> " + intent.scheme)
            LogPrint.d("intent getDataString +++===> " + intent.dataString)
        } catch (ex: URISyntaxException) {
            LogPrint.d("Bad URI " + url + ":" + ex.message)

            return false
        }

        return callAppResult(intent, activity, url)
    }

    private fun callAppResult(intent: Intent, activity: Activity, url: String): Boolean {
        try {
            LogPrint.d("callAppResult url :: $url")

            var retval = false

            if (url.startsWith("intent")) {
                if (activity.packageManager.resolveActivity(intent, 0) == null) {
                    val packagename = intent.getPackage()
                    if (packagename != null) {
                        try {
                            val uri = Uri.parse(intent.dataString)
                            activity.startActivity(Intent(Intent.ACTION_VIEW, uri))
                            return true
                        } catch (e: ActivityNotFoundException) {
                            if (intent == null) return false
                            val packageName = intent.getPackage()
                            if (packageName != null) {
                                activity.startActivity(
                                    Intent(
                                        Intent.ACTION_VIEW,
                                        Uri.parse("market://details?id=$packageName")
                                    )
                                )
                                return true
                            }
                            return false
                        }
                    }
                } else {
                    intent.addCategory(Intent.CATEGORY_BROWSABLE)
                    intent.setComponent(null)
                    try {
                        if (activity.startActivityIfNeeded(intent, -1)) {
                            retval = true
                        }
                    } catch (ex: ActivityNotFoundException) {
                        retval = false
                    }
                }
            } else {
                var isNaverExist = false

                // 설치된 패키지 확인
                val pm = activity.packageManager
                val activityList = pm.queryIntentActivities(intent, 0)
                for (i in activityList.indices) {
                    val app = activityList[i]
                    if (app.activityInfo.name.contains("com.nhn.android.search")) {
                        isNaverExist = true
                        break
                    }
                }

                // 해당 앱이 없을때 마켓으로 연결
                if (url.startsWith("naversearchapp://") && !isNaverExist) {
                    val intent1 = Intent(Intent.ACTION_VIEW)
                    intent1.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    intent1.setData(Uri.parse("market://details?id=" + "com.nhn.android.search"))
                    activity.startActivityForResult(intent1, 0)
                    retval = true
                } else {
                    val uri = Uri.parse(url)
                    val intent1 = Intent(Intent.ACTION_VIEW)
                    intent1.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    intent1.setData(uri)
                    activity.startActivityForResult(intent1, 0)
                    retval = true
                }
            }
            return retval
        } catch (e: ActivityNotFoundException) {
            e.printStackTrace()
            return false
        }
    }
}
