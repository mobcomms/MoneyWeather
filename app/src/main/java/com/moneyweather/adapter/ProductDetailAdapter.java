package com.moneyweather.adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.moneyweather.R;
import com.moneyweather.databinding.ItemProductDetailBinding;
import com.moneyweather.model.ProductItem;
import com.moneyweather.view.ProductDetailActivity;

public class ProductDetailAdapter extends RecyclerView.Adapter<ProductDetailAdapter.DetailViewHolder> {

    private ProductDetailActivity mProductDetailActivity;
    private ProductItem mProductVO = new ProductItem();

    public ProductDetailAdapter(ProductDetailActivity productDetailActivity) {
        mProductDetailActivity = productDetailActivity;
    }

    @NonNull
    @Override
    public DetailViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        ItemProductDetailBinding binding = DataBindingUtil.inflate(inflater, R.layout.item_product_detail, parent, false);
        return new DetailViewHolder(mProductDetailActivity, binding);
    }

    @Override
    public void onBindViewHolder(@NonNull DetailViewHolder holder, int position) {
        holder.bind(mProductVO);
    }

    @Override
    public int getItemCount() {
        return 2;
    }

    public void updateData(ProductItem productVO) {
        mProductVO.setGoodsDescription(productVO.getGoodsDescription());
        mProductVO.setCaution(productVO.getCaution());
        notifyDataSetChanged();
    }

    public static class DetailViewHolder extends RecyclerView.ViewHolder {

        private ProductDetailActivity mProductDetailActivity;
        private final ItemProductDetailBinding binding;

        public DetailViewHolder(ProductDetailActivity productDetailActivity, @NonNull ItemProductDetailBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
            mProductDetailActivity = productDetailActivity;
        }

        public void bind(ProductItem productVO) {
            if (getAdapterPosition() == 0) {
                binding.desc.setText(productVO.getGoodsDescription());
            } else if (getAdapterPosition() == 1) {
                binding.desc.setText(productVO.getCaution());
            }
        }
    }

}
