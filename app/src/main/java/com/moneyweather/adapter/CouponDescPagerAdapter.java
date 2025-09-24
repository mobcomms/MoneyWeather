package com.moneyweather.adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.moneyweather.R;
import com.moneyweather.databinding.ViewholderCouponDescBinding;
import com.moneyweather.util.CommonUtils;

import java.util.ArrayList;

public class CouponDescPagerAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private ArrayList<String> mData;

    public void setData(ArrayList<String> data) {
        mData = data;
        notifyDataSetChanged();
    }


    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        ViewholderCouponDescBinding binding = DataBindingUtil.inflate(inflater, R.layout.viewholder_coupon_desc, parent, false);
        return new TestViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        String content = mData.get(position);
        ((TestViewHolder) holder).bind(content);
    }

    @Override
    public int getItemCount() {
        return CommonUtils.Companion.isListEmpty(mData) ? 0 : mData.size();
    }

    public static class TestViewHolder extends RecyclerView.ViewHolder {

        private ViewholderCouponDescBinding mBinding;

        public TestViewHolder(@NonNull ViewholderCouponDescBinding binding) {
            super(binding.getRoot());
            mBinding = binding;
        }

        public void bind(String content) {
            mBinding.txtContent.setText(content);
        }
    }

}
