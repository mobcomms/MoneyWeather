package com.moneyweather.data.remote.impl

import com.moneyweather.data.remote.UrlHelper
import com.moneyweather.data.remote.model.ApiUserModel
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
import com.moneyweather.data.remote.service.ApiUserService
import com.moneyweather.util.PrefRepository
import io.reactivex.Single
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import org.json.JSONObject
import retrofit2.Response
import java.io.File
import javax.inject.Inject


class ApiUserImpl @Inject constructor(
    private val service: ApiUserService
) : ApiUserModel {
    override fun post(url: String, m: Map<String, Any>): Single<JSONObject> {
        TODO("Not yet implemented")
    }


    override fun userPhotoUpload(params: String): Single<BaseResponse> {
        val f = File(params)
//        val requestFile = RequestBody.create(f, MediaType.parse("image/*"))
        val requestFile = f.asRequestBody("image/*".toMediaTypeOrNull())
        var p = MultipartBody.Part.createFormData("file", f.getName(), requestFile)
        return service.userPhotoUpload(p)
    }

    override fun userInfo(): Single<UserResponse> {
        var version = UrlHelper.API_VERSION
        return service.userInfo(version)
    }

    override fun userPoint(): Single<UserPointResponse> {
        var version = UrlHelper.API_VERSION
        return service.userPoint(version)
    }

    override fun regionListInfo(latitude: String, longitude: String): Single<RegionResponse> {
        var version = UrlHelper.API_VERSION
        return service.regionListInfo(version, latitude, longitude)
    }

    override fun getPointSoundVibrate(): Single<Response<UserPointSoundVibrateResponse>> =
        service.getPointSoundVibrate(
            version = UrlHelper.API_VERSION
        )

    override fun updatePointSoundVibrate(request: PointSoundVibrateRequest): Single<Response<Unit>> =
        service.fetchPointSoundVibrate(
            version = UrlHelper.API_VERSION,
            request = request
        )

    override fun refreshSession(): Single<TokenResponse> {
        var m = HashMap<String, Any?>()
        m["refreshToken"] = PrefRepository.UserInfo.refreshToken
        return service.refreshSession(m)
    }

    override fun signUpGuest(m: HashMap<String, Any?>): Single<TokenResponse> {
        return service.signUpGuest(m)
    }

    override fun getCategoryList(): Single<ShopCategoryResponse> {
        var version = UrlHelper.API_VERSION

        return service.getCategoryList(version)
    }

    override fun getProductList(
        category: String,
        affiliate: String,
        search: String,
        showCount: Int,
        page: Int,
        type: Boolean
    ): Single<ShopProductListResponse> {
        var version = UrlHelper.API_VERSION

        return service.getProductList(version, category, affiliate, search, showCount, page, type)
    }

    override fun getProductDetail(pk: String): Single<ProductResponse> {
        var version = UrlHelper.API_VERSION
        return service.getProductDetail(version, pk)
    }

    override fun purchaseCoupon(m: HashMap<String, Any?>): Single<BaseResponse> {
        var version = UrlHelper.API_VERSION
        return service.purchaseCoupon(version, m)
    }

    override fun updateCouponState(couponId: Int): Single<BaseResponse> {
        var version = UrlHelper.API_VERSION
        return service.updateCouponState(version, couponId)
    }

    override fun getAuth(phone: String): Single<AuthResponse> {
        var version = UrlHelper.API_VERSION
        return service.getAuth(version, phone)
    }

    override fun modifyAuth(m: HashMap<String, Any?>): Single<BaseResponse> {
        var version = UrlHelper.API_VERSION
        return service.modifyAuth(version, m)
    }

    override fun getCouponList(m: HashMap<String, Any?>): Single<CouponResponse> {
        var version = UrlHelper.API_VERSION
        return service.getCouponList(version, m)
    }

    override fun getCouponDetail(pk: Int): Single<CouponDetailResponse> {
        var version = UrlHelper.API_VERSION
        return service.getCouponDetail(version, pk)
    }

    override fun couponRefund(m: HashMap<String, Any?>): Single<BaseResponse> {
        var version = UrlHelper.API_VERSION
        return service.couponRefund(version, m)
    }

    override fun getTerms(): Single<TermsResponse> {
        var version = UrlHelper.API_VERSION
        return service.getTerms(version)
    }

    override fun getPrivacy(): Single<TermsResponse> {
        var version = UrlHelper.API_VERSION
        return service.getPrivacy(version)
    }

    override fun getInviteInfo(): Single<InviteResponse> {
        var version = UrlHelper.API_VERSION
        return service.getInviteInfo(version)
    }

    override fun noticeListInfo(page: Int, showCount: Int): Single<NoticeResponse> {
        var version = UrlHelper.API_VERSION
        return service.noticeListInfo(version, page, showCount)
    }

    override fun noticeDetail(noticeId: Int): Single<NoticeDetailResponse> {
        var version = UrlHelper.API_VERSION
        return service.noticeDetail(version, noticeId)
    }

    override fun qaListInfo(): Single<QaListResponse> {
        var version = UrlHelper.API_VERSION
        return service.qaListInfo(version)
    }

    override fun qaWrite(m: HashMap<String, Any?>): Single<BaseResponse> {
        var version = UrlHelper.API_VERSION
        return service.qaWrite(version, m)
    }

    override fun qaDetail(pk: Int): Single<QaDetailResponse> {
        var version = UrlHelper.API_VERSION
        return service.qaDetail(version, pk)
    }

    override fun faqListInfo(category: String): Single<FaqResponse> {
        var version = UrlHelper.API_VERSION
        return service.faqListInfo(version, category)
    }

    override fun faqDetail(pk: Int): Single<FaqDetailResponse> {
        var version = UrlHelper.API_VERSION
        return service.faqDetail(version, pk)
    }

    override fun signUpUser(m: HashMap<String, Any?>): Single<TokenResponse> {
        var version = UrlHelper.API_VERSION
        return service.signUpUser(version, m)
    }

    override fun login(m: HashMap<String, Any?>): Single<TokenResponse> {
        var version = UrlHelper.API_VERSION
        return service.login(version, m)
    }

    override fun logOut(): Single<BaseResponse> {
        var m = HashMap<String, Any?>()
        m["accessToken"] = PrefRepository.UserInfo.accessToken
        var version = UrlHelper.API_VERSION
        return service.logOut(version, m)
    }

    override fun withdraw(): Single<BaseResponse> {
        var version = UrlHelper.API_VERSION
        return service.withdraw(version)
    }

    override fun notiWeather(latitude: String, longitude: String): Single<WeatherResponse> {
        var version = UrlHelper.API_VERSION
        return service.notiWeather(version, latitude, longitude)
    }

    override fun currentWeather(latitude: String, longitude: String): Single<WeatherResponse> {
        var version = UrlHelper.API_VERSION
        return service.currentWeather(version, latitude, longitude)
    }

    override fun hourlyWeather(
        latitude: String,
        longitude: String,
        size: Int
    ): Single<WeatherResponse> {
        var version = UrlHelper.API_VERSION
        return service.hourlyWeather(version, latitude, longitude, size)
    }

    override fun regionsWeather(latitude: String, longitude: String): Single<WeatherResponse> {
        var version = UrlHelper.API_VERSION
        return service.regionsWeather(version, latitude, longitude)
    }

    override fun weeklyWeather(latitude: String, longitude: String): Single<WeatherResponse> {
        var version = UrlHelper.API_VERSION
        return service.weeklyWeather(version, latitude, longitude)
    }

    override fun appVersion(): Single<AppVersionResponse> {
        var version = "2"
        return service.appVersion(version)
    }

    override fun verification(): Single<VerificationResponse> {
        var version = UrlHelper.API_VERSION
        return service.verification(version)
    }

    override fun configInit(): Single<ConfigInitResponse> {
        var version = UrlHelper.API_VERSION
        return service.configInit(version)
    }

    override fun newsFeeds(): Single<NewsResponse> {
        var version = UrlHelper.API_VERSION
        var size = 5
        return service.newsFeeds(version, size)
    }

    override fun pointSave(point: Int): Single<BaseResponse> {
        var m = HashMap<String, Any?>()
        m["clickPoint"] = point
        var version = UrlHelper.API_VERSION
        return service.pointSave(version, m)
    }

    override fun pointHistory(page: Int, showCount: Int, type: Int): Single<PointHistoryResponse> {
        var version = UrlHelper.API_VERSION
        return service.pointHistory(version, page, showCount, type)
    }

    override fun pointAvailable(): Single<AvailablePointResponse> {
        var version = UrlHelper.API_VERSION
        return service.pointAvailable(version)
    }

    override fun findEmail(m: HashMap<String, Any?>): Single<FindEmailResponse> {
        var version = UrlHelper.API_VERSION
        return service.findEmail(version, m)
    }

    override fun sendEmailCode(m: HashMap<String, Any?>): Single<BaseResponse> {
        var version = UrlHelper.API_VERSION
        return service.sendEmailCode(version, m)
    }

    override fun codeVerify(codeVerify: String): Single<BaseResponse> {
        var version = UrlHelper.API_VERSION
        return service.codeVerify(version, codeVerify)
    }

    override fun sendNewPass(m: HashMap<String, Any?>): Single<BaseResponse> {
        var version = UrlHelper.API_VERSION
        return service.sendNewPass(version, m)
    }

    override fun configMy(m: HashMap<String, Any?>): Single<BaseResponse> {
        var version = UrlHelper.API_VERSION
        return service.configMy(version, m)
    }

    override fun configMy(): Single<ConfigResponse> {
        var version = UrlHelper.API_VERSION
        return service.configMy(version)
    }

    override fun inviteRedeem(m: HashMap<String, Any?>): Single<BaseResponse> {
        var version = UrlHelper.API_VERSION
        return service.inviteRedeem(version, m)
    }

    override fun inviteRedeemSocial(m: HashMap<String, Any?>): Single<BaseResponse> {
        var version = UrlHelper.API_VERSION
        return service.inviteRedeemSocial(version, m)
    }

    override fun infoLockScreen(m: HashMap<String, Any?>): Single<LockScreenResponse> {
        var version = "2"
        return service.infoLockScreen(version, m)
    }

    override fun simpleLockScreen(m: HashMap<String, Any?>): Single<LockScreenResponse> {
        var version = "2"
        return service.simpleLockScreen(version, m)
    }

    override fun getHolidays(): Single<HolidayResponse> {
        var version = UrlHelper.API_VERSION
        return service.getHolidays(version)
    }

    override fun poMissionAuto(m: HashMap<String, Any?>): Single<BaseResponse> {
        var version = UrlHelper.API_VERSION
        return service.poMissionAuto(version, m)
    }

    override fun anic(): Single<AnicResponse> {
        var version = UrlHelper.API_VERSION
        return service.anic(version)
    }

    override fun anic(m: HashMap<String, Any?>): Single<Any> {
        var version = UrlHelper.API_VERSION
        var historyId = PrefRepository.UserInfo.historyId
        return service.anic(version, historyId, m)
    }

    override fun mobonReward(): Single<Any> {
        var version = UrlHelper.API_VERSION
        var sc = PrefRepository.UserInfo.sc
        return service.mobonReward(version, sc)
    }

    override fun screenPopupsActive(): Single<ScreenPopupsActiveResponse> {
        var version = UrlHelper.API_VERSION
        return service.screenPopupsActive(version)
    }

    override fun newsReward(guid: String): Single<Any> {
        var version = UrlHelper.API_VERSION
        return service.newsReward(version, guid)
    }

    override fun dailyAdPoint(): Single<DailyAdPointResponse> {
        var version = UrlHelper.API_VERSION
        return service.dailyAdPoint(version)
    }

    override fun dailyAdPoint(m: HashMap<String, Any?>): Single<Any> {
        var version = UrlHelper.API_VERSION
        return service.dailyAdPoint(version, m)
    }

    override fun updateFcmToken(token: String): Single<Response<Unit>> {
        return service.usersFcm(
            version = UrlHelper.API_VERSION,
            body = mapOf("fcm" to token)
        )
    }

    override fun lockscreenLandingEvent(): Single<LockScreenResponse> {
        return service.lockscreenLandingEvent(
            version = UrlHelper.API_VERSION
        )
    }

    override fun getPushAgree(): Single<UserPushAgreeResponse> {
        return service.pushAgree(
            version = UrlHelper.API_VERSION
        )
    }

    override fun updatePushAgree(m: Map<String, String>): Single<Response<Unit>> {
        return service.pushAgree(
            version = UrlHelper.API_VERSION,
            body = m
        )
    }

    override fun getLockScreenBottomBannerPoint(): Single<LockScreenBannerPointResponse> {
        return service.getLockScreenBottomBannerPoint(
            version = UrlHelper.API_VERSION
        )
    }

    override fun saveLockScreenBottomBannerPoint(m: Map<String, Int>): Single<Response<Unit>> {
        return service.saveLockScreenBottomBannerPoint(
            version = UrlHelper.API_VERSION,
            body = m
        )
    }

    override fun saveLockScreenCoupangCpsPoint(): Single<Response<Unit>> {
        return service.saveLockScreenCoupangCpsPoint(
            version = UrlHelper.API_VERSION
        )
    }

    override fun mobOnDongDongReward(param: MobOnDongDongRewardRequest): Single<Response<Unit>> {
        return service.mobOnDongDongReward(
            version = UrlHelper.API_VERSION,
            body = param
        )
    }

    override fun getPomissionZoneUrl(): Single<PomissionZoneUrlResponse> {
        return service.getPomissionZoneUrl(
            version = UrlHelper.API_VERSION
        )
    }

    override fun saveLockScreenPomissionZonePoint(): Single<Response<Unit>> {
        return service.saveLockScreenPomissionZonePoint(
            version = UrlHelper.API_VERSION
        )
    }
}