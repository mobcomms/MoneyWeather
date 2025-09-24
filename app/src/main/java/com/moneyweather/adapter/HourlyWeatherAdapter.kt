package com.moneyweather.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.moneyweather.databinding.ItemHourlyWeatherBinding
import com.moneyweather.model.Weather

class HourlyWeatherAdapter : ListAdapter<Weather, HourlyWeatherAdapter.HourlyWeatherHolder>(WeatherDiffCallback) {

    inner class HourlyWeatherHolder(private val binding: ItemHourlyWeatherBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(weather: Weather) {
            binding.apply {
                Glide.with(root.context)
                    .load(weather.weatherImage())
                    .into(weatherIV)

                hourTV.text = weather.hour()
                tempTV.text = weather.temp()
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = HourlyWeatherHolder(
        ItemHourlyWeatherBinding.inflate(LayoutInflater.from(parent.context), parent, false)
    )

    override fun onBindViewHolder(holder: HourlyWeatherHolder, position: Int) {
        holder.bind(getItem(position))
    }
}