package com.moneyweather.model

class RegionItem {
    var id: Int? = -1
    var name: String? = ""
    var mintemp: Int? = 0
    var hightemp: Int? = 0
    var temp: Int? = 0
    var dust: String? = ""
    var status: String? = ""


    override fun toString(): String {
        return "RegionItem(id=$id, name=$name,mintemp=$mintemp,hightemp=$hightemp,dust=$dust,temp=$temp,status=$status)"
    }
}