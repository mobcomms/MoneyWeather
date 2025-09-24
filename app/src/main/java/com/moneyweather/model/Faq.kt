package com.moneyweather.model

class Faq {

    var faqId: Int? = -1
    var title: String? = ""
    var content: String? = ""
    var createdAt: String? = ""
    var category: Int? = 0
    var categoryDesc: String? = ""


    override fun toString(): String {
        return "Faq(faqId=$faqId, title=$title,content=$content,createdAt=$createdAt)"
    }
}