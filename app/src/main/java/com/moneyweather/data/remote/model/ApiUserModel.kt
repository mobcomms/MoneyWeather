package com.moneyweather.data.remote.model

import com.moneyweather.data.remote.request.MobOnDongDongRewardRequest
import com.moneyweather.data.remote.request.PointSoundVibrateRequest
import com.moneyweather.data.remote.response.AnicResponse
import com.moneyweather.data.remote.response.AppVersionResponse
import com.moneyweather.data.remote.response.AuthResponse
import com.moneyweather.data.remote.response.AvailablePointResponse
import com.moneyweather.data.remote.response.BaseResponse
import com.moneyweather.data.remote.response.ConfigInitResponse
import com.moneyweather.data.remote.response.ConfigResponse
import com.moneyweather.data.remote.response.CouponDetailResponse
import com.moneyweather.data.remote.response.CouponResponse
import com.moneyweather.data.remote.response.DailyAdPointResponse
import com.moneyweather.data.remote.response.FaqDetailResponse
import com.moneyweather.data.remote.response.FaqResponse
import com.moneyweather.data.remote.response.FindEmailResponse
import com.moneyweather.data.remote.response.HolidayResponse
import com.moneyweather.data.remote.response.InviteResponse
import com.moneyweather.data.remote.response.LockScreenBannerPointResponse
import com.moneyweather.data.remote.response.LockScreenResponse
import com.moneyweather.data.remote.response.NewsResponse
import com.moneyweather.data.remote.response.NoticeDetailResponse
import com.moneyweather.data.remote.response.NoticeResponse
import com.moneyweather.data.remote.response.PointHistoryResponse
import com.moneyweather.data.remote.response.PomissionZoneUrlResponse
import com.moneyweather.data.remote.response.ProductResponse
import com.moneyweather.data.remote.response.QaDetailResponse
import com.moneyweather.data.remote.response.QaListResponse
import com.moneyweather.data.remote.response.RegionResponse
import com.moneyweather.data.remote.response.ScreenPopupsActiveResponse
import com.moneyweather.data.remote.response.ShopCategoryResponse
import com.moneyweather.data.remote.response.ShopProductListResponse
import com.moneyweather.data.remote.response.TermsResponse
import com.moneyweather.data.remote.response.TokenResponse
import com.moneyweather.data.remote.response.UserPointResponse
import com.moneyweather.data.remote.response.UserPointSoundVibrateResponse
import com.moneyweather.data.remote.response.UserPushAgreeResponse
import com.moneyweather.data.remote.response.UserResponse
import com.moneyweather.data.remote.response.VerificationResponse
import com.moneyweather.data.remote.response.WeatherResponse
import io.reactivex.Single
import org.json.JSONObject
import retrofit2.Response

interface ApiUserModel {
    fun post(url: String, m: Map<String, Any>): Single<JSONObject>
    fun userPhotoUpload(params: String): Single<BaseResponse>
    fun userInfo(): Single<UserResponse>
    fun userPoint(): Single<UserPointResponse>
    fun regionListInfo(latitude: String, longitude: String): Single<RegionResponse>
    fun getPointSoundVibrate(): Single<Response<UserPointSoundVibrateResponse>>
    fun updatePointSoundVibrate(request: PointSoundVibrateRequest): Single<Response<Unit>>

    fun refreshSession(): Single<TokenResponse>
    fun signUpGuest(m: HashMap<String, Any?>): Single<TokenResponse>
    fun signUpUser(m : HashMap<String, Any?>): Single<TokenResponse>
    fun login(m : HashMap<String, Any?>): Single<TokenResponse>
    fun logOut(): Single<BaseResponse>
    fun withdraw(): Single<BaseResponse>

    fun getCategoryList() : Single<ShopCategoryResponse>
    fun getProductList(category : String, affiliate : String, search : String,showCount : Int, page : Int, type : Boolean) : Single<ShopProductListResponse>
    fun getProductDetail(pk : String) : Single<ProductResponse>
    fun purchaseCoupon(m: HashMap<String, Any?>) : Single<BaseResponse>
    fun updateCouponState(couponId : Int) : Single<BaseResponse>
    fun getAuth(phone : String): Single<AuthResponse>
    fun modifyAuth(m: HashMap<String, Any?>): Single<BaseResponse>
    fun getCouponList(m: HashMap<String, Any?>): Single<CouponResponse>
    fun getCouponDetail(pk : Int): Single<CouponDetailResponse>
    fun couponRefund(m: HashMap<String, Any?>): Single<BaseResponse>
    fun getPrivacy(): Single<TermsResponse>
    fun getTerms(): Single<TermsResponse>
    fun getInviteInfo(): Single<InviteResponse>

    fun noticeListInfo(page: Int,showCount: Int): Single<NoticeResponse>
    fun noticeDetail(noticeId: Int): Single<NoticeDetailResponse>
    fun qaListInfo(): Single<QaListResponse>
    fun qaWrite(m: HashMap<String, Any?>): Single<BaseResponse>
    fun qaDetail(pk : Int): Single<QaDetailResponse>
    fun faqListInfo(category : String): Single<FaqResponse>
    fun faqDetail(pk : Int): Single<FaqDetailResponse>

    fun notiWeather(latitude: String, longitude: String): Single<WeatherResponse>
    fun currentWeather(latitude: String, longitude: String): Single<WeatherResponse>
    fun hourlyWeather(latitude: String, longitude: String, size: Int): Single<WeatherResponse>
    fun regionsWeather(latitude: String, longitude: String): Single<WeatherResponse>
    fun weeklyWeather(latitude: String, longitude: String): Single<WeatherResponse>
    fun appVersion(): Single<AppVersionResponse>
    fun verification(): Single<VerificationResponse>
    fun configInit(): Single<ConfigInitResponse>
    fun newsFeeds(): Single<NewsResponse>
    fun pointSave(point : Int) : Single<BaseResponse>
    fun pointHistory(page: Int,showCount: Int,type: Int) : Single<PointHistoryResponse>
    fun pointAvailable() : Single<AvailablePointResponse>
    fun findEmail(m : HashMap<String, Any?>) : Single<FindEmailResponse>
    fun sendEmailCode(m : HashMap<String, Any?>) : Single<BaseResponse>
    fun codeVerify(code : String) : Single<BaseResponse>
    fun sendNewPass(m : HashMap<String, Any?>) : Single<BaseResponse>
    fun configMy(m : HashMap<String, Any?>) : Single<BaseResponse>
    fun configMy() : Single<ConfigResponse>
    fun inviteRedeem(m : HashMap<String, Any?>) : Single<BaseResponse>
    fun inviteRedeemSocial(m: HashMap<String, Any?>): Single<BaseResponse>

    fun updateFcmToken(token: String): Single<Response<Unit>>

    fun infoLockScreen(m: HashMap<String, Any?>): Single<LockScreenResponse>
    fun simpleLockScreen(m: HashMap<String, Any?>): Single<LockScreenResponse>
    fun getHolidays() : Single<HolidayResponse>
    fun poMissionAuto(m: HashMap<String, Any?>): Single<BaseResponse>
    fun anic(): Single<AnicResponse>
    fun anic(m: HashMap<String, Any?>): Single<Any>
    fun mobonReward(): Single<Any>
    fun screenPopupsActive(): Single<ScreenPopupsActiveResponse>
    fun newsReward(guid: String): Single<Any>
    fun dailyAdPoint(): Single<DailyAdPointResponse>
    fun dailyAdPoint(m: HashMap<String, Any?>): Single<Any>
    fun lockscreenLandingEvent(): Single<LockScreenResponse>
    fun getPushAgree(): Single<UserPushAgreeResponse>
    fun updatePushAgree(m: Map<String, String>): Single<Response<Unit>>
    fun getLockScreenBottomBannerPoint(): Single<LockScreenBannerPointResponse>
    fun saveLockScreenBottomBannerPoint(m: Map<String, Int>): Single<Response<Unit>>
    fun saveLockScreenCoupangCpsPoint(): Single<Response<Unit>>
    fun mobOnDongDongReward(param: MobOnDongDongRewardRequest): Single<Response<Unit>>
    fun getPomissionZoneUrl(): Single<PomissionZoneUrlResponse>
    fun saveLockScreenPomissionZonePoint(): Single<Response<Unit>>
}