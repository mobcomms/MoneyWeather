package com.moneyweather.extensions

import android.content.Context
import com.tnkfactory.ad.TNK_POINT_EFFECT_TYPE
import com.tnkfactory.ad.TnkAdConfig
import com.tnkfactory.ad.TnkOfferwall

fun createTnkOfferwall(
    context: Context,
    userId: String
) = TnkOfferwall(context).apply {
    settings(userId)
}

private fun TnkOfferwall.settings(userId: String) {
    // 유저 식별값 설정
    setUserName(userId)

    // COPPA 설정 (https://www.ftc.gov/business-guidance/privacy-security/childrens-privacy)
    setCOPPA(false)

    // 포인트 금액 앞에 아이콘, 뒤에 재화 단위 출력 여부를 설정합니다.
    // 금액 뒤에 관리자 페이지에서 설정한 단위 출력
    TnkAdConfig.pointEffectType = TNK_POINT_EFFECT_TYPE.UNIT

    getEarnPoint { }
}

fun TnkOfferwall.openOfferwall(context: Context) {
    startOfferwallActivity(context)
}

fun TnkOfferwall.landingPage(context: Context, category: Int, filter: Int) {
    TnkAdConfig.headerConfig.apply {
        startCategory = category
        startFilter = filter
    }
    openOfferwall(context)
}