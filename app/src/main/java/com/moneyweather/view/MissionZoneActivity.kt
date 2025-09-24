package com.moneyweather.view

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.activity.viewModels
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle.State.RESUMED
import androidx.lifecycle.Observer
import com.enliple.banner.MobSDK
import com.enliple.banner.common.Listener
import com.enliple.banner.fragment.LadderDirectFragment
import com.enliple.banner.fragment.LottoDirectFragment
import com.enliple.banner.fragment.NativeMissionZoneMainFragment
import com.google.firebase.analytics.FirebaseAnalytics
import com.moneyweather.R
import com.moneyweather.base.BaseKotlinActivity
import com.moneyweather.databinding.ActivityMissionZoneBinding
import com.moneyweather.util.FirebaseAnalyticsManager
import com.moneyweather.util.PrefRepository
import com.moneyweather.view.LockScreenWebViewActivity.ViewType
import com.moneyweather.view.fragment.MainHomeFragment.Companion.SUCCESS_GAME
import com.moneyweather.viewmodel.MissionZoneViewModel
import dagger.hilt.android.AndroidEntryPoint
import org.apache.commons.lang3.StringUtils

@AndroidEntryPoint
class MissionZoneActivity : BaseKotlinActivity<ActivityMissionZoneBinding, MissionZoneViewModel>() {

    override val layoutResourceId: Int get() = R.layout.activity_mission_zone
    override val viewModel: MissionZoneViewModel by viewModels()

    private var gamePoint: Int = 0

    override fun initStartView() {
        FirebaseAnalyticsManager.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW, Bundle().apply {
            putString(FirebaseAnalyticsManager.VIEW_NAME, "에이닉 게임존")
            putString(FirebaseAnalyticsManager.START_POINT, "lockScreen")
        })

        viewModel.connectAnicParticipationCheck(true)

        viewModel.isFirst.observe(this@MissionZoneActivity, Observer {
            try {
                it?.let {
                    if (it) startGameZone()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        })
    }

    private fun startGameZone() {
        var userId = PrefRepository.UserInfo.userId

        Handler(Looper.getMainLooper()).post {
            MobSDK.setUserId(userId) { isSetUserSuccess ->
                if (isSetUserSuccess) {
                    MobSDK.enGameZoneFragment(null, gameListener,
                        Listener.GameFragmentListener { fragmentObject ->
                            if (fragmentObject != null) {
                                var fragment: Fragment? = when (fragmentObject) {
                                    is NativeMissionZoneMainFragment -> fragmentObject // 미션존 리스트 화면
                                    is LadderDirectFragment -> fragmentObject // 미션존 사다리 화면
                                    is LottoDirectFragment -> fragmentObject // 미션존 복권
                                    else -> null
                                }

                                if (fragment != null) {
                                    val fragmentManager = supportFragmentManager
                                    val fragmentTransaction = fragmentManager.beginTransaction()
                                    fragmentTransaction.replace(R.id.fragmentContainer, fragment)
                                    fragmentTransaction.addToBackStack(null)

                                    if (!fragmentManager.isStateSaved || lifecycle.currentState.isAtLeast(RESUMED)) {
                                        fragmentTransaction.commit()
                                    } else {
                                        fragmentTransaction.commitAllowingStateLoss()
                                    }
                                }
                            }
                        },
                        Listener.GameFragmentEndListener {
                            if (gamePoint > 0) {
                                var intent = Intent()
                                intent.putExtra("type", ViewType.ANIC_GAME.name)
                                intent.putExtra("gamePoint", gamePoint)
                                setResult(RESULT_OK, intent)
                            }
                            finish()
                        }
                    )
                }
            }
        }
    }

    private val gameListener =
        Listener.OnGameListener { resultCode, resultMsg, point, trackingId, gameType, location ->
            try {
                if (StringUtils.isNotEmpty(resultCode) && SUCCESS_GAME == resultCode) {
                    if (location.isNotEmpty() && "banner" == location && point > 0) {
                        viewModel.saveDailyAdPoint(point)
                    } else {
                        viewModel.connectAnicPoint(point, trackingId.toInt(), gameType, location)
                        gamePoint = point
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
}