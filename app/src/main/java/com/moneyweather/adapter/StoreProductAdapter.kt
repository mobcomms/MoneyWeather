package com.moneyweather.adapter

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.moneyweather.R
import com.moneyweather.databinding.ViewholderHorizonProductBinding
import com.moneyweather.databinding.ViewholderProductBinding
import com.moneyweather.model.ShopProductItem
import com.moneyweather.ui.viewholder.ProductHorizontalViewHolder
import com.moneyweather.ui.viewholder.ProductViewHolder
import com.moneyweather.util.OnItemClickListener
import com.moneyweather.util.analytics.GaCouponClickEvent.logCouponNpay
import com.moneyweather.view.ProductDetailActivity
import com.moneyweather.view.fragment.ShopFragment

class StoreProductAdapter(val context: Context, val storeFragment: ShopFragment? = null, val isHorizontal: Boolean = false) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private var mIsPageLoadEnable = false
    private var mPage = 0
    private val productList: MutableList<ShopProductItem> = ArrayList()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        if (!isHorizontal) {
            val binding = DataBindingUtil.inflate<ViewholderProductBinding>(inflater, R.layout.viewholder_product, parent, false)
            return ProductViewHolder(binding, mOnItemClickListener)
        } else {
            val binding = DataBindingUtil.inflate<ViewholderHorizonProductBinding>(inflater, R.layout.viewholder_horizon_product, parent, false)
            return ProductHorizontalViewHolder(binding, mOnItemClickListener)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is ProductViewHolder) {
            holder.bind(productList[position])
        } else if (holder is ProductHorizontalViewHolder) {
            holder.bind(productList[position])
        }

        if (mIsPageLoadEnable && position == itemCount - 1) {
            storeFragment?.searchProduct(++mPage)
        }
    }

    override fun getItemCount(): Int {
        return productList.size
    }

    fun initData() {
        mIsPageLoadEnable = false
        mPage = 1

        productList.clear()
        notifyDataSetChanged()
    }

    fun addData(list: List<ShopProductItem>?) {
        mIsPageLoadEnable = !list.isNullOrEmpty() && list.size >= 20

        val positionStart = productList.size

        if (isHorizontal) {
            for (item in list!!) {
                item.goodsName = item.goodsName!!.replace("네이버페이 포인트 ", "Npay ")
            }
        }

        if (productList.size > 0) {
            productList.clear()
        }
        productList.addAll(list!!)

        notifyItemRangeInserted(positionStart, list.size)
    }

    private val mOnItemClickListener: OnItemClickListener = object : OnItemClickListener() {
        override fun onClick(view: View?, position: Int, `object`: Any?) {
            super.onClick(view, position, `object`)

            val intent = Intent(context, ProductDetailActivity::class.java)
            intent.putExtra("productPk", productList[position].goodsId)
            context.startActivity(intent)

            if (isHorizontal) {
                // Ga Log Event
                logCouponNpay(productList[position].goodsName ?: "")
            }
        }
    }
}
