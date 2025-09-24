package com.moneyweather.model.enums;

import org.jetbrains.annotations.NotNull;

/**
 * HTTP Status Code
 */
public enum ResultCode {

    SUCCESS(0), // 성공
    TOKEN_ERROR(1), // 토큰 에러
    QUERY_ERROR(2), // 쿼리 에러
    NO_DATA(3), // 데이터 없음
    DUPLICATE_AD_ID(4), // 이미 등록된 ADID
    DUPLICATE_EMAIL(5), // 이미 등록된 이메일
    ALREADY_REGISTERED_EMAIL(7), // 이미 가입된 회원
    WRONG_LOG_INFO(8), // 이메일 또는 비밀번호 오류
    WRONG_RECOMMEND_CODE(9), // 추천인 코드 오류
    SECESSION_USER(10), // 탈퇴한 회원
    WRONG_DATA(11), // 잘못된 데이터
    ALREADY_LIKE_SAYING(13), // "이미 좋아요 한 명언
    SMS_AUTH_TIMEOUT(20), // SMS 인증시간 초과
    SMS_AUTH_WRONG_DATA(21), // SMS 인증코드 잘못된 데이터
    SMS_AUTH_DUPLICATE_PHONE(22), // 이미 존재하는 핸드폰 번호
    USER_ONLY_USE(23), // 회원이 아님
    MAIL_SEND_FAIL(24), // 메일 발송이 실패
    DUPLICATION_REQUEST(25), // 잠시 후 다시 시도
    IMPOSSIBLE_SALE_GOODS(26), // 상품 판매가 불가능한 상품입니다
    COUPON_WEEK_2_LIMIT(27), // 쿠폰은 한 주에 2번만 구매 가능합니다
    GIFT_CARD_HOUR_1_LIMIT(28), // 상품권은 1시간 이내 1번만 구매 가능합니다
    GIFT_CARD_DAILY_10_LIMIT(29), // 상품권은 하루에 전체 구매건수가 10회로 제한
    COUPON_DAILY_200000_LIMIT(30), // 쿠폰은 하루에 전체 구매금액이 20만원으로 제한됩니다
    CASH_LACK(31), // 적립금이 부족합니다
    COUPON_BUY_FAILURE(32), // 쿠폰 구매 등록이 실패하였습니다
    IMPOSSIBLE_CANCEL_COUPON(33), // 쿠폰 환불이 불가능한 쿠폰입니다
    COUPON_CANCEL_FAILURE(34), // 쿠폰 환불 등록이 실패하였습니다
    NO_AUTH(42), // 본인인증을 하지 않은 회원입니다.
    AUTH_AND_DEPOSITOR_NO_MATCH(43), // 본인인증 정보와 예금주 정보가 일치하지 않습니다.
    DUPLICATE_AUTH(55), // 본인인증 정보와 예금주 정보가 일치하지 않습니다.

    OVER_PUSH_MESSAGE_LIMIT(49),
    DUPLICATED_PUSH_MESSAGE_ID(50), // 이미 적립한 알림톡 메시지 입니다.
    SYSTEM_WORK(99), // 시스템 정비중
    DUPLICATE_USER(1207), // 이미 가입된 유저
    DUPLICATE_ACCOUNT(1210), //등록된 계정이 있음
    SESSION_EXPIRED(1101), // 세션 만료
    SESSION_WRONG(1102), // refreshSession 오류
    DUPLICATE_PHONE_NUMBER(1208), //이미 등록된 전화번호
    INVITE_CODE_FAIL(1401), //잘못된 초대코드

    NO_VERIFIED(1104), // 본인인증 안됨
    NO_SEARCH_USER(1202), //찾을 수 없는 회원

    ;

    private final int resultCode;

    ResultCode(int resultCode) {
        this.resultCode = resultCode;
    }

    public int getResultCode() {
        return resultCode;
    }

    @NotNull
    public static ResultCode toCode(int code) {
        for (ResultCode value : values()) {
            if (value.resultCode == code)
                return value;
        }
        return null;
    }
}
