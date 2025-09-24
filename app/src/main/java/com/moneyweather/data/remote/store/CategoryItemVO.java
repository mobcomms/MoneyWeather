package com.moneyweather.data.remote.store;

import java.util.List;

public class CategoryItemVO extends BaseVO {

    private String category;
    private List<AffiliateVO> affiliate;

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public List<AffiliateVO> getAffiliate() {
        return affiliate;
    }

    public void setAffiliate(List<AffiliateVO> affiliate) {
        this.affiliate = affiliate;
    }
}