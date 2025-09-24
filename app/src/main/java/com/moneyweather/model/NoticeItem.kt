package com.moneyweather.model

class NoticeItem {
    var pk: Int? = -1
    var title: String? = ""
    var content: String? = ""
    var reg_date: String? = ""

    override fun toString(): String {
        return "RegionItem(pk=$pk, title=$title,content=$content,reg_date=$reg_date)"
    }
}