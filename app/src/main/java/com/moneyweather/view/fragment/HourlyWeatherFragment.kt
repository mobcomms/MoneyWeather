package com.moneyweather.view.fragment

import androidx.core.content.ContextCompat
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.moneyweather.R
import com.moneyweather.adapter.HourlyWeatherAdapter
import com.moneyweather.base.BaseKotlinFragment
import com.moneyweather.databinding.FragmentHourlyBinding
import com.moneyweather.event.lockscreen.LockScreenUiEffect
import com.moneyweather.event.theme.ThemeWeatherUiEvent
import com.moneyweather.extensions.isScreenOn
import com.moneyweather.extensions.throttleFirst
import com.moneyweather.model.Weather
import com.moneyweather.ui.SlideDetectConstraintLayout
import com.moneyweather.view.LockScreenActivity.Companion.FETCH_REFRESH_DATA_DELAY
import com.moneyweather.viewmodel.LockScreenViewModel
import com.moneyweather.viewmodel.ThemeWeatherViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.milliseconds

@AndroidEntryPoint
class HourlyWeatherFragment : BaseKotlinFragment<FragmentHourlyBinding, ThemeWeatherViewModel>() {
    override val layoutResourceId: Int get() = R.layout.fragment_hourly
    override val viewModel: ThemeWeatherViewModel by viewModels()
    private val activityViewModel: LockScreenViewModel by activityViewModels()

    override fun initStartView() {
        viewDataBinding.vm = viewModel

        setHourlyWeatherAdapter()
        setLineChart()

        observeViewModel()
        observeActivityViewModel()

        refreshWeather()
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.RESUMED) {
                viewModel.weatherInfos.collect { response ->
                    response?.let {
                        submitLineChartData(it)
                    }
                }
            }
        }
    }

    private fun observeActivityViewModel() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                activityViewModel.replayEffect
                    .throttleFirst(FETCH_REFRESH_DATA_DELAY.milliseconds)
                    .collect { effect ->
                        when (effect.data) {
                            is LockScreenUiEffect.RefreshData -> {
                                refreshWeather(fromCreated = false)
                            }
                        }
                    }
            }
        }
    }

    private fun refreshWeather(fromCreated: Boolean = true) {
        if (!fromCreated && !requireActivity().isScreenOn()) return

        viewModel.dispatchEvent(ThemeWeatherUiEvent.FetchHourlyWeather)
    }

    private fun setHourlyWeatherAdapter() {
        val parent = requireActivity().findViewById<SlideDetectConstraintLayout>(R.id.rootLayout)
        parent.setTargetRecyclerView(viewDataBinding.recyclerHourlyWeather)

        viewDataBinding.recyclerHourlyWeather.apply {
            adapter = HourlyWeatherAdapter()

            addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    setLineChartRangeAndMove(recyclerView)
                }
            })
        }
    }

    private fun setLineChartRangeAndMove(recyclerView: RecyclerView) {
        recyclerView.post {
            val layoutManager = recyclerView.layoutManager as? LinearLayoutManager
            val firstVisibleItemPosition = layoutManager?.findFirstVisibleItemPosition() ?: 0
            val lastVisibleItemPosition = layoutManager?.findLastVisibleItemPosition() ?: 0
            val visibleItemCount = lastVisibleItemPosition - firstVisibleItemPosition + 0.25f
            val firstVisibleItem = layoutManager?.findViewByPosition(firstVisibleItemPosition) ?: return@post

            // 스크롤 된 경우 (아이템 너비 기준 비율)
            val left = firstVisibleItem.left.toFloat()
            val itemWidth = firstVisibleItem.width.toFloat()
            val offset = -left / itemWidth
            val targetX = firstVisibleItemPosition + offset

            viewDataBinding.lineChart.apply {
                setVisibleXRangeMaximum(visibleItemCount)
                moveViewToX(targetX)
            }
        }
    }

    private fun submitLineChartData(hourlyWeatherList: List<Weather>) {
        val temperatureList = hourlyWeatherList.map { it.temp.toInt() }

        val spacingFactor = 0.3f
        val entryList = arrayListOf<Entry>()
        temperatureList.forEachIndexed { index, data ->
            entryList.add(Entry(index + spacingFactor, data.toFloat()))
        }

        val lineDataSet = LineDataSet(entryList, "Temperature").apply {
            circleRadius = 2.0f
            circleHoleRadius = 2.0f
            lineWidth = 1.0f
            valueTextSize = 0f
            setCircleColor(ContextCompat.getColor(requireContext(), R.color.hour_weather_line_point))
            circleHoleColor = ContextCompat.getColor(requireContext(), R.color.hour_weather_line_point)
            color = ContextCompat.getColor(requireContext(), R.color.hour_weather_line_color)
        }

        viewDataBinding.lineChart.apply {
            xAxis.axisMaximum = temperatureList.size.toFloat()
            data = LineData(listOf(lineDataSet))
        }

        setLineChartRangeAndMove(viewDataBinding.recyclerHourlyWeather)
    }

    private fun setLineChart() {
        viewDataBinding.lineChart.apply {
            setPinchZoom(false)
            setScaleEnabled(false)
            setTouchEnabled(false)
            isDragEnabled = false
            isScaleXEnabled = false
            isScaleYEnabled = false
            isDoubleTapToZoomEnabled = false
            axisRight.isEnabled = false
            axisLeft.isEnabled = false
            legend.isEnabled = false
            description.isEnabled = false
            xAxis.apply {
                isEnabled = false
                spaceMin = 0f
                spaceMax = 0f
                axisMinimum = 0f
                setAvoidFirstLastClipping(true)
            }
        }
    }
}