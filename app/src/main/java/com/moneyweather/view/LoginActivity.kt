package com.moneyweather.view

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.moneyweather.BuildConfig
import com.moneyweather.R
import com.moneyweather.base.BaseKotlinActivity
import com.moneyweather.base.BaseKotlinViewModel
import com.moneyweather.databinding.ActivityLoginBinding
import com.moneyweather.listener.AppFinishListener
import com.moneyweather.view.fragment.LoginFragment
import com.moneyweather.viewmodel.LoginViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class LoginActivity : BaseKotlinActivity<ActivityLoginBinding, BaseKotlinViewModel>(), View.OnClickListener,
    AppFinishListener {
    override val layoutResourceId: Int get() = R.layout.activity_login
    override val viewModel: LoginViewModel by viewModels()

    override fun initStartView() {

        viewDataBinding.apply {
            replaceFragment(R.id.fragmentContainer, LoginFragment::class.java, intent)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onFinish(dialog: Dialog) {

    }

    companion object {

        private var googleLoginModule: GoogleSignInClient? = null

        fun getGoogleLoginModule(context: Context) = try {
            if (googleLoginModule == null) {
                val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                    .requestIdToken(BuildConfig.GOOGLE_CLIENT_ID)
                    .requestEmail()
                    .build()

                googleLoginModule = GoogleSignIn.getClient(context, gso)
                googleLoginModule
            } else {
                googleLoginModule
            }
        } catch (e: Exception) {
            null
        }
    }
}