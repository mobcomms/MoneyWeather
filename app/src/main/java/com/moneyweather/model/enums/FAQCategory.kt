package com.moneyweather.model.enums

import com.google.gson.annotations.SerializedName

enum class FAQCategory(value : String) {
    @SerializedName("")
    ALL("")      /* 0 */,
    @SerializedName("1")
    SAVE("1")      /* 1 */,
    @SerializedName("2")
    WITHDRAW("2")     /* 2 */,
    @SerializedName("3")
    COUPON("3") /* 3 */,
    @SerializedName("4")
    OTHER("4") /* 4 */,
    ;


    companion object {
        fun parserToString(value: Int): String {
            if (value > 0 && value < values().size)
                return values()[value].ordinal.toString()
            else
                return ""
        }
    }
}