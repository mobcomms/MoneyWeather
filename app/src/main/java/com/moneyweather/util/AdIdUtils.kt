package com.moneyweather.util

import java.text.SimpleDateFormat
import java.util.*

object AdIdUtils {
    fun convertToValidADID(_adid: String?): String{
        val adid = _adid?:"0"

        return if (isValidAdId(adid)){
            adid
        } else{
            val currentAdId = PrefRepository.UserInfo.adid
            if (isValidAdId(currentAdId)){
                currentAdId
            } else{
                makeTimeStampUUID()
            }
        }
    }

    private fun makeTimeStampUUID(): String{
        val timeStamp = SimpleDateFormat("yyyyMMddHHmmssSSS", Locale.KOREA).format(Date())

        var uuidStr = UUID.randomUUID().toString().replace("-", "")
        uuidStr = "${uuidStr.substring(0, 10)}-$timeStamp"

        Logger.d("jhl_log", "makeTimeStampUUID : $uuidStr")

        return uuidStr
    }

    fun isValidAdId(adid: String): Boolean{
        return if (adid.isBlank()) false
        else adid.replace("-", "").toIntOrNull() != 0

    }
}