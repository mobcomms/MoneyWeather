package com.moneyweather.view

import android.app.AlertDialog
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.ContactsContract
import android.text.TextUtils
import android.view.Gravity
import android.view.KeyEvent
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.widget.AppCompatEditText
import androidx.lifecycle.Observer
import com.google.firebase.analytics.FirebaseAnalytics
import com.kakao.sdk.share.ShareClient
import com.kakao.sdk.share.model.SharingResult
import com.kakao.sdk.template.model.Button
import com.kakao.sdk.template.model.Content
import com.kakao.sdk.template.model.FeedTemplate
import com.kakao.sdk.template.model.Link
import com.moneyweather.R
import com.moneyweather.base.BaseKotlinActivity
import com.moneyweather.databinding.ActivityInviteBinding
import com.moneyweather.model.enums.ActionBarLeftButtonEnum
import com.moneyweather.util.CustomToast
import com.moneyweather.util.FirebaseAnalyticsManager
import com.moneyweather.util.Logger
import com.moneyweather.util.PrefRepository
import com.moneyweather.view.PhoneCertifiedActivity.Companion.DATA_USER_CERTIFICATION
import com.moneyweather.view.PhoneCertifiedActivity.Companion.RESULT_SUCCESS
import com.moneyweather.viewmodel.InviteFriendViewModel
import dagger.hilt.android.AndroidEntryPoint
import org.apache.commons.lang3.StringUtils

@AndroidEntryPoint
class InviteFriendActivity : BaseKotlinActivity<ActivityInviteBinding, InviteFriendViewModel>(),
    View.OnClickListener {

    override val layoutResourceId: Int get() = R.layout.activity_invite
    override val viewModel: InviteFriendViewModel by viewModels()

    private var kakaoRedirectUrl: String? = null
    private var mShortLink: String? = null
    private var mInviteCode: String? = null
    private var mInviteImgUrl: String? = null
    private var mInviteMsg: String? = null
    private var isRedeemed: Boolean = false
    private var isVerified: Boolean = false
    private var isClickable = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun initStartView() {
        FirebaseAnalyticsManager.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW, Bundle().apply {
            putString(FirebaseAnalyticsManager.VIEW_NAME, "친구초대")
        })

        viewDataBinding.vm = viewModel

        initActionBar(
            viewDataBinding.iActionBar,
            R.string.invate,
            ActionBarLeftButtonEnum.BACK_BUTTON
        )

        viewModel.isRedeemed.observe(this@InviteFriendActivity, {
            if (it) {
                viewDataBinding.btnInputFriendCode.text = getString(R.string.invate_completed)
                viewDataBinding.btnInputFriendCode.isClickable = false
                isClickable = false
            } else {
                viewDataBinding.btnInputFriendCode.text = getString(R.string.invate_input_friend_code)
                viewDataBinding.btnInputFriendCode.isClickable = true
                isClickable = true
            }
        })

        viewModel.resultData.observe(this@InviteFriendActivity, Observer {
            it?.let {
                Logger.d("data=$it")
                kakaoRedirectUrl = it.kakaoRedirectUrl
                mShortLink = it.redirectUrl
                mInviteCode = it.inviteCode
                mInviteImgUrl = it.imageUrl
                mInviteMsg = it.description
                isRedeemed = it.isRedeemed
                isVerified = it.isVerified

                if (isRedeemed) {
                    viewDataBinding.btnInputFriendCode.text = getString(R.string.invate_completed)
                    viewDataBinding.btnInputFriendCode.isClickable = false
                    isClickable = false
                } else {
                    viewDataBinding.btnInputFriendCode.text = getString(R.string.invate_input_friend_code)
                    viewDataBinding.btnInputFriendCode.isClickable = true
                    isClickable = true
                }
            }
        })

        viewDataBinding.apply {

            btnInputFriendCode.setOnClickListener {
                if (isClickable) {
                    if (isVerified) { // 본인 인증이 된 경우 친구 코드 입력
                        inputCodeForInviteRedeem()
                    } else { // 본인 인증이 안된 경우 본인 인증
                        val dialog = AlertDialog.Builder(this@InviteFriendActivity).create()

                        val view = layoutInflater.inflate(R.layout.dialog_input, null)

                        view.findViewById<TextView>(R.id.txtTitle).text = getString(R.string.invate_dialog_verification_message)
                        view.findViewById<AppCompatEditText>(R.id.inputText).visibility = View.GONE

                        val button = view.findViewById<android.widget.Button>(R.id.btnInviteFinish)
                        button.text = getString(R.string.invate_dialog_verification_button)
                        button.setOnClickListener {
                            startActivityForResult(PhoneCertifiedActivity::class.java, VERIFICATION_REQUEST_CODE)
                            dialog.dismiss()
                        }

                        dialog.setView(view)
                        dialog.show()
                    }
                }
            }

            btnInvate.setOnClickListener {
                if (!PrefRepository.UserInfo.isLogin) {
                    CustomToast.showToast(this@InviteFriendActivity, R.string.message_non_login_user)
                } else {
                    showSharePopup()
                }
            }

            copyLayout.setOnClickListener {
                setClipBoardLink(mInviteCode)
            }

        }

        viewModel.connectInviteInfo()


        var intent = Intent()
        intent.putExtra("isRedeemed", isRedeemed)
        setResult(RESULT_OK, intent)
    }

    override fun onBackPressed() {
        finish()
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    private fun showSharePopup() {
        var builder2: AlertDialog.Builder? = null
        builder2 = if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) AlertDialog.Builder(this) else AlertDialog.Builder(
            this,
            android.R.style.Theme_DeviceDefault_Light_Dialog_Alert
        )
        val dialogView = layoutInflater.inflate(R.layout.invite_popup, null)
        builder2.setView(dialogView)
        builder2.setCustomTitle(null)
        val mEndingAd = builder2.create()
        mEndingAd.window!!.setGravity(Gravity.CENTER)
        mEndingAd.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialogView.findViewById<View>(R.id.btnInviteKakao).setOnClickListener {
            inviteKakao()
            mEndingAd.dismiss()
        }
        dialogView.findViewById<View>(R.id.btnInviteMessage).setOnClickListener {
            inviteMessage()
            mEndingAd.dismiss()
        }

        dialogView.findViewById<View>(R.id.btnUrlCopy).setOnClickListener {
            val msg =  /*getString(R.string.invite_message)*/"""
                $mInviteMsg
                
                $mShortLink
                """.trimIndent()
            if (!TextUtils.isEmpty(msg)) {
                setClipBoardLink(msg)
            }
            mEndingAd.dismiss()
        }

        mEndingAd.setOnKeyListener { arg0, keyCode, event ->
            if (keyCode == KeyEvent.KEYCODE_BACK && event.action == KeyEvent.ACTION_DOWN) {
                mEndingAd.dismiss()
            }
            true
        }
        mEndingAd.show()
    }

    /**
     * 초대하기 - 카카오톡
     */
    private fun inviteKakao() {
        if (ShareClient.instance.isKakaoTalkSharingAvailable(applicationContext)) {

//            String description = getString(R.string.invite_message);
            val link = Link(kakaoRedirectUrl, kakaoRedirectUrl, null, null)
            val content = Content("", mInviteImgUrl!!, link, mInviteMsg)
            val button = Button("초대하기", link)
            val buttonList: MutableList<Button> = ArrayList()
            buttonList.add(button)
            val feedTemplate = FeedTemplate(content, null, null, buttonList)
            ShareClient.instance.shareDefault(applicationContext, feedTemplate) { linkResult: SharingResult?, error: Throwable? ->
                if (error != null) {
                    Logger.v("카카오링크 보내기 실패" + error.message)
                    // Toast.makeText(applicationContext, "실패" + error.message, Toast.LENGTH_SHORT).show()
                } else if (linkResult != null) {
                    Logger.d("카카오링크 보내기 성공 " + linkResult.intent)
                    startActivity(linkResult.intent)
                    // 카카오링크 보내기에 성공했지만 아래 경고 메시지가 존재할 경우 일부 컨텐츠가 정상 동작하지 않을 수 있습니다.
                    Logger.w("Warning Msg: " + linkResult.warningMsg)
                    Logger.w("Argument Msg: " + linkResult.argumentMsg)
                }
                null
            }
        } else {
            Toast.makeText(applicationContext, R.string.sns_not_installed, Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * 초대하기 - 메시지
     */
    private fun inviteMessage() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.data = ContactsContract.CommonDataKinds.Phone.CONTENT_URI
        startActivityForResult(intent, 100)
    }

    /**
     * 초대하기 - 메시지
     */
    private fun sendSMS(data: Intent?) {
        val msg =  /*getString(R.string.invite_message)*/"""
             $mInviteMsg
             
             $mShortLink
             """.trimIndent()
        val cursor = contentResolver.query(
            data!!.data!!, arrayOf(
                ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
                ContactsContract.CommonDataKinds.Phone.NUMBER
            ), null, null, null
        )
        cursor!!.moveToFirst()
        val name = cursor.getString(0) //0은 이름을 얻어옵니다.
        val number = cursor.getString(1) //1은 번호를 받아옵니다.
        cursor.close()
        val n = Uri.parse("smsto: $number")
        val intent = Intent(Intent.ACTION_SENDTO, n)
        intent.putExtra("sms_body", msg)
        startActivity(intent)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == RESULT_OK && requestCode == 100) {
            sendSMS(data)
        }

        if (RESULT_SUCCESS == resultCode && VERIFICATION_REQUEST_CODE == requestCode) {
            data?.let {
                val isSuccess = it.getBooleanExtra(DATA_USER_CERTIFICATION, false)
                if (isSuccess) {
                    Logger.d("본인 인증 완료")
                    inputCodeForInviteRedeem()
                }
            }
        }

        super.onActivityResult(requestCode, resultCode, data)
    }

    /**
     * 클립보드에 주소 복사 기능
     *
     * @param link
     */
    fun setClipBoardLink(link: String?) {
        val clipboardManager = getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
        val clipData = ClipData.newPlainText("label", link)
        clipboardManager.setPrimaryClip(clipData)
        CustomToast.showToast(this@InviteFriendActivity, resources.getString(R.string.clipboard_msg))
    }

    /**
     * 친구 코드 입력
     */
    fun inputCodeForInviteRedeem() {
        val view = layoutInflater.inflate(R.layout.dialog_input, null)

        val dialog = AlertDialog.Builder(this).create()
        dialog.setView(view)
        dialog.show()

        view.findViewById<android.widget.Button>(R.id.btnInviteFinish).setOnClickListener {
            var code = view.findViewById<AppCompatEditText>(R.id.inputText).text.toString()
            if (StringUtils.isNotEmpty(code)) {
                Logger.d("code=$code")
                viewModel.inviteRedeem(code)
                dialog.dismiss()
            } else {
                Toast.makeText(this@InviteFriendActivity, getString(R.string.invate_dialog_input_code), Toast.LENGTH_SHORT).show()
            }
        }
    }

    companion object {
        const val VERIFICATION_REQUEST_CODE = 101
    }
}