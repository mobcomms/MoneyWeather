package com.moneyweather.model.enums

import com.moneyweather.util.ValueOf

/**
 * 가입 유형
 */
enum class SignUpType(val type: String, val sns: String) {
    NON_MEMBER("", ""),  // 미가입 회원
    SERVICE("1", ""),  // 자체 로그인
    GOOGLE("2", "구글"),  // 구글 로그인
    KAKAO("3", "카카오"),  // 카카오 로그인
    NAVER("4", "네이버") // 네이버 로그인
    ;

    companion object {

        fun setTypeByErrorCode(errorCode: Int) : String{
            return when(errorCode){
                ApiResultCode.ALREADY_REGISTERED_GOOGLE -> GOOGLE.type
                ApiResultCode.ALREADY_REGISTERED_KAKAO -> KAKAO.type
                ApiResultCode.ALREADY_REGISTERED_NAVER -> NAVER.type
                else -> NON_MEMBER.type
            }
        }

        @JvmStatic
        fun find(type: String): SignUpType {
            if (type.isNullOrEmpty()) {
                return NON_MEMBER
            }
            val value = ValueOf.valueOf(SignUpType::class.java, "type", type)
            return if (value != null) {
                ValueOf.valueOf(SignUpType::class.java, "type", type)
            } else {
                NON_MEMBER
            }
        }
    }
}