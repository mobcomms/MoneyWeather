package com.moneyweather.ui.dialog

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.lifecycleScope
import com.enliple.banner.MobSDK
import com.enliple.banner.common.Listener
import com.enliple.banner.common.MobConstant
import com.enliple.banner.daro.AdUnit
import com.moneyweather.R
import com.moneyweather.databinding.DialogAppEndBinding
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class AppEndDialog(
    var finishButtonUnit: ()->Unit
) : DialogFragment(), View.OnClickListener {

    private lateinit var _binding: DialogAppEndBinding

    // 2. Non-null assertion을 사용한 Getter (편의용)
    // 이 프로퍼티는 onCreateView와 onDestroyView 사이에서만 유효합니다.
//    private val binding get() = _binding!!
    private val houseBannerLandingUrl = "https://ad.n-bridge.io/103936SA?nbr_sub_media=moneyweather_close_aos"

    //    init {
//        window?.apply {
//            setBackgroundDrawable(Color.TRANSPARENT.toDrawable())
//            setDimAmount(0.5f)
//        }
//        setContentView(binder.root)
//
//        initView()
//    }
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = DialogAppEndBinding.inflate(inflater, container, false)
        return _binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
    }

    private fun initView() {

        var daroView = MobSDK.getDaroView(
            context,
            MobConstant.DARO_AD_TYPE_MREC,
            AdUnit.DONSEE_APP_END_POPUP_BANNER,
            object : Listener.OnDaroListener {
                override fun onParticipated(adUnit: AdUnit?) {

                }

                override fun onLoadFail() {
                    // 하우스 배너 출력
                    if(_binding.daroAdContainer.visibility != View.VISIBLE) {
                        _binding.adDefenceImage.visibility = View.VISIBLE
                    }
                }

                override fun onLoadSuccess() {
                    // 광고 출력
                    _binding.adDefenceImage.visibility = View.INVISIBLE
                    _binding.daroAdContainer.visibility = View.VISIBLE
                }
            })
        _binding.daroAdContainer.removeAllViews()
        _binding.daroAdContainer.addView(daroView)

        _binding.btnFinish.setOnClickListener(this)
        _binding.btnCancel.setOnClickListener(this)
        _binding.adDefenceImage.setOnClickListener(this)
        checkViewVisibilityAfterDelay()
    }

    override fun onClick(view: View?) {

        when(view?.id) {
            R.id.btn_cancel -> dismiss()
            R.id.btn_finish -> finishButtonUnit()
            R.id.ad_defence_image -> {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(houseBannerLandingUrl))
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                try {
                    requireActivity().startActivity(intent)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }
    private fun checkViewVisibilityAfterDelay() {
        // viewLifecycleOwner.lifecycleScope를 사용해 다이얼로그 뷰의 생명주기에 맞는 코루틴을 시작합니다.
        viewLifecycleOwner.lifecycleScope.launch {
            // 2초 (2000 밀리초) 동안 코루틴을 지연시킵니다.
            delay(2000)

            // 2초 후, 뷰가 여전히 화면에 붙어 있는지(isAttachedToWindow)와
            // 보이는 상태(visibility)인지 함께 확인하는 것이 더 안전합니다.
            // (딜레이 동안 다이얼로그가 닫혔을 수도 있기 때문입니다.)
            if (!_binding.daroAdContainer.isVisible) {
                _binding.adDefenceImage.visibility = View.VISIBLE
            }
        }
    }
}