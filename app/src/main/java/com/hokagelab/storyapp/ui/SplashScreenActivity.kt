package com.hokagelab.storyapp.ui

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.CountDownTimer
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.PreferenceManager
import com.hokagelab.storyapp.BuildConfig
import com.hokagelab.storyapp.R
import com.hokagelab.storyapp.databinding.ActivitySplashScreenBinding
import com.hokagelab.storyapp.ui.login.LoginActivity
import com.hokagelab.storyapp.ui.login.LoginViewModel
import com.hokagelab.storyapp.ui.main.MainActivity
import com.hokagelab.storyapp.utils.LoginViewModelFactory
import com.hokagelab.storyapp.utils.Utils

@RequiresApi(Build.VERSION_CODES.O)
@SuppressLint("CustomSplashScreen")
class SplashScreenActivity : AppCompatActivity() {
    private var _binding: ActivitySplashScreenBinding? = null
    private val binding get() = _binding
    private val loginViewModel: LoginViewModel by viewModels {
        LoginViewModelFactory.getInstance(this)
    }
    var clicked = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivitySplashScreenBinding.inflate(layoutInflater)
        setContentView(binding?.root)

        binding?.tvCountdown?.setOnClickListener {
            startIntent()
            clicked = true
        }

        binding?.tvVersionApp?.text = getString(R.string.label_version_app).plus(" ").plus(
            BuildConfig.VERSION_NAME)

        object : CountDownTimer(5000, 1000) {

            override fun onTick(millisUntilFinished: Long) {
                ("" + millisUntilFinished / 1000 + getString(R.string.text_skip)).also { binding?.tvCountdown?.text = it }
            }

            override fun onFinish() {
                if (!clicked) {
                    startIntent()
                }
            }
        }.start()

        applyTheme()
    }

    private fun startIntent() {
        loginViewModel.getToken().observe(this){ token ->
            if (!token.equals("")){
                val intent = Intent(this@SplashScreenActivity, MainActivity::class.java)
                intent.putExtra("token", token)
                startActivity(intent)
                finish()
            } else {
                val intent = Intent(this@SplashScreenActivity, LoginActivity::class.java)
                startActivity(intent)
                finishAffinity()
            }
        }
    }

    private fun applyTheme() {
        PreferenceManager.getDefaultSharedPreferences(this)
            .getString(getString(R.string.key_app_theme), Utils.DEFAULT_MODE)?.apply {
                Utils.applyTheme(this)
            }
    }
}