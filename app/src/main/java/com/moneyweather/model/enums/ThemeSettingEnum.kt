package com.moneyweather.model.enums

enum class ThemeSettingEnum {
    INFORMATION            /*  0 */,
    SIMPLE          /*  1 */,
    CALENDAR     /*  2 */,
    BACKGROUND     /*  3 */,
    ;

    companion object {
        fun parserToEnum(value: Int): ThemeSettingEnum {
            return values()[value]
        }
    }
}