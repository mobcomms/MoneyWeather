package com.moneyweather.social.login;

import android.os.AsyncTask;

import com.moneyweather.util.Logger;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class NaverProfileTask extends AsyncTask<String, Void, String> {

    private NaverRequestCallback mNaverRequestCallback;
    private String result;

    public NaverProfileTask(NaverRequestCallback naverRequestCallback) {
        this.mNaverRequestCallback = naverRequestCallback;
    }

    @Override
    protected String doInBackground(String... strings) {
        String token = strings[0];// 네이버 로그인 접근 토큰
        String header = "Bearer " + token; // Bearer 다음에 공백 추가
        try {
            String apiURL = "https://openapi.naver.com/v1/nid/me";
            URL url = new URL(apiURL);
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("GET");
            con.setRequestProperty("Authorization", header);
            int responseCode = con.getResponseCode();
            BufferedReader br;
            if (responseCode == 200) { // 정상 호출
                br = new BufferedReader(new InputStreamReader(con.getInputStream()));
            } else {  // 에러 발생
                br = new BufferedReader(new InputStreamReader(con.getErrorStream()));
            }
            String inputLine;
            StringBuffer response = new StringBuffer();
            while ((inputLine = br.readLine()) != null) {
                response.append(inputLine);
            }
            result = response.toString();
            br.close();
        } catch (Exception e) {
            Logger.e(e.getMessage());
        }
        //result 값은 JSONObject 형태로 넘어옵니다.
        return result;
    }

    @Override
    protected void onPostExecute(String s) {
        super.onPostExecute(s);
        try {
            JSONObject object = new JSONObject(result);
            mNaverRequestCallback.result(object);
//            if (TextUtils.equals(object.getString("resultcode"), "00")) {
////                    String token = MyApplication.getInstance().getNaverLoginModule().getAccessToken(getApplicationContext());
////                    PreferencesUtil.getInstance().putString(Key.KEY_ACCESS_TOKEN, token);
//                JSONObject jsonObject = new JSONObject(object.getString("response"));
//                if (jsonObject.has("email")) {
//                    mSnsEmail = jsonObject.getString("email");
//                }
//                if (jsonObject.has("name")) {
//                    mSnsNickname = jsonObject.getString("name");
//                }
//                if (jsonObject.has("mobile")) {
//                    mSnsPhoneNum = jsonObject.getString("mobile").replaceAll("-", "");
//                }
//                serviceLogin(SignUpType.NAVER.getType());
//                Logger.v("naver login result : " + jsonObject.toString());
//            }
        } catch (Exception e) {
//            unLink(SignUpType.NAVER.getType());
            mNaverRequestCallback.fail(e.getMessage());
        }
    }
}
