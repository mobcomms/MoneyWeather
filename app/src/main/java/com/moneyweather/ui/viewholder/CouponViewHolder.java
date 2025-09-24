package com.moneyweather.ui.viewholder;

import android.content.Context;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.moneyweather.R;
import com.moneyweather.databinding.ViewholderCouponBinding;
import com.moneyweather.model.CouponItem;
import com.moneyweather.model.enums.CouponStatus;
import com.moneyweather.util.CommonUtils;
import com.moneyweather.util.Logger;
import com.moneyweather.util.OnItemClickListener;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class CouponViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

    private ViewholderCouponBinding mBinding;
    private final Context mContext;
    private final OnItemClickListener mOnItemClickListener;
    private CouponItem mCouponItem;

    public CouponViewHolder(@NonNull ViewholderCouponBinding binding,
                            OnItemClickListener onItemClickListener) {
        super(binding.getRoot());
        mBinding = binding;
        mContext = mBinding.getRoot().getContext();
        mBinding.setClickListener(this);
        mBinding.executePendingBindings();
        mOnItemClickListener = onItemClickListener;
    }

    public void bind(CouponItem couponItem) {
        mCouponItem = couponItem;

        int corner = CommonUtils.Companion.dpToPx(mContext, 10);

        // 상품 이미지
        Glide.with(mContext)
                .load(couponItem.getGoodsImageSmall())
                .transform(new CenterCrop(), new RoundedCorners(corner))
                .error(R.drawable.img_coupon_error)
                .fallback(R.drawable.img_coupon_error)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(mBinding.imgProduct);

        // 판매처
        mBinding.txtAffiliate.setText(mCouponItem.getAffiliate());

        // 상품명
        mBinding.txtProduct.setText(mCouponItem.getGoodsName());

        // 유효 날짜
//        try {
//            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
//            Date getDate = dateFormat.parse(mCouponItem.getLimitDay());
//            SimpleDateFormat transFormat = new SimpleDateFormat("yyyy. MM. dd");
//            String dueDate = transFormat.format(getDate) + "까지";
//            mBinding.txtDueDate.setText(dueDate);
//        } catch (ParseException e) {
//            mBinding.txtDueDate.setText(mCouponItem.getLimitDay()+ "까지");
//            Logger.e(e.getMessage());
//        }
        mBinding.txtDueDate.setText(mCouponItem.getLimit());

        // 쿠폰 상태에 따른 화면 처리
        CouponStatus couponStatus = CouponStatus.find(couponItem.getStatus());
        if (couponStatus == CouponStatus.BUY) {
            mBinding.groupCouponStatus.setVisibility(View.GONE);
            mBinding.txtProduct.setTextColor(ContextCompat.getColor(mContext, R.color.black));
        } else {
            mBinding.groupCouponStatus.setVisibility(View.VISIBLE);
            mBinding.txtProduct.setTextColor(ContextCompat.getColor(mContext, R.color.pinkish_grey));
            if (couponStatus.getContentStrRes() != 0) {
                mBinding.txtStatus.setText(mContext.getString(couponStatus.getContentStrRes()));
            }
            if (couponStatus.getContentStrColor() != 0) {
                mBinding.txtStatus.setTextColor(ContextCompat.getColor(mContext, couponStatus.getContentStrColor()));
            }
        }

    }

    public void updateState(int state) {

        mCouponItem.setStatus(state);

        CouponStatus couponStatus = CouponStatus.find(mCouponItem.getStatus());
        if (couponStatus == CouponStatus.BUY) {
            mBinding.groupCouponStatus.setVisibility(View.GONE);
            mBinding.txtProduct.setTextColor(ContextCompat.getColor(mContext, R.color.black));
        } else {
            mBinding.groupCouponStatus.setVisibility(View.VISIBLE);
            mBinding.txtProduct.setTextColor(ContextCompat.getColor(mContext, R.color.pinkish_grey));
            if (couponStatus.getContentStrRes() != 0) {
                mBinding.txtStatus.setText(mContext.getString(couponStatus.getContentStrRes()));
            }
            if (couponStatus.getContentStrColor() != 0) {
                mBinding.txtStatus.setTextColor(ContextCompat.getColor(mContext, couponStatus.getContentStrColor()));
            }
        }

    }

    @Override
    public void onClick(View view) {
        mOnItemClickListener.onSingleClick(view, getAdapterPosition(), mCouponItem);
    }

}
