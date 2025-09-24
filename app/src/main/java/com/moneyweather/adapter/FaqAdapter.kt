package com.moneyweather.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.moneyweather.databinding.ItemFaqBinding
import com.moneyweather.model.Faq
import javax.inject.Inject

class FaqAdapter @Inject constructor() : ListAdapter<Faq, RecyclerView.ViewHolder>(FaqListDiffCallback()) {
    val TAG = this.javaClass.simpleName

    private val _onClickItem: MutableLiveData<Faq> = MutableLiveData()
    var onItemClickListener: OnItemClickListener? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return ItemHolder(ItemFaqBinding.inflate(LayoutInflater.from(parent.context), parent, false), onItemClickListener)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is ItemHolder)
            holder.bind(getItem(position)!!)
    }

    class ItemHolder(val binding: ItemFaqBinding, val onItemClickListener: OnItemClickListener?) : RecyclerView.ViewHolder(binding.root) {
        val TAG = this.javaClass.simpleName
        fun bind(m: Faq) {
            binding.apply {
                model = m
                //root.setOnClickListener { onClickItem.postValue(m) }
                root.setOnClickListener { onItemClickListener?.onItemClick(m) }
                executePendingBindings()
            }
        }
    }

    interface OnItemClickListener {
        fun onItemClick(m: Faq)
    }
}

class FaqListDiffCallback : DiffUtil.ItemCallback<Faq>() {
    override fun areItemsTheSame(oldItem: Faq, newItem: Faq): Boolean {
        return oldItem.createdAt == newItem.createdAt
    }

    override fun areContentsTheSame(oldItem: Faq, newItem: Faq): Boolean {
        return oldItem.createdAt == newItem.createdAt
    }
}