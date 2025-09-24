package com.moneyweather.model.enums

object ApiResultCode {
    const val SUCCESS = 0   //성공

    /**
     * 공통 100X
     */
    const val NULL_EXCEPTION = 1001 // NULL 포인트 오류"
    const val SQL_EXCEPTION = 1002 // 데이터베이스 오류"
    const val ARRAY_EXCEPTION = 1003 // 배열 인덱스 오류"
    const val FILE_EXCEPTION = 1004 // 파일 업로드 오류"
    const val IO_EXCEPTION = 1005 // IO 오류"
    const val PARSE_EXCEPTION = 1006 // 파싱 오류"
    const val MAPPING_EXCEPTION = 1007 // 매핑 오류"
    const val METHOD_TYPE_EXCEPTION = 1008 // Method Type 오류
    const val ACCESS_EXCEPTION = 1009 // 접근권한 오류

    /**
     * 공통 Custom 110X
     */
    const val TOKEN_EXCEPTION = 1101 // 토큰 오류
    const val NOT_DATA_EXCEPTION = 1102 // 파라미터 값 오류
    const val BAD_DATA_TOKEN_EXCEPTION = 1103 // 파라미터 형식 오류

    /**
     * 회원 관련 20XX
     */
    const val NONE_USER_INFO = 2001  // 회원 정보가 없습니다.
    const val SECESSION_MEMBER = 2002  // 탈퇴한 회원입니다.
    const val NEED_RE_LOGIN = 2003  // 다시 로그인 해주시기 바랍니다.

    const val RESTRICTED_ADID = 2011  // 가입이 제한된 ADID 입니다.
    const val RESTRICTED_EMAIL = 2012  // 가입이 제한된 이메일 입니다.
    const val RESTRICTED_PHONE = 2013  // 가입이 제한된 핸드폰 입니다.


    const val ALREADY_REGISTERED_GOOGLE = 2021  // 이미 구글로 가입한 회원 정보가 있습니다.
    const val ALREADY_REGISTERED_FACEBOOK = 2022  // 이미 페이스북으로 가입한 회원 정보가 있습니다.
    const val ALREADY_REGISTERED_KAKAO = 2023  // 이미 카카오로 가입한 회원 정보가 있습니다.
    const val ALREADY_REGISTERED_NAVER = 2024  // 이미 네이버로 가입한 회원 정보가 있습니다.

    const val USE_ONLY_MEMBER = 2031  // 회원만 이용할 수 있습니다.
    const val CHECK_RECOMMEND_CODE = 2041  // 추천인 코드를 다시 확인해주세요.

    /**
     * 포인트 관련 300X
     */
    const val NONE_POINT_INFO = 3001  // 포인트 정보가 없습니다.
    const val POINT_ERROR2 = 3002  // 포인트 에러2

    /**
     * 스토어 관련 400X
     */
    const val NOT_EXIST_GOODS = 4001  // 존재하지 않는 상품입니다.
    const val NOT_EXIST_COUPON = 4002  // 존재하지 않는 쿠폰입니다.
    const val NOT_ENOUGH_POINT = 4003  // 포인트가 부족합니다.
    const val NOT_ENABLED_SELL_GOODS = 4004  // 상품 판매가 불가능한 상품입니다.
    const val FAILED_BUY_COUPON = 4005  // 상품 구매 등록이 실패하였습니다.
    const val NOT_ENABLED_REFUND_COUPON = 4006  // 쿠폰 환불이 불가능한 쿠폰입니다.
    const val FAILED_REFUND_COUPON = 4007  // 쿠폰 환불 등록이 실패하였습니다.



    /**
     * 출금 관련 500X
     */
    const val WITHDRAW_ERROR1 = 5001  // 출금 에러1
    const val WITHDRAW_ERROR2 = 5002  // 출금 에러2

    /**
     * 락스크린 관련 600X
     */
    const val LOCKSCREEN_ERROR1 = 6001  // 락스크린 에러1
    const val LOCKSCREEN_ERROR2 = 6002  // 락스크린 에러2

    /**
     * 게시판 관련 (공지사항, FAQ, 문의하기 등) 70XX
     */
    const val NO_DATA_NOTICE = 7001  // 존재하지 않는 공지사항입니다.
    const val NO_DATA_FAQ = 7011  // 존재하지 않는 FAQ입니다.
    const val NO_DATA_INQUIRY = 7021  // 존재하지 않는 문의입니다.
    const val NO_DATA_EVENT = 7031  // 존재하지 않는 이벤트입니다.

    const val SYSTEM_WORK = 9999 //시스템 점검중

    const val SYSTEM_MESSAGE = 2052 //서버 다이얼 로그 메세지
    const val SYSTEM_PHONEAUTH_ERROR = 2051 //본인인증 안한 회원
}
