package com.moneyweather.util.remoteConfig

import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.remoteConfigSettings
import com.moneyweather.util.PrefRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RemoteConfigManager @Inject constructor() {
    private val remoteConfig = FirebaseRemoteConfig.getInstance()

    private val _configFlow = MutableStateFlow<Map<String, Any>>(emptyMap())
    val configFlow: StateFlow<Map<String, Any>> = _configFlow.asStateFlow()

    init {
        remoteConfig.setConfigSettingsAsync(
            remoteConfigSettings {
                minimumFetchIntervalInSeconds = 3600
            }
        )

        fetchRemoteConfig()
    }

    private fun fetchRemoteConfig() {
        remoteConfig.fetchAndActivate()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val bannerCount = remoteConfig.getLong(BannerExposure.COUNT).toInt()
                    val bannerRatio = remoteConfig.getString(BannerExposure.RATIO)
                    val bannerAdPieOrder = remoteConfig.getString(BannerExposure.ADPIE_ORDER)
                    val buzzvilAvailable = remoteConfig.getBoolean(BannerExposure.CHARGE_BUZZVIL_AVAILABLE)

                    saveRemoteConfigToCache(
                        bannerCount,
                        bannerRatio,
                        bannerAdPieOrder,
                        buzzvilAvailable
                    )

                    _configFlow.value = mapOf(
                        BannerExposure.COUNT to bannerCount,
                        BannerExposure.RATIO to bannerRatio,
                        BannerExposure.ADPIE_ORDER to bannerAdPieOrder,
                        BannerExposure.CHARGE_BUZZVIL_AVAILABLE to buzzvilAvailable
                    )
                }
            }
    }

    private fun saveRemoteConfigToCache(
        bannerCount: Int,
        bannerRatio: String,
        bannerAdPieOrder: String,
        buzzvilAvailable: Boolean
    ) {
        PrefRepository.LockQuickInfo.apply {
            isChangeRemoteConfig =
                bannerExposureCount != bannerCount || bannerExposureRatio != bannerRatio || bannerExposureAdPieOrder != bannerAdPieOrder

            bannerExposureCount = bannerCount
            bannerExposureRatio = bannerRatio
            bannerExposureAdPieOrder = bannerAdPieOrder
            chargeBuzzvilAvailable = buzzvilAvailable
        }
    }

    companion object {
        object BannerExposure {
            const val COUNT = "banner_exposure_count"
            const val RATIO = "banner_exposure_ratio"
            const val ADPIE_ORDER = "banner_exposure_adpie_order"

            const val CHARGE_BUZZVIL_AVAILABLE = "charge_buzzvil_available"
        }
    }
}