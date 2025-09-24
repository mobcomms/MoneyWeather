package com.moneyweather.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.moneyweather.databinding.ViewholderPointHistoryBinding
import com.moneyweather.model.PointHistoryItem
import javax.inject.Inject

class PointHistoryAdapter @Inject constructor() : ListAdapter<PointHistoryItem, RecyclerView.ViewHolder>(HistoryListDiffCallback()) {
    val TAG = this.javaClass.simpleName

    var onItemLastListener: OnItemLastListener? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return ItemHolder(ViewholderPointHistoryBinding.inflate(LayoutInflater.from(parent.context), parent, false), onItemLastListener)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is ItemHolder)
            holder.bind(getItem(position)!!)

        if (position == getItemCount() - 1) {
            onItemLastListener?.onItemLast()
        }


    }

    override fun submitList(list: List<PointHistoryItem>?) {
        super.submitList(list?.let { ArrayList(it) })
    }

    class ItemHolder(val binding: ViewholderPointHistoryBinding, val onItemLastListener: OnItemLastListener?) : RecyclerView.ViewHolder(binding.root) {
        val TAG = this.javaClass.simpleName
        fun bind(m: PointHistoryItem) {
            binding.apply {
                model = m
                executePendingBindings()
            }
        }
    }


    interface OnItemLastListener {
        fun onItemLast()
    }
}

class HistoryListDiffCallback : DiffUtil.ItemCallback<PointHistoryItem>() {
    override fun areItemsTheSame(oldItem: PointHistoryItem, newItem: PointHistoryItem): Boolean {
        return oldItem.createdAt == newItem.createdAt
    }

    override fun areContentsTheSame(oldItem: PointHistoryItem, newItem: PointHistoryItem): Boolean {
        return oldItem.createdAt == newItem.createdAt
    }
}