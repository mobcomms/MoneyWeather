package com.moneyweather.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.moneyweather.data.remote.response.RegionResponse
import com.moneyweather.databinding.ItemRegionBinding
import org.apache.commons.lang3.StringUtils
import javax.inject.Inject

class RegionAdapter @Inject constructor() : ListAdapter<RegionResponse.Data, RecyclerView.ViewHolder>(RegionListDiffCallback()) {
    val TAG = this.javaClass.simpleName

    private val _onClickItem: MutableLiveData<RegionResponse.Data> = MutableLiveData()
    val onClickItem: MutableLiveData<RegionResponse.Data> get() = _onClickItem

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return ItemHolder(ItemRegionBinding.inflate(LayoutInflater.from(parent.context), parent, false), _onClickItem)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is ItemHolder)
            holder.bind(getItem(position)!!)
    }

    class ItemHolder(val binding: ItemRegionBinding, val onClickItem: MutableLiveData<RegionResponse.Data>) : RecyclerView.ViewHolder(binding.root) {
        val TAG = this.javaClass.simpleName
        fun bind(m: RegionResponse.Data) {
            binding.apply {
                model = m
                if (absoluteAdapterPosition == 0 && !m.isPermission) {
                    tvCurrent.visibility = View.VISIBLE

                    var region = if (StringUtils.isNotEmpty(m.region.depth3)) {
                        m.region.depth2.plus(" ").plus(m.region.depth3)
                    } else {
                        m.region.depth2
                    }
                    tvName.text = region
                } else {
                    tvName.text = m.region.depth1
                }
                root.setOnClickListener { onClickItem.postValue(m) }
                executePendingBindings()

            }
        }
    }
}

class RegionListDiffCallback : DiffUtil.ItemCallback<RegionResponse.Data>() {
    override fun areItemsTheSame(oldItem: RegionResponse.Data, newItem: RegionResponse.Data): Boolean {
        return true
    }

    override fun areContentsTheSame(oldItem: RegionResponse.Data, newItem: RegionResponse.Data): Boolean {
        return true
    }
}