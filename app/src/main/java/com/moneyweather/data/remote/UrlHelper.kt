package com.moneyweather.data.remote

import com.moneyweather.BuildConfig

object UrlHelper {

    /**
     * donsee
     */
    private const val DOMAIN_DEBUG = "https://stage-api.donsee.co.kr"
    private const val DOMAIN_RELEASE = "https://api.donsee.co.kr"
    val DOMAIN = if (BuildConfig.DEBUG) DOMAIN_DEBUG else DOMAIN_RELEASE
    const val API_VERSION = "1"

    /**
     * pomission
     */
    const val POMISSION_DOMAIN = "https://api.pomission.com"
    const val POMISSION_API_VERSION = "1"

    /**
     * anic
     */
    const val ANIC_DOMAIN = "https://moneyweather-api.commsad.com"

    /**
     * shoplus
     */
    const val SHOPLUS_DOMAIN = "https://app.shoplus.io/index.php"

    /**
     * mobon
     */
    const val MOBON_DOMAIN = "https://mediacategory.com"

    /**
     * mobwith
     */
    const val MOBWITH_DOMAIN = "https://www.mobwithad.com"
}