package com.moneyweather.view.fragment

import androidx.fragment.app.viewModels
import com.moneyweather.R
import com.moneyweather.base.BaseKotlinFragment
import com.moneyweather.databinding.FragmentWeeklyBinding
import com.moneyweather.event.theme.ThemeWeatherUiEvent
import com.moneyweather.util.WeatherUtils
import com.moneyweather.viewmodel.ThemeWeatherViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class WeeklyWeatherFragment : BaseKotlinFragment<FragmentWeeklyBinding, ThemeWeatherViewModel>() {
    override val layoutResourceId: Int get() = R.layout.fragment_weekly
    override val viewModel: ThemeWeatherViewModel by viewModels()

    override fun initStartView() {
        viewDataBinding.vm = viewModel

        setWeeklyWeather()
        refreshWeather()
    }

    private fun refreshWeather() {
        viewModel.dispatchEvent(ThemeWeatherUiEvent.FetchWeeklyWeather)
    }

    private fun setWeeklyWeather() {
        with(viewDataBinding) {
            val dayTextViews = listOf(dayOfWeek0, dayOfWeek1, dayOfWeek2, dayOfWeek3, dayOfWeek4, dayOfWeek5, dayOfWeek6)
            val today = WeatherUtils.getDayOfWeek()

            dayTextViews.forEachIndexed { i, textView ->
                val dayIndex = ((today - 1 + i) % 7) + 1
                textView.text = WeatherUtils.getDayOfWeekStr(dayIndex)
            }
        }
    }
}