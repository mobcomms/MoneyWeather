package com.moneyweather.ui.viewholder;

import android.content.Context;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.moneyweather.R;
import com.moneyweather.databinding.ViewholderHorizonProductBinding;
import com.moneyweather.model.ShopProductItem;
import com.moneyweather.util.CommonUtils;
import com.moneyweather.util.OnItemClickListener;

public class ProductHorizontalViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

    private ViewholderHorizonProductBinding mBinding;
    private Context mContext;

    private final OnItemClickListener mOnItemClickListener;

    public ProductHorizontalViewHolder(@NonNull ViewholderHorizonProductBinding binding,
                                       OnItemClickListener onItemClickListener) {
        super(binding.getRoot());
        mBinding = binding;
        mBinding.setClickListener(this);
        mBinding.executePendingBindings();
        mContext = mBinding.getRoot().getContext();
        mOnItemClickListener = onItemClickListener;
    }

    public void bind(ShopProductItem productItemVO) {

        Glide.with(mContext)
                .load(productItemVO.getGoodsImageSmall())
                .centerInside()
                .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                .into(mBinding.imgProduct);

        mBinding.txtProduct.setText(productItemVO.getGoodsName());

        String priceStr = String.format(mContext.getString(R.string.add_point), CommonUtils.Companion.getCommaNumeric(productItemVO.getSalePrice()));
        mBinding.txtPrice.setText(priceStr);

    }

    @Override
    public void onClick(View view) {
        mOnItemClickListener.onSingleClick(view, getAdapterPosition(), null);
    }

}
