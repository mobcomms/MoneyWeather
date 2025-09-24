package com.moneyweather.bindingAdapter

import android.app.Activity
import android.os.Build
import android.os.Bundle
import android.text.Html
import android.text.Spannable
import android.text.SpannableString
import android.text.SpannableStringBuilder
import android.text.style.ForegroundColorSpan
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.ColorInt
import androidx.core.content.ContextCompat
import androidx.databinding.BindingAdapter
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.google.firebase.analytics.FirebaseAnalytics
import com.moneyweather.R
import com.moneyweather.adapter.HourlyWeatherAdapter
import com.moneyweather.base.BaseViewPager2Adapter
import com.moneyweather.model.Weather
import com.moneyweather.model.enums.DustEnum
import com.moneyweather.util.CommonUtils
import com.moneyweather.util.FirebaseAnalyticsManager
import timber.log.Timber

object BindingAdapters {
    /**
     * 서버 통신 후 데이터 처리를 해야하는 부분도 있어 전역으로 변경
     */
    @JvmStatic
    fun viewPager2SetupTabLayout(tabLayout: TabLayout?, viewPager: ViewPager2?) {
        try {
            if (tabLayout != null && viewPager != null) {
                if (viewPager.adapter == null || viewPager.adapter !is BaseViewPager2Adapter) {
                    Timber.d("xxxxx adapater가 null이거나 BaseViewPager2Adapter가 아니므로 리턴 처리함.")
                    return
                }

                var adapter: BaseViewPager2Adapter = viewPager.adapter as BaseViewPager2Adapter
                TabLayoutMediator(tabLayout, viewPager) { tab, position ->
                    tab.text = adapter.getTitle(position).trim()

                }.attach()

                var tabSelectedListener = object : TabLayout.OnTabSelectedListener {
                    override fun onTabReselected(tab: TabLayout.Tab?) {
                    }

                    override fun onTabUnselected(tab: TabLayout.Tab?) {
                    }

                    override fun onTabSelected(tab: TabLayout.Tab?) {
                        Timber.d("tab position : ${tab?.position}")
                        tab?.position?.let {
                            var tabName = "N페이샵"
                            when (it) {
                                0 -> tabName = "N페이샵"
                                1 -> tabName = "쿠폰샵"
                            }

                            FirebaseAnalyticsManager.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW, Bundle().apply {
                                putString(FirebaseAnalyticsManager.VIEW_NAME, "상점")
                                putString(FirebaseAnalyticsManager.TAB_NAME, tabName)
                            })

                            (viewPager.context as? Activity)?.runOnUiThread {
                                viewPager.currentItem = it
                            }
                        }
                    }
                }
                tabLayout.addOnTabSelectedListener(tabSelectedListener)
            }
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
    }

    @JvmStatic
    @BindingAdapter("app:convertDate")
    fun convertDate(v: TextView?, text: String?) {

        if (v != null) {
            text?.let {
                v.text = CommonUtils.newDateFormat(it)
            }
        }
    }

    @JvmStatic
    @BindingAdapter("app:toHtml")
    fun toHtml(v: TextView?, text: String?) {

        if (v != null) {
            text?.let {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
                    v.text = Html.fromHtml(it, Html.FROM_HTML_MODE_LEGACY)
                else
                    v.text = Html.fromHtml(it)
            }
        }
    }

    @JvmStatic
    @BindingAdapter("app:combine", "app:more")
    fun toCombine(v: TextView?, text: String?, moreText: String?) {

        if (v != null) {
            if (text != null && moreText != null) {
                v.text = text.plus(" ").plus(moreText)
            }
        }
    }

    @JvmStatic
    @BindingAdapter("app:toPoint")
    fun toPoint(v: TextView?, point: Int?) {

        if (v != null) {
            point?.let {
                v.text = CommonUtils.getCommaNumeric(point.toFloat()).plus("P")
            }
        }
    }

    @JvmStatic
    @BindingAdapter("app:convertTemp")
    fun convertTemp(v: TextView?, temp: Float?) {

        if (v != null) {
            temp?.let {
                v.text = temp.toInt().toString().plus(v.context.getString(R.string.degrees))
            }
        }
    }

    @JvmStatic
    @BindingAdapter("app:convertFloatTemp")
    fun convertFloatTemp(v: TextView?, temp: Float?) {

        if (v != null) {
            temp?.let {
                v.text = temp.toString().plus(v.context.getString(R.string.degrees))
            }
        }
    }

    @JvmStatic
    @BindingAdapter("app:convertHumidity")
    fun convertHumidity(v: TextView?, temp: Int?) {

        if (v != null) {
            temp?.let {
                v.text = temp.toString().plus(v.context.getString(R.string.percent))
            }
        }
    }

    @JvmStatic
    @BindingAdapter(requireAll = false, value = ["app:fineDust", "app:useOriColor"])
    fun fineDust(v: TextView?, status: String?, useOriColor: Boolean?) {
        if (v != null) {
            status?.let {
                val first_str = v.context.getString(R.string.fine_dust).plus(" ")
                val last_str = it
                val spannableString = SpannableString(first_str)
                val builder = SpannableStringBuilder(spannableString)
                builder.append(last_str)

                val begin = first_str.length
                val end = builder.length
                val useOriginalColor = useOriColor ?: true
                val dustColor = when (it) {
                    DustEnum.FINE.value -> if (useOriginalColor) R.color.dust_fine_color else R.color.dust_fine_color2
                    DustEnum.NORMAL.value -> if (useOriginalColor) R.color.dust_normal_color else R.color.dust_normal_color2
                    DustEnum.BAD.value -> if (useOriginalColor) R.color.dust_bad_color else R.color.dust_bad_color2
                    DustEnum.VERY_BAD.value -> if (useOriginalColor) R.color.dust_very_bad_color else R.color.dust_very_bad_color2
                    else -> R.color.dust_fine_color
                }

                builder.setSpan(
                    ForegroundColorSpan(ContextCompat.getColor(v.context, dustColor)),
                    begin,
                    end,
                    Spannable.SPAN_INCLUSIVE_EXCLUSIVE
                )
                v.text = builder
            }
        }
    }

    @JvmStatic
    @BindingAdapter(requireAll = false, value = ["app:ultraDust", "app:useOriColor"])
    fun ultraDust(v: TextView?, status: String?, useOriColor: Boolean?) {
        if (v != null) {
            status?.let {
                val first_str = v.context.getString(R.string.ultra_dust).plus(" ")
                val last_str = it
                val spannableString = SpannableString(first_str)
                val builder = SpannableStringBuilder(spannableString)
                builder.append(last_str)

                val begin = first_str.length
                val end = builder.length
                val useOriginalColor = useOriColor ?: true
                val dustColor = when (it) {
                    DustEnum.FINE.value -> if (useOriginalColor) R.color.dust_fine_color else R.color.dust_fine_color2
                    DustEnum.NORMAL.value -> if (useOriginalColor) R.color.dust_normal_color else R.color.dust_normal_color2
                    DustEnum.BAD.value -> if (useOriginalColor) R.color.dust_bad_color else R.color.dust_bad_color2
                    DustEnum.VERY_BAD.value -> if (useOriginalColor) R.color.dust_very_bad_color else R.color.dust_very_bad_color2
                    else -> R.color.dust_fine_color
                }

                builder.setSpan(
                    ForegroundColorSpan(ContextCompat.getColor(v.context, dustColor)),
                    begin,
                    end,
                    Spannable.SPAN_INCLUSIVE_EXCLUSIVE
                )
                v.text = builder
            }
        }
    }

    @JvmStatic
    @BindingAdapter("app:humidity")
    fun humidity(v: TextView?, percentage: Int?) {
        v?.apply {
            percentage ?: return@apply

            text = String.format(context.getString(R.string.humidity_percentage), percentage)
        }
    }

    @JvmStatic
    @BindingAdapter("app:tint")
    fun ImageView.setImageTint(@ColorInt color: Int) {
        setColorFilter(color)
    }

}

@BindingAdapter("viewPager2SetupTabLayout")
fun viewPager2SetupTabLayout(tabLayout: TabLayout?, viewPager: ViewPager2?) {
    BindingAdapters.viewPager2SetupTabLayout(tabLayout, viewPager)
}

@BindingAdapter("submitWeatherList")
fun bindWeatherList(recyclerView: RecyclerView, weatherList: List<Weather>?) {
    val adapter = recyclerView.adapter
    if (adapter is HourlyWeatherAdapter) {
        adapter.submitList(weatherList ?: emptyList())
    }
}

