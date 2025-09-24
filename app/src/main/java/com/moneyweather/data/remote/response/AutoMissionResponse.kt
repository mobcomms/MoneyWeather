package com.moneyweather.data.remote.response

import android.os.Parcelable

@kotlinx.parcelize.Parcelize
data class AutoMissionResponse(
    var result: Int,
    var auto: ArrayList<Auto>,
    var mission: ArrayList<Mission>
) : Parcelable {

    @kotlinx.parcelize.Parcelize
    data class Auto(
        val src: String?,
        val mission_class: String?,
        val landing: String?,
        val media_id: String?,
        val script: String?,
        val auto_yn: String?,
        val timer: Int?
    ) : Parcelable

    @kotlinx.parcelize.Parcelize
    data class Mission(
        val mission_seq: Int?,
        val mission_id: String?,
        val daily_participation_cnt: Int?,
        val daily_participation: Int?,
        val adver_name: String?,
        val adver_url: String?,
        val keyword: String?,
        val intro_img: String?,
        val thumb_img: String?,
        val reg_date: String?,
        val media_point: String?,
        val user_point: String?,
        var mission_class: String?,
        val check_url: String?,
        val check_url2: String?,
        val check_time: Int?,
        val p_search_nm: String?,
        val p_no: String?,
        val p_no2: String?,
        val p_no3: String?,
        val p_syncnvmid: String?
    ) : Parcelable
}