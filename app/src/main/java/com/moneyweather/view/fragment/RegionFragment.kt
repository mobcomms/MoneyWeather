package com.moneyweather.view.fragment

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Paint
import android.os.Bundle
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.fragment.app.viewModels
import com.google.firebase.analytics.FirebaseAnalytics
import com.moneyweather.R
import com.moneyweather.base.BaseKotlinFragment
import com.moneyweather.databinding.FragmentRegionBinding
import com.moneyweather.model.enums.ActionBarLeftButtonEnum
import com.moneyweather.model.enums.TermsType
import com.moneyweather.util.CommonUtils
import com.moneyweather.util.FirebaseAnalyticsManager
import com.moneyweather.util.PermissionUtils
import com.moneyweather.view.TermsActivity
import com.moneyweather.viewmodel.RegionViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class RegionFragment : BaseKotlinFragment<FragmentRegionBinding, RegionViewModel>() {
    override val layoutResourceId: Int get() = R.layout.fragment_region
    override val viewModel: RegionViewModel by viewModels()

    private var instanceBundle: Bundle? = null

    override fun initStartView() {
        FirebaseAnalyticsManager.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW, Bundle().apply {
            putString(FirebaseAnalyticsManager.VIEW_NAME, "지역")
        })

        viewDataBinding.vm = viewModel
        initActionBar(
            viewDataBinding.iActionBar,
            R.string.region,
            ActionBarLeftButtonEnum.BACK_BUTTON
        )

        viewDataBinding.apply {
            activity?.let {
                if (ActivityCompat.checkSelfPermission(
                        it,
                        Manifest.permission.ACCESS_FINE_LOCATION
                    ) != PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(
                        it,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    lyTop.visibility = View.VISIBLE
                    viewModel.noPermission.value = true
                }
            }

            tvAgreement.paintFlags = Paint.UNDERLINE_TEXT_FLAG

            tvAgreement.setOnClickListener {
                val intent = Intent(getContext(), TermsActivity::class.java)
                intent.putExtra("type", TermsType.SERVICE)
                startActivity(intent)
            }

            tvAgree.setOnClickListener {
                requestNeededPermission {
                    viewDataBinding.lyTop.visibility = View.GONE
                }
            }

        }


        viewModel.connectRegionList()
    }


    override fun onChildResume() {
        super.onChildResume()

    }

    private val requestNeededPermission =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {
            var isAllGrant = true
            for (isGrant in it) {
                if (!isGrant.value)
                    isAllGrant = false
            }
            if (isAllGrant) {
                //권한 얻은 후!
                viewDataBinding.lyTop.visibility = View.GONE

                CommonUtils.getLocation(
                    onSuccessCallbackListener = object : CommonUtils.Companion.OnCallbackLocationListener {
                        override fun onCallbackLocation(lat: String, lon: String) {
                            viewModel.noPermission.value = false
                            viewModel.connectRegionList()
                        }
                    },
                    onFailureCallbackListener = object : CommonUtils.Companion.OnCallbackLocationListener {
                        override fun onCallbackLocation(lat: String, lon: String) {
                            viewModel.noPermission.value = false
                            viewModel.connectRegionList()
                        }
                    }
                )
            }
        }

    private fun requestNeededPermission(callback: () -> Unit) {
        context?.let {
            val locationPermimssions = REQUEST_PERMISSIONS

            val granted = PermissionUtils.isGrantedPermission(it, locationPermimssions)
            if (!granted) {
                requestNeededPermission.launch(locationPermimssions)
            } else {
                callback()
            }
        }

    }


    companion object {
        private val REQUEST_PERMISSIONS = arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )


    }

}