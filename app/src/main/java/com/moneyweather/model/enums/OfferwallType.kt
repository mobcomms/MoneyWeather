package com.moneyweather.model.enums

import com.moneyweather.R

enum class OfferwallEnum(
    val landingEventId: Int,
    val category: Int,
    val filter: Int,
    val buttonImage: Int
) {
    TNK_VIDEO(1, 1, 102, R.drawable.ic_tnk_video),
    TNK_QUIZ(2, 1, 203, R.drawable.ic_tnk_quiz),
    TNK_NEWS(3, 1, 101, R.drawable.ic_tnk_news);

    companion object {
        fun getLandingPage(landingEventId: Int): OfferwallEnum? = values().find {
            it.landingEventId == landingEventId
        }

        fun getLandingImage(landingEventId: Int): Int? = getLandingPage(landingEventId)?.buttonImage

    }
}