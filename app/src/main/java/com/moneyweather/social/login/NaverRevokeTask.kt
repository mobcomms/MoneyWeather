package com.moneyweather.social.login

import android.content.Context
import android.os.AsyncTask
import com.moneyweather.base.BaseApplication
import com.moneyweather.util.Logger
import com.navercorp.nid.NaverIdLoginSDK
import com.navercorp.nid.oauth.NidOAuthLogin
import com.navercorp.nid.oauth.OAuthLoginCallback

/**
 * 네이버 로그인 권한 해제
 */
class NaverRevokeTask(private val mNaverRequestCallback: NaverRequestCallback) :
    AsyncTask<Context?, Void?, Boolean?>() {

    override fun onPostExecute(result: Boolean?) {
        super.onPostExecute(result)
        if (result != null) {
            Logger.v("naver login revoke result $result")
            mNaverRequestCallback.result(result)
        } else {
            mNaverRequestCallback.fail("fail")
        }
    }

    override fun doInBackground(vararg p0: Context?): Boolean? {
        NaverIdLoginSDK.logout()

        NidOAuthLogin().callDeleteTokenApi(BaseApplication.appContext(),object : OAuthLoginCallback {
            override fun onSuccess() {
                //서버에서 토큰 삭제에 성공한 상태입니다.

            }
            override fun onFailure(httpStatus: Int, message: String) {
                // 서버에서 토큰 삭제에 실패했어도 클라이언트에 있는 토큰은 삭제되어 로그아웃된 상태입니다.
                // 클라이언트에 토큰 정보가 없기 때문에 추가로 처리할 수 있는 작업은 없습니다.

            }
            override fun onError(errorCode: Int, message: String) {
                // 서버에서 토큰 삭제에 실패했어도 클라이언트에 있는 토큰은 삭제되어 로그아웃된 상태입니다.
                // 클라이언트에 토큰 정보가 없기 때문에 추가로 처리할 수 있는 작업은 없습니다.
                onFailure(errorCode, message)
            }
        })
        return true
    }
}