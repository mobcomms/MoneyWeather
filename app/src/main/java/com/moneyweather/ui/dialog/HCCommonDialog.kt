package com.moneyweather.ui.dialog

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.text.Spannable
import android.text.SpannableString
import android.text.SpannableStringBuilder
import android.text.TextUtils
import android.text.style.ForegroundColorSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout.LayoutParams
import androidx.viewpager2.widget.ViewPager2
import com.bumptech.glide.Glide
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.moneyweather.R
import com.moneyweather.adapter.FragmentAdapter
import com.moneyweather.databinding.DialogHcCommonBinding
import com.moneyweather.databinding.PopupNewPointInfoBinding
import com.moneyweather.databinding.PopupPointPolicyInfoBinding
import com.moneyweather.model.enums.DialogType

/**
 * COMMON DIALOG
 */
class HCCommonDialog(
    context: Context,
    themeResId: Int = R.style.Theme_CommonDialog,
) : Dialog(context, themeResId), View.OnClickListener {

    private val binding = DialogHcCommonBinding.inflate(LayoutInflater.from(context))

    private var onDismissListener: OnDismissListener? = null

    init {
        window?.apply {
            setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            setDimAmount(0.5f)
        }
        setContentView(binding.root)

        initView()
    }

    private fun initView() {
        with(binding) {
            tvHcCommonConfirmBtn.setOnClickListener(this@HCCommonDialog)
            tvHcCommonPositiveBtn.setOnClickListener(this@HCCommonDialog)
            tvHcCommonNegativeBtn.setOnClickListener(this@HCCommonDialog)
        }
    }

    public override fun onStart() {
        val metrics = context.resources.displayMetrics
        val width = (metrics.widthPixels * 0.8).toInt()
        window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        window?.setLayout(width, ViewGroup.LayoutParams.WRAP_CONTENT)

        super.onStart()
    }

    fun setDialogType(dialogType: DialogType): HCCommonDialog {
        setButtonStyle(dialogType)

        return this
    }

    private fun hideCommonTitleAndContent() {
        binding.tvHcCommonTitle.visibility = View.GONE
        binding.tvHcCommonContent.visibility = View.GONE
    }

    /**
     * 다이얼로그 본문 layout 추가 버튼 이외에 textview 는 모두 gone
     * @param layoutId
     */
    fun setLayout(layoutId: Int): HCCommonDialog {
        hideCommonTitleAndContent()

        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val layout = inflater.inflate(layoutId, null)

        binding.llHcCommonContainer.addView(layout)

        return this
    }

    @SuppressLint("SetJavaScriptEnabled")
    fun setLockPointPolicyWebViewLayout(url: String, height: Int): HCCommonDialog {
        hideCommonTitleAndContent()

        val newPointPolicyInfoBinding = PopupNewPointInfoBinding.inflate(LayoutInflater.from(context))
        val pointPolicyInfoBinding = PopupPointPolicyInfoBinding.inflate(LayoutInflater.from(context))
        val webView = pointPolicyInfoBinding.wvPopupPointPolicyInfo

        setWebView(webView, url, height)

        newPointPolicyInfoBinding.clPointPolicyContainer.addView(pointPolicyInfoBinding.root)
        binding.llHcCommonContainer.addView(newPointPolicyInfoBinding.root)

        return this
    }

    @SuppressLint("SetJavaScriptEnabled")
    fun setMainPointPolicyWebViewLayout(url: String, height: Int): HCCommonDialog {
        hideCommonTitleAndContent()

        val pointPolicyInfoBinding = PopupPointPolicyInfoBinding.inflate(LayoutInflater.from(context))
        val webView = pointPolicyInfoBinding.wvPopupPointPolicyInfo

        setWebView(webView, url, height)

        binding.llHcCommonContainer.addView(pointPolicyInfoBinding.root)

        return this
    }

    private fun setWebView(webView: WebView, url: String, height: Int) {
        webView.layoutParams = LayoutParams(
            LayoutParams.MATCH_PARENT,
            height
        ).apply {
            topToTop = LayoutParams.PARENT_ID
            bottomToBottom = LayoutParams.PARENT_ID
            startToStart = LayoutParams.PARENT_ID
            endToEnd = LayoutParams.PARENT_ID
        }

        with(webView.settings) {
            javaScriptEnabled = true
            domStorageEnabled = true
            useWideViewPort = true
            loadWithOverviewMode = true
            cacheMode = android.webkit.WebSettings.LOAD_NO_CACHE
        }

        webView.setBackgroundColor(Color.TRANSPARENT)
        webView.webViewClient = WebViewClient()
        webView.loadUrl(url)
    }

    /**
     * 업데이트 체크 팝업용
     * @param layoutId
     * @param content
     * @return
     */
    fun setLayout(layoutId: Int, content: String?): HCCommonDialog {
        hideCommonTitleAndContent()

        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val layout = inflater.inflate(layoutId, null)

        val contentTV = layout.findViewById<TextView>(R.id.contentTV)
        contentTV?.text = content

        binding.llHcCommonContainer.addView(layout)

        return this
    }

    /**
     * 강조할 텍스트 색상 변경
     * @param textView
     * @param text
     * @param start1
     * @param end1
     * @param start2
     * @param end2
     */
    private fun setTextColor(textView: TextView, text: String, start1: Int, end1: Int, start2: Int, end2: Int) {
        try {
            val blueTextPart1 = text.substring(start1, end1)
            val blueTextPart2 = text.substring(start2, end2)

            val spannableString = SpannableString(text)

            spannableString.setSpan(
                ForegroundColorSpan(context.resources.getColor(R.color.default_main_color)),
                text.indexOf(blueTextPart1),
                text.indexOf(blueTextPart1) + blueTextPart1.length,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
            spannableString.setSpan(
                ForegroundColorSpan(context.resources.getColor(R.color.default_main_color)),
                text.indexOf(blueTextPart2),
                text.indexOf(blueTextPart2) + blueTextPart2.length,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )

            textView.text = spannableString
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * 다이얼로그 제목 설정
     * @param title 제목 String
     */
    fun setDialogTitle(title: String?): HCCommonDialog {
        binding.tvHcCommonTitle.apply {
            visibility = View.VISIBLE
            text = title
        }

        return this
    }

    /**
     * 다이얼로그 제목 설정
     * @param strResId 제목 Resource ID
     */
    fun setDialogTitle(strResId: Int): HCCommonDialog {
        binding.tvHcCommonTitle.apply {
            visibility = View.VISIBLE
            setText(strResId)
        }

        return this
    }

    fun setContent(builder: SpannableStringBuilder?): HCCommonDialog {
        binding.tvHcCommonTitle.apply {
            visibility = View.VISIBLE
            text = builder
        }

        return this
    }

    /**
     * 다이얼로그 내용 설정
     * @param content 내용 String
     */
    fun setContent(content: String?): HCCommonDialog {
        binding.tvHcCommonContent.text = content

        return this
    }

    /**
     * 다이얼로그 내용 설정
     * @param strResId 내용 Resource ID
     */
    fun setContent(strResId: Int): HCCommonDialog {
        binding.tvHcCommonContent.setText(strResId)
        return this
    }

    /**
     * 다이얼로그 경고 이미지
     * @param src 이미지
     */
    fun setDialogImage(src: Int): HCCommonDialog {
        binding.ivHcCommonImage.setImageResource(src)

        return this
    }

    /**
     * 공지팝업용 이미지
     * @param imgUrl
     * @return
     */
    fun setNoticeDialogImage(imgUrl: String?): HCCommonDialog {
        val metrics = context.resources.displayMetrics
        val width = (metrics.widthPixels * 0.8).toInt()

        val params = binding.ivHcCommonNotice.layoutParams.apply {
            this.width = width
            this.height = (width * 1.66f).toInt()
        }

        binding.ivHcCommonNotice.layoutParams = params
        binding.ivHcCommonNotice.visibility = View.VISIBLE

        Glide.with(context)
            .load(imgUrl)
            .into(binding.ivHcCommonNotice)

        return this
    }

    /**
     * 시간별 날씨, 주간 날씨
     * @param fragmentAdapter
     * @return
     */
    fun setWeatherInfo(fragmentAdapter: FragmentAdapter?): HCCommonDialog {
        val tabLayout: TabLayout? = findViewById(R.id.tab)
        val viewPager: ViewPager2? = findViewById(R.id.viewPager)

        if (tabLayout == null || viewPager == null) return this

        viewPager.apply {
            setUserInputEnabled(false)
            setAdapter(fragmentAdapter)
        }

        TabLayoutMediator(tabLayout, viewPager) { tab: TabLayout.Tab, position: Int ->
            when (position) {
                0 -> tab.setText(context.getString(R.string.hourly_weather))
                1 -> tab.setText(context.getString(R.string.weekly_weather))
            }
        }.attach()

        return this
    }

    /**
     * 다이얼로그 Positive Button 설정
     * @param content Positive Button String (default : '확인')
     */
    fun setPositiveButtonText(content: String?): HCCommonDialog {
        binding.tvHcCommonPositiveBtn.text = if (TextUtils.isEmpty(content)) {
            context.resources.getString(R.string.ok)
        } else {
            content
        }

        return this
    }

    /**
     * 다이얼로그 Positive Button 설정
     * @param strResId Positive Button Resource ID (default : '확인')
     */
    fun setPositiveButtonText(strResId: Int): HCCommonDialog {
        binding.tvHcCommonPositiveBtn.setText(
            if (strResId <= 0) {
                R.string.ok
            } else {
                strResId
            }
        )

        return this
    }

    /**
     * 다이얼로그 Negative Button 설정
     * @param content Negative Button String (default : '취소')
     */
    fun setNegativeButtonText(content: String?): HCCommonDialog {
        binding.tvHcCommonNegativeBtn.text = content

        return this
    }

    /**
     * 다이얼로그 Negative Button 설정
     * @param strResId Negative Button Resource ID (default : '취소')
     */
    fun setNegativeButtonText(strResId: Int): HCCommonDialog {
        binding.tvHcCommonNegativeBtn.setText(strResId)

        return this
    }

    /**
     * 다이얼로그 확인 버튼 설정
     * @param content 확인 버튼 String (default : '확인')
     */
    fun setConfirmButtonText(content: String?): HCCommonDialog {
        binding.tvHcCommonConfirmBtn.text = content

        return this
    }

    /**
     * 다이얼로그 확인 버튼 설정
     * @param strResId 확인 버튼 Resource ID (default : '확인')
     */
    fun setConfirmButtonText(strResId: Int): HCCommonDialog {
        binding.tvHcCommonConfirmBtn.setText(strResId)

        return this
    }

    /**
     * 다이얼로그 버튼 클릭 시 리스너
     */
    fun setOnDismissListener(onDismissListener: OnDismissListener?): HCCommonDialog {
        this.onDismissListener = onDismissListener

        return this
    }

    override fun onClick(view: View) {
        onDismissListener?.let {
            when (view.id) {
                R.id.tv_hc_common_confirm_btn -> it.onDismiss(DialogType.BUTTON_CONFIRM.ordinal)
                R.id.tv_hc_common_negative_btn -> it.onDismiss(DialogType.BUTTON_NEGATIVE.ordinal)
                R.id.tv_hc_common_positive_btn -> it.onDismiss(DialogType.BUTTON_POSITIVE.ordinal)
            }
        }

        dismiss()
    }

    /**
     * 다이얼로그 타입에 따른 버튼 셋팅
     * @param dialogType 다이얼로그 타입 (ALERT, CONFIRM)<br></br>
     * DialogType.ALERT = 예/아니오 버튼 있는 다이얼로그<br></br>
     * DialogType.CONFIRM = 확인 용도로 사용하는 다이얼로그
     */
    private fun setButtonStyle(dialogType: DialogType) {
        val isConfirm = dialogType == DialogType.CONFIRM

        with(binding) {
            tvHcCommonConfirmBtn.visibility = if (isConfirm) View.VISIBLE else View.GONE
            tvHcCommonPositiveBtn.visibility = if (isConfirm) View.GONE else View.VISIBLE
            tvHcCommonNegativeBtn.visibility = if (isConfirm) View.GONE else View.VISIBLE
        }
    }

    interface OnDismissListener {
        fun onDismiss(menuId: Int)
    }

    companion object {
        val TAG: String = HCCommonDialog::class.java.simpleName
    }
}
