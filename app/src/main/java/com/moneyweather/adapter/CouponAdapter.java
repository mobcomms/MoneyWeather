package com.moneyweather.adapter;

import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.moneyweather.R;
import com.moneyweather.databinding.ViewholderCouponBinding;
import com.moneyweather.model.CouponItem;

import com.moneyweather.ui.viewholder.CouponViewHolder;
import com.moneyweather.util.CommonUtils;
import com.moneyweather.util.OnItemClickListener;
import com.moneyweather.view.MyCouponActivity;


import java.util.ArrayList;
import java.util.List;

/**
 * 쿠폰 리스트 어댑터
 */
public class CouponAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final Context mContext;
    private ArrayList<CouponItem> mCouponList;
    private boolean mIsPageLoadEnable; // 다음 페이지 로딩 가능 판별값
    private int mPage; // 페이지 번호
    private final int LIST_ITEM_COUNT = 20;
    private final String PAYLOAD_TYPE = "PAYLOAD_TYPE";
    private final String PAYLOAD_UPDATE_COUPON_STATE = "PAYLOAD_UPDATE_COUPON_STATE";

    public CouponAdapter(Context context) {
        this.mContext = context;
    }

    public void setData(int page, ArrayList<CouponItem> couponList) {
        if (mCouponList == null) {
            mIsPageLoadEnable = CommonUtils.Companion.isListEmpty(couponList) == false && couponList.size() >= 20;
            mPage = page;
            mCouponList = couponList;
            notifyDataSetChanged();
        } else {
            if (page == 0) {
                mIsPageLoadEnable = CommonUtils.Companion.isListEmpty(couponList) == false && couponList.size() >= 20;
                mPage = page;
                mCouponList = couponList;
                notifyDataSetChanged();
            } else {
                if (CommonUtils.Companion.isListEmpty(couponList) == false) {
                    mIsPageLoadEnable = couponList.size() % LIST_ITEM_COUNT == 0;
                    mCouponList.addAll(couponList);
                    notifyDataSetChanged();
                } else {
                    mIsPageLoadEnable = false;
                }
            }
        }
    }

    public void updateCouponState(int couponPk, int state) {
        int position = -1;
        for (CouponItem coupon : mCouponList) {
            if (coupon.getCouponId() == couponPk) {
                coupon.setStatus(state);
                position = mCouponList.indexOf(coupon);
                break;
            }
        }
        if (position >= 0) {
            notifyItemChanged(position, getPayload(PAYLOAD_UPDATE_COUPON_STATE));
        }
    }

    private Bundle getPayload(@NonNull String type) {
        Bundle payload = new Bundle();
        payload.putString(PAYLOAD_TYPE, type);
        return payload;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        ViewholderCouponBinding binding = DataBindingUtil.inflate(inflater, R.layout.viewholder_coupon, parent, false);
        return new CouponViewHolder(binding, mOnItemClickListener);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position, @NonNull List<Object> payloads) {
        if (!payloads.isEmpty()) {
            for (Object payload : payloads) {
                if (!(payload instanceof Bundle)) {
                    return;
                }
                Bundle payloadItem = (Bundle) payload;
                String type = TextUtils.isEmpty(payloadItem.getString(PAYLOAD_TYPE)) ? "" : payloadItem.getString(PAYLOAD_TYPE);
                switch (type) {
                    case PAYLOAD_UPDATE_COUPON_STATE:
                        if (holder instanceof CouponViewHolder) {
                            int state = mCouponList.get(position).getStatus();
                            ((CouponViewHolder) holder).updateState(state);
                        }
                        break;
                }
            }
        } else {
            super.onBindViewHolder(holder, position, payloads);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (CommonUtils.Companion.isListEmpty(mCouponList) == false) {
            CouponItem couponItem = mCouponList.get(position);
            ((CouponViewHolder)holder).bind(couponItem);
        }

        if (mIsPageLoadEnable && position == getItemCount() - 1) {
            mPage++;
            mIsPageLoadEnable = false;
            if (mContext instanceof MyCouponActivity) {
                ((MyCouponActivity) mContext).getCouponList(mPage);
            }
        }
    }

    @Override
    public int getItemCount() {
        return CommonUtils.Companion.isListEmpty(mCouponList) ? 0 : mCouponList.size();
    }

    private final OnItemClickListener mOnItemClickListener = new OnItemClickListener() {
        @Override
        public void onClick(View view, int position, Object object) {
            super.onClick(view, position, object);
            CouponItem coupon = (CouponItem) object;
            if (coupon != null) {
                ((MyCouponActivity)mContext).startDetailActivity(coupon.getCouponId());
            }
        }
    };


}
