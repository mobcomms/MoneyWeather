package com.moneyweather.model.enums


enum class WeatherType(val type: Int) {
    CLEAN(1),  // 맑음
    CLOUDY(2),  // 구름조금
    CLOUD(3),  // 흐림
    RAIN(4),  // 비
    SNOW(5),  // 눈
    SNOW_RAIN(6),  //눈비
    SHOWER(7),  // 소나기
    SHOWER_SNOW(8),  //소낙눈
    SMOG(9),  // 안개
    THUNDER(10),  //뇌우
    GRADUALLY_CLOUDY(11),  // 차차 흐려짐
    CLOUDY_THUNDER(12),  //흐려져 뇌우
    CLOUDY_RAIN(13),  // 흐려져 비
    CLOUDY_SNOW(14),  // 흐려져 눈
    CLOUDY_SNOW_RAIN(15),  // 흐려져 눈비
    CLOUDY_CLEAN(16),  // 흐린 후 갬
    THUNDER_CLEAN(17),  // 뇌우 후 갬
    RAIN_CLEAN(18),  // 비후 갬
    SNOW_CLEAN(19),  // 눈 후 갬
    SNOW_RAIN_CLEAN(20),  // 눈비 후 갬
    CLOUDY_MANY(21),  // 구름많음
    DUST_STORM(22);  // 황사

    companion object {
        fun fromInt(value: Int) = WeatherType.values().first { it.type == value }
    }
}