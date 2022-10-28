package com.hokagelab.storyapp.ui.login

import android.animation.ObjectAnimator
import android.app.Dialog
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.view.View
import android.view.Window
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.hokagelab.storyapp.R
import com.hokagelab.storyapp.data.Resource
import com.hokagelab.storyapp.databinding.ActivityLoginBinding
import com.hokagelab.storyapp.ui.main.MainActivity
import com.hokagelab.storyapp.ui.register.RegisterActivity
import com.hokagelab.storyapp.utils.LoginViewModelFactory
import com.hokagelab.storyapp.utils.Utils

@RequiresApi(Build.VERSION_CODES.O)
class LoginActivity : AppCompatActivity() {

    private var _binding: ActivityLoginBinding? = null
    private val binding get() = _binding
    private var dialog: Dialog? = null
    private val loginViewModel: LoginViewModel by viewModels {
        LoginViewModelFactory.getInstance(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding?.root)

        dialog = Dialog(this@LoginActivity)

        val email: String = intent.getStringExtra("email").toString()
        val password: String = intent.getStringExtra("password").toString()

        if (email != "null" && password != "null") {
            binding?.etEmail?.setText(email)
            binding?.etPassword?.setText(password)
        }

        setButtonLoginEnable()
        binding?.etEmail?.addTextChangedListener(loginTextWatcher)
        binding?.etPassword?.addTextChangedListener(loginTextWatcher)

        binding?.ivShowHidePass?.setOnClickListener {
            if (binding?.ivShowHidePass?.isSelected == true) {
                binding?.ivShowHidePass?.isSelected = false
                binding?.ivShowHidePass?.setImageResource(R.drawable.ic_visibility_off)
                binding?.etPassword?.transformationMethod =
                    PasswordTransformationMethod.getInstance()
            } else {
                binding?.ivShowHidePass?.isSelected = true
                binding?.ivShowHidePass?.setImageResource(R.drawable.ic_visibility)
                binding?.etPassword?.transformationMethod =
                    HideReturnsTransformationMethod.getInstance()
            }
        }

        binding?.tvHaventAccount?.setOnClickListener {
            val intent = Intent(this@LoginActivity, RegisterActivity::class.java)
            startActivity(intent)
        }

        startIntent()
        handleLogin()
        playAnimation()
    }

    private val loginTextWatcher: TextWatcher = object : TextWatcher {
        override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
        override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
            setButtonLoginEnable()
        }
        override fun afterTextChanged(s: Editable) {}
    }

    private fun setButtonLoginEnable() {
        binding?.btnLogin?.isEnabled =
            binding?.etEmail?.text.toString().isNotEmpty() &&
                    binding?.etPassword?.text.toString().isNotEmpty() &&
                    binding?.etPassword?.text.toString().length >= 6 &&
                    Utils.checkEmailValid(binding?.etEmail?.text.toString())
        if (binding?.btnLogin?.isEnabled == true) {
            binding?.btnLogin?.setBackgroundResource(R.drawable.blue_button_active_background)
        } else {
            binding?.btnLogin?.setBackgroundResource(R.drawable.blue_button_disable_background)
        }
    }

    private fun handleLogin() {
        binding?.btnLogin?.setOnClickListener {
            val email = binding?.etEmail?.text.toString()
            val password = binding?.etPassword?.text.toString()

            loginViewModel.loginAccount(email, password)
                .observe(this) { result ->
                    when (result) {
                        is Resource.Loading -> {
                            showLoadingProcess(true)
                        }

                        is Resource.Success -> {
                            showLoadingProcess(false)
                            if (!result.data.error){
                                val token = result.data.loginResult.token
                                loginViewModel.saveToken(token)
                            }
                        }

                        is Resource.Error -> {
                            showLoadingProcess(false)
                            binding?.root?.let { result.error.showMessage() }
                        }
                    }
                }
        }

    }

    private fun playAnimation() {
        ObjectAnimator.ofFloat(binding?.ivLogo, View.TRANSLATION_X, -30f, 30f).apply {
            duration = 6000
            repeatCount = ObjectAnimator.INFINITE
            repeatMode = ObjectAnimator.REVERSE
        }.start()
    }

    private fun startIntent() {
        loginViewModel.getToken().observe(this){ token ->
            if (!token.equals("")){
                processToMainActivity(token)
            }
        }
    }

    private fun processToMainActivity(token: String) {
        val intent = Intent(this@LoginActivity, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
        intent.putExtra("token", token)
        startActivity(intent)
        finish()
    }

    private fun String.showMessage() {
        dialog?.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog?.setCancelable(false)
        dialog?.setContentView(R.layout.layout_dialog)
        val body = dialog?.findViewById(R.id.tvDialogInformation) as TextView
        body.text = this
        val iconDialog = dialog?.findViewById(R.id.ivDialogLogo) as ImageView
        val confirmBtn = dialog?.findViewById(R.id.btnDialogAction) as Button
        confirmBtn.visibility = View.VISIBLE
        iconDialog.setImageResource(R.drawable.ic_failed)
        confirmBtn.setBackgroundResource(R.drawable.red_button_background)
        confirmBtn.setOnClickListener {
            dialog?.dismiss()
        }
        dialog?.show()
    }

    private fun showLoadingProcess(isLoading: Boolean) {
        binding?.btnLogin?.visibility = if (isLoading) View.GONE else View.VISIBLE
        binding?.tvHaventAccount?.visibility = if (isLoading) View.GONE else View.VISIBLE
        binding?.progressBarLogin?.visibility = if (isLoading) View.VISIBLE else View.GONE
    }

    override fun onStop() {
        super.onStop()
        dialog?.dismiss()
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
        dialog?.dismiss()
    }
}