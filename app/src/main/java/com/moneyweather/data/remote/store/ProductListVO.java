package com.moneyweather.data.remote.store;

import java.util.List;

public class ProductListVO extends BaseVO {

    private int count;
    private List<ProductItemVO> goods;

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public List<ProductItemVO> getGoods() {
        return goods;
    }

    public void setGoods(List<ProductItemVO> goods) {
        this.goods = goods;
    }
}
