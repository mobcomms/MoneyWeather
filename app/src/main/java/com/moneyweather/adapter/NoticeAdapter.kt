package com.moneyweather.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.moneyweather.databinding.ItemNoticeBinding
import com.moneyweather.model.Notice
import javax.inject.Inject

class NoticeAdapter @Inject constructor() : ListAdapter<Notice, RecyclerView.ViewHolder>(NoticeListDiffCallback()) {
    val TAG = this.javaClass.simpleName

    private val _onClickItem: MutableLiveData<Notice> = MutableLiveData()

    // val onClickItem: MutableLiveData<Notice> get() = _onClickItem
    var onItemClickListener: OnItemClickListener? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return ItemHolder(ItemNoticeBinding.inflate(LayoutInflater.from(parent.context), parent, false), onItemClickListener)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is ItemHolder)
            holder.bind(getItem(position)!!)
    }

    class ItemHolder(val binding: ItemNoticeBinding, val onItemClickListener: OnItemClickListener?) : RecyclerView.ViewHolder(binding.root) {
        val TAG = this.javaClass.simpleName
        fun bind(m: Notice) {
            binding.apply {
                model = m
                //root.setOnClickListener { onClickItem.postValue(m) }
                root.setOnClickListener { onItemClickListener?.onItemClick(m, adapterPosition) }
                executePendingBindings()
            }
        }
    }

    interface OnItemClickListener {
        fun onItemClick(m: Notice, position: Int)
    }
}

class NoticeListDiffCallback : DiffUtil.ItemCallback<Notice>() {
    override fun areItemsTheSame(oldItem: Notice, newItem: Notice): Boolean {
        return oldItem.createdAt == newItem.createdAt
    }

    override fun areContentsTheSame(oldItem: Notice, newItem: Notice): Boolean {
        return oldItem.createdAt == newItem.createdAt
    }
}