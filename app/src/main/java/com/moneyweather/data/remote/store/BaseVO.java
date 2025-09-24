package com.moneyweather.data.remote.store;

import org.apache.commons.lang3.builder.ToStringBuilder;

import java.io.Serializable;

public class BaseVO implements Serializable {

    private int result; // 결과 코드 ( @see ResultCode )
    private String msg; // 에러 메시지
    private String token; // 토큰
    private String refresh;  // 재발행요청 토큰
    private String key; // 비회원 유저키
    private String login; // 로그인 여부
    //private String type; // 로그인 타입

    public int getResult() {
        return result;
    }

    public void setResult(int result) {
        this.result = result;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getRefresh() {
        return refresh;
    }

    public void setRefresh(String refresh) {
        this.refresh = refresh;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }
}
