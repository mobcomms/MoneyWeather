package com.moneyweather.model

class Notice {

    var noticeId: Int? = -1
    var isRead: Boolean? = false
    var title: String? = ""
    var content: String? = ""
    var createdAt: String? = ""

    override fun toString(): String {
        return "Notice(noticeId=$noticeId isRead=$isRead title=$title content=$content createdAt=$createdAt)"
    }
}