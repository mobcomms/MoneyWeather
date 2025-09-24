package com.moneyweather.view.fragment

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.kakao.sdk.auth.model.OAuthToken
import com.kakao.sdk.common.model.AuthError
import com.kakao.sdk.user.UserApiClient
import com.moneyweather.R
import com.moneyweather.base.BaseActivity
import com.moneyweather.base.BaseKotlinFragment
import com.moneyweather.databinding.FragmentLoginBinding
import com.moneyweather.model.AppInfo
import com.moneyweather.model.User
import com.moneyweather.model.enums.SignUpType
import com.moneyweather.util.CustomToast
import com.moneyweather.util.FirebaseAnalyticsManager
import com.moneyweather.util.Logger
import com.moneyweather.util.PrefRepository
import com.moneyweather.view.LoginActivity
import com.moneyweather.view.MainActivity
import com.moneyweather.view.RecommendCodeInputActivity
import com.moneyweather.viewmodel.LoginViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class LoginFragment : BaseKotlinFragment<FragmentLoginBinding, LoginViewModel>() {
    override val layoutResourceId: Int get() = R.layout.fragment_login
    override val viewModel: LoginViewModel by viewModels()

    private var instanceBundle: Bundle? = null

    override fun initStartView() {
        FirebaseAnalyticsManager.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW, Bundle().apply {
            putString(FirebaseAnalyticsManager.VIEW_NAME, "로그인")
        })

        // NotificationUtil.createNotificationChannel(activity)
        // NotificationUtil.createAlarmChannel(activity)
        viewDataBinding.vm = viewModel

        initEventSetting()
        initViewModelSetting()

    }

    private fun initViewModelSetting() {
        viewModel.loginResult.observe(this, Observer {
            if (it) {
                viewModel.connectConfigMy()

                PrefRepository.UserInfo.isFirstRun = false
                activity?.finishAffinity()

                val inviteRecommendCode = PrefRepository.UserInfo.inviteRecommendCode
                if (inviteRecommendCode.isNotEmpty()) {
                    startActivity(RecommendCodeInputActivity::class.java)
                } else {
                    startActivity(MainActivity::class.java)
                }
            } else {
                Toast.makeText(context, R.string.error_network, Toast.LENGTH_SHORT).show()
            }

        })

    }

    private fun initEventSetting() {
        viewDataBinding.apply {

            btnGoogle.setOnClickListener {
                googleLogin()
            }

            btnKakao.setOnClickListener {
                kakaoLogin()
            }

            btnGuest.setOnClickListener {
                PrefRepository.UserInfo.isFirstRun = false
                PrefRepository.UserInfo.isGuestLogin = true

                AppInfo.setUserInfo(
                    User(
                        email = getString(R.string.guest),
                        name = getString(R.string.user_type_guest),
                        phone = "",
                        userId = ""
                    )
                )

                val intent = Intent(context, MainActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
                }
                startActivity(intent)
                requireActivity().finish()
            }

            btnEmail.setOnClickListener {
                startFragment(R.id.fragmentContainer, LoginEmailFragment::class.java)
            }
        }
    }

    private fun googleLogin() {
        FirebaseAnalyticsManager.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW, Bundle().apply {
            putString(FirebaseAnalyticsManager.VIEW_NAME, "구글 로그인")
        })

        val signIntent = LoginActivity.getGoogleLoginModule(requireContext())?.signInIntent
        requestGoogleLogin.launch(signIntent)
    }

    private val requestGoogleLogin = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == BaseActivity.RESULT_OK) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            try {
                val account = task.getResult(ApiException::class.java)

                firebaseAuthWithGoogle(account.idToken)


                Logger.v("login_info", "firebaseAuthWithGoogle:  " + "account - id : ${account.id} , email : ${account.email}")
            } catch (e: ApiException) {
                Log.d("tsshin", "ApiException : ${e.message}")
            }
        } else {
            Log.d("tsshin", "requestGoogleLogin fail : ${result.data?.extras} ")
        }
    }

    /**
     * 구글 로그인 - 파이어베이스 연동
     */
    private fun firebaseAuthWithGoogle(idToken: String?) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        activity.run {
            FirebaseAuth.getInstance().signInWithCredential(credential).addOnCompleteListener() { task: Task<AuthResult?> ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    val user = FirebaseAuth.getInstance().currentUser
                    user.let {
                        FirebaseAnalyticsManager.logEvent(FirebaseAnalytics.Event.LOGIN, Bundle().apply {
                            putString(FirebaseAnalyticsManager.LOGIN_TYPE, "google")
                        })

//                        mSnsEmail = user.email
//                        mSnsNickname = user.displayName
//                        mSnsPhoneNum = user.phoneNumber
//                        Log.d("tsshin", "uid : ${user!!.uid} /  email : ${user!!.email} ")
                        viewModel.serviceLogin(SignUpType.GOOGLE, user!!.uid ?: "", user!!.email ?: "", user!!.displayName ?: "")
                    }
                } else {
                    Log.d("tsshin", "FirebaseAuth : fail")
                }
            }
        }

    }

    private fun kakaoLogin() {
        with(UserApiClient.instance) {
            if (isKakaoTalkLoginAvailable(requireContext())) {
                loginWithKakaoTalk(
                    context = requireContext(),
                    callback = kakaoLoginCallback
                )
            } else {
                loginWithKakaoAccount(
                    context = requireContext(),
                    callback = kakaoLoginCallback
                )
            }
        }
    }

    /**
     * 카카오 로그인 콜백
     */
    private val kakaoLoginCallback: Function2<OAuthToken?, Throwable?, Unit> =
        label@{ token: OAuthToken?, loginError: Throwable? ->
            try {
                FirebaseAnalyticsManager.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW, Bundle().apply {
                    putString(FirebaseAnalyticsManager.VIEW_NAME, "카카오 로그인")
                })

                if (loginError != null) {
                    // Logger.e("kakao login.. errorMessage=${loginError.message}")

                    if (loginError is AuthError) {
                        // Logger.e("kakao login.. errorCode=${loginError.statusCode}")

                        when (loginError.statusCode) {
                            302 -> {
                                // KakaoTalk is installed but not connected to Kakao account.
                                CustomToast.showToast(requireContext(), getString(R.string.warning_kakao_not_login))
                            }
                        }
                        null
                    }
                } else {
                    // 사용자 정보 요청
                    UserApiClient.instance.me { user: com.kakao.sdk.user.model.User?, meError: Throwable? ->
                        if (meError != null) {
                            // Logger.e("kakao me.. errorMessage=${meError.message}")
                        } else {
                            if (user != null) {
                                val account = user.kakaoAccount
                                if (account != null) {
                                    var email = ""
                                    if (!account.email.isNullOrEmpty()) {
                                        email = account.email!!
                                    }

                                    var mSnsNickname = ""
                                    if (account.profile != null) {
                                        mSnsNickname = if (account.profile!!.nickname.isNullOrEmpty()) "" else account.profile!!.nickname!!
                                    }

                                    viewModel.serviceLogin(SignUpType.KAKAO, user.id.toString(), email, mSnsNickname)

                                    FirebaseAnalyticsManager.logEvent(FirebaseAnalytics.Event.LOGIN, Bundle().apply {
                                        putString(FirebaseAnalyticsManager.LOGIN_TYPE, "kakao")
                                    })
                                }
                            }
                        }
                        null
                    }
                }
                null
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
}