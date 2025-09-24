package com.moneyweather.model.enums;


import com.moneyweather.R;
import com.moneyweather.util.ValueOfJava;

/**
 * 쿠폰 상태
 */
public enum CouponStatus {

    BUY(0, 0, 0, 0), // 구매
    USE(1, R.string.coupon_status_use, R.color.pinkish_grey, R.drawable.ico_coupon_use), // 사용
    REFUND(2, R.string.coupon_status_refund, R.color.main_red, R.drawable.ico_coupon_refund), // 환불
    EXPIRE(3, R.string.coupon_status_expire, R.color.pinkish_grey, R.drawable.ico_coupon_exfire); // 만료

    private final int state;
    private final int contentStrRes;
    private final int contentStrColor;
    private final int contentImgRes;

    CouponStatus(int state,
                 int contentStrRes,
                 int contentStrColor,
                 int contentImgRes) {
        this.state = state;
        this.contentStrRes = contentStrRes;
        this.contentStrColor = contentStrColor;
        this.contentImgRes = contentImgRes;
    }

    public int getState() {
        return state;
    }

    public int getContentStrRes() {
        return contentStrRes;
    }

    public int getContentStrColor() {
        return contentStrColor;
    }

    public int getContentImgRes() {
        return contentImgRes;
    }

    public static CouponStatus find(int state) {
        if (state < 0) {
            return CouponStatus.BUY;
        }
        CouponStatus value = ValueOfJava.valueOf(CouponStatus.class, "state", state);
        if (value != null) {
            return ValueOfJava.valueOf(CouponStatus.class, "state", state);
        } else {
            return CouponStatus.BUY;
        }
    }

}
