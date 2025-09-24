package com.moneyweather.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.moneyweather.databinding.ItemQaBinding
import com.moneyweather.model.Qa
import javax.inject.Inject

class QaListAdapter @Inject constructor() : ListAdapter<Qa, RecyclerView.ViewHolder>(QaListDiffCallback()) {
    val TAG = this.javaClass.simpleName

    private val _onClickItem: MutableLiveData<Qa> = MutableLiveData()

    // val onClickItem: MutableLiveData<Qa> get() = _onClickItem
    var onItemClickListener: OnItemClickListener? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return ItemHolder(ItemQaBinding.inflate(LayoutInflater.from(parent.context), parent, false), onItemClickListener)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is ItemHolder)
            holder.bind(getItem(position)!!)
    }

    class ItemHolder(val binding: ItemQaBinding, val onItemClickListener: OnItemClickListener?) : RecyclerView.ViewHolder(binding.root) {
        val TAG = this.javaClass.simpleName
        fun bind(m: Qa) {
            binding.apply {
                model = m
                //root.setOnClickListener { onClickItem.postValue(m) }
                root.setOnClickListener {
                    onItemClickListener?.onItemClick(m, adapterPosition)
                }
                executePendingBindings()
            }
        }
    }

    interface OnItemClickListener {
        fun onItemClick(m: Qa, position: Int)
    }
}

class QaListDiffCallback : DiffUtil.ItemCallback<Qa>() {
    override fun areItemsTheSame(oldItem: Qa, newItem: Qa): Boolean {
        return oldItem.createdAt == newItem.createdAt
    }

    override fun areContentsTheSame(oldItem: Qa, newItem: Qa): Boolean {
        return oldItem.createdAt == newItem.createdAt
    }
}