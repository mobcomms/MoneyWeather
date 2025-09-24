package com.moneyweather.data.remote.response

data class AppVersionResponse(
    val result: Int,
    val msg: String,
    val data: Data
) {
    data class Data(
        val updateType: String?,
        val latestVersion: String?,
        val minSupportedVersion: String?
    )
}