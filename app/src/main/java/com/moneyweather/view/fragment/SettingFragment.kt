package com.moneyweather.view.fragment

import android.content.Intent
import android.text.TextUtils
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.enliple.datamanagersdk.ENDataManager
import com.enliple.datamanagersdk.events.models.ENPageView
import com.moneyweather.R
import com.moneyweather.base.BaseKotlinFragment
import com.moneyweather.databinding.FragmentSettingBinding
import com.moneyweather.model.enums.ActionBarLeftButtonEnum
import com.moneyweather.model.enums.DialogType
import com.moneyweather.ui.dialog.HCCommonDialog
import com.moneyweather.util.CommonUtils
import com.moneyweather.util.CustomToast
import com.moneyweather.util.PrefRepository
import com.moneyweather.util.analytics.GaToggleClickEvent
import com.moneyweather.view.InviteFriendActivity
import com.moneyweather.view.MainActivity
import com.moneyweather.view.PhoneCertifiedActivity
import com.moneyweather.view.PushSettingActivity
import com.moneyweather.viewmodel.SettingViewModel
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber

@AndroidEntryPoint
class SettingFragment : BaseKotlinFragment<FragmentSettingBinding, SettingViewModel>() {
    override val layoutResourceId: Int get() = R.layout.fragment_setting
    override val viewModel: SettingViewModel by viewModels()

    private val RESULT_CODE: Int = 1122
    private var moveName: String? = ""
    override fun initStartView() {
        viewDataBinding.vm = viewModel
        initActionBar(viewDataBinding.iActionBar, R.string.setting, ActionBarLeftButtonEnum.BACK_BUTTON)

        arguments?.let {
            moveName = it.getString("move_tab")
        }

        viewDataBinding.apply {
            regionLayout.setOnClickListener {
                addTuneEvent("view.fragment.RegionFragment")
                startFragment(R.id.fragmentContainer, RegionFragment::class.java)
            }

            themeLayout.setOnClickListener {
                addTuneEvent("view.fragment.ThemeSettingFragment")
                startFragment(R.id.fragmentContainer, ThemeSettingFragment::class.java)
            }

            noticeLayout.setOnClickListener {
                addTuneEvent("view.fragment.NoticeFragment")
                startFragment(R.id.fragmentContainer, NoticeFragment::class.java)
            }

            qaLayout.setOnClickListener {
                if (PrefRepository.UserInfo.isLogin)
                    startFragment(R.id.fragmentContainer, QaFragment::class.java)
                else
                    CustomToast.showToast(requireContext(), R.string.message_non_login_user)
            }

            faqLayout.setOnClickListener {
                addTuneEvent("view.fragment.FaqFragment")
                startFragment(R.id.fragmentContainer, FaqFragment::class.java)
            }

            inviteBanner.setOnClickListener {
                if (!PrefRepository.UserInfo.isLogin) {
                    CustomToast.showToast(requireContext(), R.string.message_non_login_user)
                } else {
                    startActivity(InviteFriendActivity::class.java)
                }
            }

            switchLockScreen.setOnClickListener {
                // Ga Log Event
                GaToggleClickEvent.logSettingToggleLockScreenClickEvent(PrefRepository.SettingInfo.useLockScreen)
            }

            switchLockScreen.setOnCheckedChangeListener { compoundButton, b ->
                PrefRepository.SettingInfo.useLockScreen = b

                MainActivity.instance?.let {
                    it.requestNeededPermission { it.startLockScreen() }
                }

                viewModel.connectConfigMy()
            }

            pushLayout.setOnClickListener {
                startActivity(PushSettingActivity::class.java)
            }

            llSunflowerSettingLayout.setOnClickListener {
                startFragment(R.id.fragmentContainer, SunflowerSettingFragment::class.java)
            }
        }


        viewModel.resultAppVersion.observe(this, Observer {
            it?.let { makeVersionInfo(it.latestVersion!!) }
        })

        viewModel.connectAppVersion()

        moveName?.let {
            if (TextUtils.equals(it, "notice"))
                startFragment(R.id.fragmentContainer, NoticeFragment::class.java)
        }
    }

    /**
     * @param pvName
     */
    private fun addTuneEvent(pvName: String) {
        try {
            if (ENDataManager.isInitialized() && pvName.isNotEmpty()) {
                val pageView = ENPageView(pvName, "")
                ENDataManager.getInstance().addEvent(pageView)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun verificationPopup() {
        val dialog = HCCommonDialog(requireContext())
            .setDialogType(DialogType.ALERT)
            .setDialogTitle(R.string.certified)
            .setContent(R.string.certified_msg)
            .setPositiveButtonText(R.string.phone_certified2)
            .setOnDismissListener(object : HCCommonDialog.OnDismissListener {
                override fun onDismiss(menuId: Int) {
                    if (menuId == DialogType.BUTTON_POSITIVE.ordinal) {
                        startActivityForResult(PhoneCertifiedActivity::class.java, RESULT_CODE)
                    }
                }
            })
        dialog.show()

    }

    override fun onChildResume() {
        super.onChildResume()

    }

    fun makeVersionInfo(ver: String) {
        val curVersion = String.format(getString(R.string.app_version), CommonUtils.getAppVersion())
        viewDataBinding.version.text = curVersion

//        var newVersion = String.format(getString(R.string.new_version),ver)
//
//        val spannableString = SpannableString(curVersion)
//        val builder = SpannableStringBuilder(spannableString)
//        builder.append(newVersion)
//        val begin = 0
//        val end = curVersion.length
//        builder.setSpan(
//            ForegroundColorSpan(ContextCompat.getColor(requireContext(), R.color.grey_222)),
//            begin,
//            end,
//            Spannable.SPAN_INCLUSIVE_EXCLUSIVE
//        )
//        viewDataBinding.version.text = builder
//        viewDataBinding.versionStatus.visibility = if(TextUtils.equals(CommonUtils.getAppVersion(),ver)) View.VISIBLE else View.INVISIBLE
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        Timber.d("onActivityResult is call")
        super.onActivityResult(requestCode, resultCode, data)

    }

}