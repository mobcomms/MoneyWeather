package com.moneyweather.util

import com.moneyweather.model.enums.BannerType

object AdRatioScheduler {

    private const val DEFAULT_RATIO: Int = 5
    private val bannerList: MutableList<String> = mutableListOf()
    private var index: Int = PrefRepository.LockQuickInfo.lockScreenBottomBannerIndex

    private fun generateNewSequence() {
        if (bannerList.isNotEmpty()) {
            bannerList.clear()
        }

        val bannerExposureRatio = PrefRepository.LockQuickInfo.bannerExposureRatio
        val (adpieCount, mobwithCount) = bannerExposureRatio.split(":").map { it.toInt() }

        if (DEFAULT_RATIO == adpieCount) {
            repeat(DEFAULT_RATIO) {
                bannerList.add(BannerType.ADPIE.type)
                bannerList.add(BannerType.MOBWITH.type)
            }
        } else {
            repeat(adpieCount) {
                bannerList.add(BannerType.ADPIE.type)
            }

            repeat(mobwithCount) {
                bannerList.add(BannerType.MOBWITH.type)
            }

            bannerList.shuffle()
        }
    }

    /**
     * @return true: 최초 실행시, 10회 노출 후, Remote Config 변경시
     */
    private fun shouldGenerateNewSequence(): Boolean {
        return bannerList.isEmpty() || index % 10 == 0 || PrefRepository.LockQuickInfo.isChangeRemoteConfig
    }

    /**
     * @return 노출 배너 타입 (AdPie, MobWith)
     */
    fun getBannerType(): String {
        if (shouldGenerateNewSequence()) {
            generateNewSequence()
            PrefRepository.LockQuickInfo.isChangeRemoteConfig = false
            PrefRepository.LockQuickInfo.lockScreenBottomBannerIndex = 0
        }

        val bannerType = bannerList[index % 10]

        index = (index + 1) % 10
        PrefRepository.LockQuickInfo.lockScreenBottomBannerIndex = index

        return bannerType
    }

    fun isAdPieNativeFirst(): Boolean {
        val bannerExposureAdPieOrder = PrefRepository.LockQuickInfo.bannerExposureAdPieOrder

        return bannerExposureAdPieOrder == "native"
    }
}