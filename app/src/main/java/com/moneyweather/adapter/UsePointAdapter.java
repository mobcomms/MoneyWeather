package com.moneyweather.adapter;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.moneyweather.R;
import com.moneyweather.databinding.ViewholderPointHistoryBinding;
import com.moneyweather.model.CashHistoryItem;
import com.moneyweather.util.CommonUtils;
import com.moneyweather.util.Logger;

import java.util.ArrayList;

/**
 * 포인트 사용 내역 리스트 어댑터
 */
public class UsePointAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private ArrayList<CashHistoryItem> mUsePointList;

    public void setData(ArrayList<CashHistoryItem> usePointList) {
        mUsePointList = usePointList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        ViewholderPointHistoryBinding binding = DataBindingUtil.inflate(inflater, R.layout.viewholder_point_history, parent, false);
        return new UsePointViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {

        CashHistoryItem cash = mUsePointList.get(position);

//        if (holder.getItemViewType() == ItemViewType.WITHDRAW_COMPLETE.ordinal()) {
//            ((UsePointViewHolder) holder).bindComplete(cash);
//        } else if (holder.getItemViewType() == ItemViewType.WITHDRAW_REQUEST.ordinal()) {
//            ((UsePointViewHolder) holder).bindRequest(cash);
//        } else if (holder.getItemViewType() == ItemViewType.WITHDRAW_CANCEL.ordinal()) {
//            ((UsePointViewHolder) holder).bindCancel(cash);
//        } else if(holder.getItemViewType() == ItemViewType.STORE.ordinal()) {
//            ((UsePointViewHolder) holder).bindStore(cash);
//        }

    }

    public class UsePointViewHolder extends RecyclerView.ViewHolder{

        private final ViewholderPointHistoryBinding mBinding;
        private final Context mContext;

        public UsePointViewHolder(@NonNull ViewholderPointHistoryBinding binding) {
            super(binding.getRoot());
            mBinding = binding;
            mContext = mBinding.getRoot().getContext();
        }

        public void bindComplete(CashHistoryItem cash) {

            mBinding.txtContent.setText(cash.getComment());

            // 수수료 포함 금액
            String price = String.format(mContext.getString(R.string.use_point), CommonUtils.Companion.getCommaNumeric(cash.getCash()));
            mBinding.txtPoint.setText(price);
            mBinding.txtPoint.setTextColor(ContextCompat.getColor(mContext, R.color.point_red_two));

            try {
                if (!TextUtils.isEmpty(cash.getReg_date())) {
                    mBinding.txtDate.setText(CommonUtils.Companion.newDateFormat(cash.getReg_date()));
                }
            } catch (Exception e) {
                Logger.e(e.getMessage());
            }
        }

    }

    @Override
    public int getItemCount() {
        return CommonUtils.Companion.isListEmpty(mUsePointList) ? 0 : mUsePointList.size();
    }

    @Override
    public int getItemViewType(int position) {
        if (!CommonUtils.Companion.isListEmpty(mUsePointList)) {
            CashHistoryItem cash = mUsePointList.get(position);
            // 타입이 1(출금)일 경우
//            if (cash.getType() == 1) {
//                if (cash.getState() == WithdrawStateType.COMPLETE.getState()) {
//                    return ItemViewType.WITHDRAW_COMPLETE.ordinal();
//                } else if (cash.getState() == WithdrawStateType.REQUEST.getState()) {
//                    return ItemViewType.WITHDRAW_REQUEST.ordinal();
//                } else {
//                    return ItemViewType.WITHDRAW_CANCEL.ordinal();
//                }
//            } else {
//                // 타입이 2(스토어)일 경우
//                return ItemViewType.STORE.ordinal();
//            }
        }
        return super.getItemViewType(position);
    }
}
