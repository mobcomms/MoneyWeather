package com.moneyweather.model

class News {

    var title: String = ""
    var agency: String = ""
    var articleUrl: String = ""
    var guid: String? = ""

    override fun toString(): String {
        return "News(title=$title, agency=$agency, articleUrl=$articleUrl, guid=$guid)"
    }
}