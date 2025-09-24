package com.moneyweather.data.remote.service

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
import okhttp3.MultipartBody
import org.json.JSONObject
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.HTTP
import retrofit2.http.Headers
import retrofit2.http.Multipart
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query
import retrofit2.http.QueryMap


interface ApiUserService {
    @Headers("Content-Type: application/json")
    @POST("{path}/{value}")
    fun post(
        @Path(value = "path", encoded = true) path: String,
        @Path(value = "value", encoded = true) value: String,
        @Body params: JSONObject
    ): Single<JSONObject>

    @Headers("Content-Type: application/json")
    @POST("{path}")
    fun post(@Path(value = "path", encoded = true) path: String, @Body params: JSONObject): Single<JSONObject>


    @Multipart
    @POST("/api/public/upload/image")
    fun userPhotoUpload(@Part params: MultipartBody.Part): Single<BaseResponse>

    @GET("/api/v{version}/users/my")
    fun userInfo(@Path("version") version: String): Single<UserResponse>

    @GET("/api/v{version}/points")
    fun userPoint(@Path("version") version: String): Single<UserPointResponse>


    @GET("/api/public/email/check")
    fun emailCheck(@Query("userEmail") email: String): Single<BaseResponse>

    @Headers("Content-Type: application/json")
    @POST("/api/v1/sign/in/refresh")
    fun refreshSession(@Body body: HashMap<String, Any?>): Single<TokenResponse>

    @Headers("Content-Type: application/json")
    @POST("/api/v1/sign/up/guest")
    fun signUpGuest(@Body body: HashMap<String, Any?>): Single<TokenResponse>


    @GET("/api/v{version}/store/categories")
    fun getCategoryList(@Path("version") version: String): Single<ShopCategoryResponse>

    @GET("/api/v{version}/store/goods")
    fun getProductList(
        @Path("version") version: String,
        @Query("category") category: String,
        @Query("affiliate") affiliate: String,
        @Query("search") search: String,
        @Query("showCount") showCount: Int,
        @Query("page") page: Int,
        @Query("showNaverPayOnly") showNaverPayOnly: Boolean
    ): Single<ShopProductListResponse>

    @GET("/api/v{version}/store/goods/{pk}")
    fun getProductDetail(
        @Path("version") version: String,
        @Path("pk") pk: String
    ): Single<ProductResponse>

    @Headers("Content-Type: application/json")
    @POST("/api/v{version}/store/coupons")
    fun purchaseCoupon(
        @Path("version") version: String,
        @Body body: HashMap<String, Any?>
    ): Single<BaseResponse>

    @Headers("Content-Type: application/json")
    @PATCH("/api/v{version}/store/coupons/refresh/{couponId}")
    fun updateCouponState(
        @Path("version") version: String,
        @Path("couponId") couponId: Int
    ): Single<BaseResponse>

    @GET("api/v{version}/user/auth")
    fun getAuth(
        @Path("version") version: String,
        @Query("phone") phone: String
    ): Single<AuthResponse>

    @Headers("Content-Type: application/json")
    @POST("api/v{version}/verification")
    fun modifyAuth(
        @Path("version") version: String,
        @Body body: HashMap<String, Any?>
    ): Single<BaseResponse>


    @GET("api/v{version}/store/coupons")
    fun getCouponList(
        @Path("version") version: String,
        @QueryMap map: HashMap<String, Any?>
    ): Single<CouponResponse>


    @GET("api/v{version}/store/coupons/{couponId}")
    fun getCouponDetail(
        @Path("version") version: String,
        @Path("couponId") couponId: Int
    ): Single<CouponDetailResponse>

    @PUT("api/v{version}/store/coupons/refund")
    fun couponRefund(
        @Path("version") version: String,
        @Body body: HashMap<String, Any?>
    ): Single<BaseResponse>

    @GET("api/v{version}/policy/privacy")
    fun getPrivacy(
        @Path("version") version: String
    ): Single<TermsResponse>

    @GET("api/v{version}/policy/service")
    fun getTerms(
        @Path("version") version: String
    ): Single<TermsResponse>

    @GET("api/v{version}/invite/my")
    fun getInviteInfo(@Path("version") version: String): Single<InviteResponse>

    @GET("api/v{version}/notices")
    fun noticeListInfo(@Path("version") version: String, @Query("page") page: Int?, @Query("showCount") showCount: Int?): Single<NoticeResponse>

    @GET("api/v{version}/notices/{noticeId}")
    fun noticeDetail(@Path("version") version: String, @Path("noticeId") noticeId: Int): Single<NoticeDetailResponse>

    @GET("api/v{version}/inquiries")
    fun qaListInfo(@Path("version") version: String): Single<QaListResponse>

    @GET("api/v{version}/config/version")
    fun appVersion(@Path("version") version: String): Single<AppVersionResponse>

    @POST("api/v{version}/inquiries")
    fun qaWrite(
        @Path("version") version: String,
        @Body body: HashMap<String, Any?>
    ): Single<BaseResponse>

    @GET("api/v{version}/inquiries/{inquiiryId}")
    fun qaDetail(@Path("version") version: String, @Path("inquiiryId") pk: Int): Single<QaDetailResponse>


    @GET("api/v{version}/faqs")
    fun faqListInfo(@Path("version") version: String, @Query("category") category: String?): Single<FaqResponse>

    @GET("api/v{version}/faqs/{pk}")
    fun faqDetail(@Path("version") version: String, @Path("pk") pk: Int): Single<FaqDetailResponse>

    @Headers("Content-Type: application/json")
    @POST("/api/v{version}/sign/up/user")
    fun signUpUser(
        @Path("version") version: String,
        @Body body: HashMap<String, Any?>
    ): Single<TokenResponse>

    @Headers("Content-Type: application/json")
    @POST("/api/v{version}/sign/in")
    fun login(
        @Path("version") version: String,
        @Body body: HashMap<String, Any?>
    ): Single<TokenResponse>


    @HTTP(method = "DELETE", path = "/api/v{version}/sign/out", hasBody = true)
    fun logOut(
        @Path("version") version: String, @Body body: HashMap<String, Any?>
    ): Single<BaseResponse>

    @DELETE("/api/v{version}/users")
    fun withdraw(@Path("version") version: String): Single<BaseResponse>


    @GET("api/v{version}/screen/noti/weather")
    fun notiWeather(@Path("version") version: String, @Query("latitude") latitude: String, @Query("longitude") longitude: String): Single<WeatherResponse>

    @GET("/api/v{version}/weather/regions")
    fun regionListInfo(@Path("version") version: String, @Query("latitude") latitude: String, @Query("longitude") longitude: String): Single<RegionResponse>

    @GET("api/v{version}/weather/current")
    fun currentWeather(@Path("version") version: String, @Query("latitude") latitude: String, @Query("longitude") longitude: String): Single<WeatherResponse>

    @GET("api/v{version}/weather/hourly")
    fun hourlyWeather(
        @Path("version") version: String,
        @Query("latitude") latitude: String,
        @Query("longitude") longitude: String,
        @Query("size") size: Int
    ): Single<WeatherResponse>

    @GET("api/v{version}/weather/regions")
    fun regionsWeather(@Path("version") version: String, @Query("latitude") latitude: String, @Query("longitude") longitude: String): Single<WeatherResponse>

    @GET("api/v{version}/weather/weekly")
    fun weeklyWeather(@Path("version") version: String, @Query("latitude") latitude: String, @Query("longitude") longitude: String): Single<WeatherResponse>

    @GET("api/v{version}/verification")
    fun verification(@Path("version") version: String): Single<VerificationResponse>

    @GET("api/v{version}/config/init")
    fun configInit(@Path("version") version: String): Single<ConfigInitResponse>

    @GET("api/v{version}/newsfeeds")
    fun newsFeeds(@Path("version") version: String, @Query("newsSize") newsSize: Int): Single<NewsResponse>

    @Headers("Content-Type: application/json")
    @POST("/api/v{version}/points/click")
    fun pointSave(
        @Path("version") version: String,
        @Body body: HashMap<String, Any?>
    ): Single<BaseResponse>

    @GET("api/v{version}/points/histories")
    fun pointHistory(
        @Path("version") version: String,
        @Query("page") page: Int,
        @Query("showCount") showCount: Int,
        @Query("type") type: Int
    ): Single<PointHistoryResponse>

    /**
     * 사용 가능한 포인트
     */
    @GET("api/v{version}/points/available")
    fun pointAvailable(@Path("version") version: String): Single<AvailablePointResponse>

    @Headers("Content-Type: application/json")
    @POST("/api/v{version}/users/email/verify")
    fun findEmail(
        @Path("version") version: String,
        @Body body: HashMap<String, Any?>
    ): Single<FindEmailResponse>

    @Headers("Content-Type: application/json")
    @POST("/api/v{version}/users/password/reset/email")
    fun sendEmailCode(
        @Path("version") version: String,
        @Body body: HashMap<String, Any?>
    ): Single<BaseResponse>

    @GET("api/v{version}/users/password/reset/verify")
    fun codeVerify(@Path("version") version: String, @Query("resetCode") code: String): Single<BaseResponse>

    @Headers("Content-Type: application/json")
    @POST("/api/v{version}/users/password/reset")
    fun sendNewPass(
        @Path("version") version: String,
        @Body body: HashMap<String, Any?>
    ): Single<BaseResponse>

    @Headers("Content-Type: application/json")
    @PATCH("/api/v{version}/config/my")
    fun configMy(
        @Path("version") version: String,
        @Body body: HashMap<String, Any?>
    ): Single<BaseResponse>

    @Headers("Content-Type: application/json")
    @GET("/api/v{version}/config/my")
    fun configMy(@Path("version") version: String): Single<ConfigResponse>

    @Headers("Content-Type: application/json")
    @POST("/api/v{version}/invite/redeem")
    fun inviteRedeem(
        @Path("version") version: String,
        @Body body: HashMap<String, Any?>
    ): Single<BaseResponse>

    @Headers("Content-Type: application/json")
    @POST("/api/v{version}/invite/redeem/social")
    fun inviteRedeemSocial(
        @Path("version") version: String,
        @Body body: HashMap<String, Any?>
    ): Single<BaseResponse>

    @Headers("Content-Type: application/json")
    @GET("/api/v{version}/screen/lock/info")
    fun infoLockScreen(
        @Path("version") version: String,
        @QueryMap map: HashMap<String, Any?>
    ): Single<LockScreenResponse>

    @Headers("Content-Type: application/json")
    @GET("/api/v{version}/screen/lock/simple")
    fun simpleLockScreen(
        @Path("version") version: String,
        @QueryMap map: HashMap<String, Any?>
    ): Single<LockScreenResponse>

    @Headers("Content-Type: application/json")
    @GET("/api/v{version}/holidays/within-one-year")
    fun getHolidays(@Path("version") version: String): Single<HolidayResponse>

    @Headers("Content-Type: application/json")
    @POST("/api/v{version}/pomission/auto")
    fun poMissionAuto(
        @Path("version") version: String,
        @Body body: HashMap<String, Any?>
    ): Single<BaseResponse>

    /**
     * 에이닉 게임 참여 1차 신청
     *
     * @param version
     */
    @Headers("Content-Type: application/json")
    @POST("/api/v{version}/anic")
    fun anic(@Path("version") version: String): Single<AnicResponse>

    /**
     * 에이닉 포인트 적립 신청
     *
     * @param version
     * @param historyId
     * @param body
     */
    @Headers("Content-Type: application/json")
    @POST("/api/v{version}/anic/{historyId}")
    fun anic(
        @Path("version") version: String,
        @Path("historyId") historyId: Int,
        @Body body: HashMap<String, Any?>
    ): Single<Any>

    /**
     * 모비온 라이브 캠페인 조회 포인트 지급 요청
     *
     * @param version
     * @param sc
     */
    @Headers("Content-Type: application/json")
    @POST("/api/v{version}/mobon/reward/{sc}")
    fun mobonReward(@Path("version") version: String, @Path("sc") sc: String): Single<Any>

    /**
     * 공지사항 팝업 조회
     *
     * @param version
     */
    @Headers("Content-Type: application/json")
    @GET("/api/v{version}/screen/popups/active")
    fun screenPopupsActive(@Path("version") version: String): Single<ScreenPopupsActiveResponse>

    /**
     * 오아시스 뉴스 조회 리워드 지급 요청
     * @param version
     * @param guid
     */
    @Headers("Content-Type: application/json")
    @POST("/api/v{version}/news/reward/{guid}")
    fun newsReward(@Path("version") version: String, @Path("guid") guid: String): Single<Any>

    @Headers("Content-Type: application/json")
    @PATCH("/api/v{version}/users/fcm")
    fun usersFcm(
        @Path("version") version: String,
        @Body body: Map<String, String>
    ): Single<Response<Unit>>

    /**
     * 일별 광고 클릭 포인트 현황 조회
     * 오늘 참여 가능 여부를 조회합니다.
     * @param version
     */
    @Headers("Content-Type: application/json")
    @GET("/api/v{version}/points/ad/daily")
    fun dailyAdPoint(
        @Path("version") version: String
    ): Single<DailyAdPointResponse>

    /**
     * 일별 광고 클릭 포인트 적립 요청
     * @param version
     */
    @Headers("Content-Type: application/json")
    @POST("/api/v{version}/points/ad/daily")
    fun dailyAdPoint(
        @Path("version") version: String,
        @Body body: HashMap<String, Any?>
    ): Single<Any>

    /**
     * 락스크린 조회 - 랜딩 이벤트 조회
     * @param version
     */
    @Headers("Content-Type: application/json")
    @GET("/api/v{version}/screen/lock/landing/event")
    fun lockscreenLandingEvent(
        @Path("version") version: String
    ): Single<LockScreenResponse>

    /**
     * 알림 동의 설정 조회
     * @param version
     * @return
     */
    @Headers("Content-Type: application/json")
    @GET("/api/v{version}/users/push")
    fun pushAgree(
        @Path("version") version: String
    ): Single<UserPushAgreeResponse>

    /**
     * 알림 동의 설정 조회
     * @param version
     * @return
     */
    @Headers("Content-Type: application/json")
    @GET("/api/v{version}/users/sound-vibrate")
    fun getPointSoundVibrate(
        @Path("version") version: String,
    ): Single<Response<UserPointSoundVibrateResponse>>

    /**
     * 알림 동의 설정 조회
     * @param version
     * @return
     */
    @Headers("Content-Type: application/json")
    @PATCH("/api/v{version}/users/sound-vibrate")
    fun fetchPointSoundVibrate(
        @Path("version") version: String,
        @Body request: PointSoundVibrateRequest,
    ): Single<Response<Unit>>

    /**
     * 알림 동의 설정 변경
     * @param version
     * @param body
     * @return
     */
    @Headers("Content-Type: application/json")
    @PATCH("/api/v{version}/users/push")
    fun pushAgree(
        @Path("version") version: String,
        @Body body: Map<String, String>,
    ): Single<Response<Unit>>

    /**
     * 락스크린 하단 광고 배너 - 포인트 지급 가능 여부 조회
     * @param version
     */
    @Headers("Content-Type: application/json")
    @GET("/api/v{version}/screen/lock/bottom/banner/point")
    fun getLockScreenBottomBannerPoint(
        @Path("version") version: String
    ): Single<LockScreenBannerPointResponse>

    /**
     * 락스크린 하단 광고 배너 - 포인트 적립 요청
     * @param version
     */
    @Headers("Content-Type: application/json")
    @POST("/api/v{version}/points/ad/lock/bottom/banner")
    fun saveLockScreenBottomBannerPoint(
        @Path("version") version: String,
        @Body body: Map<String, Int>
    ): Single<Response<Unit>>

    /**
     * 모비온 50x50 광고 이벤트(동동이) - 포인트 적립 요청
     * @param version
     */
    @Headers("Content-Type: application/json")
    @POST("/api/v{version}/mobon/dongdong/reward")
    fun mobOnDongDongReward(
        @Path("version") version: String,
        @Body body: MobOnDongDongRewardRequest
    ): Single<Response<Unit>>

    /**
     * 락스크린 쿠팡 CPS 랜딩 포인트 적립 요청
     * @param version
     */
    @Headers("Content-Type: application/json")
    @POST("/api/v{version}/coupang/cps/point")
    fun saveLockScreenCoupangCpsPoint(
        @Path("version") version: String
    ): Single<Response<Unit>>

    /**
     * pomission zone url
     * @param version
     */
    @Headers("Content-Type: application/json")
    @GET("/api/v{version}/pomission/zone")
    fun getPomissionZoneUrl(
        @Path("version") version: String
    ): Single<PomissionZoneUrlResponse>

    /**
     * 락스크린 쿠팡 CPS 랜딩 포인트 적립 요청
     * @param version
     */
    @Headers("Content-Type: application/json")
    @POST("/api/v{version}/pomission/zone/enter/point")
    fun saveLockScreenPomissionZonePoint(
        @Path("version") version: String
    ): Single<Response<Unit>>
}