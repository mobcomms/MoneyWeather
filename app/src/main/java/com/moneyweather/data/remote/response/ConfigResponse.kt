package com.moneyweather.data.remote.response

data class ConfigResponse(var result: Int, var msg: String, var data: Data) {
    data class Data(
        var themeId: Int,
        var useLockScreen: Boolean,
        var useVibrate: Boolean,
        var allowPush: Boolean
    )
}