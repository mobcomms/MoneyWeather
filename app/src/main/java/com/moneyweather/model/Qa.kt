package com.moneyweather.model

class Qa {

    var inquiryId: Int? = -1
    var title: String? = ""
    var isReplied: Boolean? = false
    var createdAt: String? = ""

    override fun toString(): String {
        return "Qa(inquiryId=$inquiryId, title=$title,isReplied=$isReplied,createdAt=$createdAt)"
    }
}