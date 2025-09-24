package com.moneyweather.data.remote.store;

public class CouponItemVO extends BaseVO {

//    private int result; // 결과 코드 ( @see ResultCode )
//    private String msg; // 에러 메시지

    private int pk; // 쿠폰 리스트 pk
    private String tr_id;
    private String pin_no;
    private String goods_id; // 상품 아이디
    private String goods_img; // 상품 이미지
    private String goods_nm; // 상품 이름
    private String affiliate; // 판매처
    private int total_price; // 상품 가격
    private int limit_date; // 유효기간 @see CouponStatus
    private int state; // 쿠폰 상태
    private String goods_desc; // 상품 설명
    private String note; // 유의사항
    private String end_date;

//    public int getResult() {
//        return result;
//    }
//
//    public void setResult(int result) {
//        this.result = result;
//    }
//
//    public String getMsg() {
//        return msg;
//    }
//
//    public void setMsg(String msg) {
//        this.msg = msg;
//    }

    public int getPk() {
        return pk;
    }

    public void setPk(int pk) {
        this.pk = pk;
    }

    public String getPin_no() {
        return pin_no;
    }

    public void setPin_no(String pin_no) {
        this.pin_no = pin_no;
    }

    public String getGoods_id() {
        return goods_id;
    }

    public void setGoods_id(String goods_id) {
        this.goods_id = goods_id;
    }

    public String getGoods_img() {
        return goods_img;
    }

    public void setGoods_img(String goods_img) {
        this.goods_img = goods_img;
    }

    public String getGoods_nm() {
        return goods_nm;
    }

    public void setGoods_nm(String goods_nm) {
        this.goods_nm = goods_nm;
    }

    public String getAffiliate() {
        return affiliate;
    }

    public void setAffiliate(String affiliate) {
        this.affiliate = affiliate;
    }

    public int getTotal_price() {
        return total_price;
    }

    public void setTotal_price(int total_price) {
        this.total_price = total_price;
    }

    public String getTr_id() {
        return tr_id;
    }

    public void setTr_id(String tr_id) {
        this.tr_id = tr_id;
    }

    public int getLimit_date() {
        return limit_date;
    }

    public void setLimit_date(int limit_date) {
        this.limit_date = limit_date;
    }

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }

    public String getGoods_desc() {
        return goods_desc;
    }

    public void setGoods_desc(String goods_desc) {
        this.goods_desc = goods_desc;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public String getEnd_date() {
        return end_date;
    }

    public void setEnd_date(String end_date) {
        this.end_date = end_date;
    }
}
