package com.hokagelab.storyapp.ui.register

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
import com.hokagelab.storyapp.databinding.ActivityRegisterBinding
import com.hokagelab.storyapp.ui.login.LoginActivity
import com.hokagelab.storyapp.utils.RegisterViewModelFactory
import com.hokagelab.storyapp.utils.Utils

@RequiresApi(Build.VERSION_CODES.O)
class RegisterActivity : AppCompatActivity() {

    private var _binding: ActivityRegisterBinding? = null
    private val binding get() = _binding
    private var dialog: Dialog? = null
    private val registerViewModel: RegisterViewModel by viewModels {
        RegisterViewModelFactory.getInstance()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding?.root)

        dialog = Dialog(this@RegisterActivity)

        setButtonRegisterEnable()
        binding?.etName?.addTextChangedListener(registerTextWatcher)
        binding?.etEmail?.addTextChangedListener(registerTextWatcher)
        binding?.etPassword?.addTextChangedListener(registerTextWatcher)

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

        binding?.tvHaveAccount?.setOnClickListener {
            val intent = Intent(this@RegisterActivity, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }

        handleRegister()
        playAnimation()
    }

    private val registerTextWatcher: TextWatcher = object : TextWatcher {
        override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
        override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
            setButtonRegisterEnable()
        }
        override fun afterTextChanged(s: Editable) {}
    }

    private fun setButtonRegisterEnable() {
        binding?.btnRegister?.isEnabled =
            binding?.etName?.text.toString().isNotEmpty() &&
            binding?.etEmail?.text.toString().isNotEmpty() &&
            binding?.etPassword?.text.toString().isNotEmpty() &&
            binding?.etPassword?.text.toString().length >= 6 &&
            Utils.checkEmailValid(binding?.etEmail?.text.toString())
        if (binding?.btnRegister?.isEnabled == true) {
            binding?.btnRegister?.setBackgroundResource(R.drawable.blue_button_active_background)
        } else {
            binding?.btnRegister?.setBackgroundResource(R.drawable.blue_button_disable_background)
        }
    }

    private fun handleRegister() {
        binding?.btnRegister?.setOnClickListener {
            val name = binding?.etName?.text.toString()
            val email = binding?.etEmail?.text.toString()
            val password = binding?.etPassword?.text.toString()

            registerViewModel.createAccount(name, email, password)
                .observe(this) { result ->
                    when (result) {
                        is Resource.Loading -> {
                            showLoadingProcess(true)
                        }

                        is Resource.Success -> {
                            showLoadingProcess(false)
                            binding?.root?.let { result.data.message.showMessage(email, password) }
                        }

                        is Resource.Error -> {
                            showLoadingProcess(false)
                            binding?.root?.let { result.error.showMessage("","") }
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

    private fun String.showMessage(email: String, password: String) {
        dialog?.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog?.setCancelable(false)
        dialog?.setContentView(R.layout.layout_dialog)
        val body = dialog?.findViewById(R.id.tvDialogInformation) as TextView
        body.text = this
        val iconDialog = dialog?.findViewById(R.id.ivDialogLogo) as ImageView
        val confirmBtn = dialog?.findViewById(R.id.btnDialogAction) as Button
        confirmBtn.visibility = View.VISIBLE
        if (this == "User created"){
            iconDialog.setImageResource(R.drawable.ic_success)
            confirmBtn.setBackgroundResource(R.drawable.green_button_background)
        } else if (this == "Email is already taken") {
            iconDialog.setImageResource(R.drawable.ic_warning)
            confirmBtn.setBackgroundResource(R.drawable.yellow_button_background)
        } else {
            iconDialog.setImageResource(R.drawable.ic_failed)
            confirmBtn.setBackgroundResource(R.drawable.red_button_background)
        }
        confirmBtn.setOnClickListener {
            if (this == "User created"){
                val intent = Intent(this@RegisterActivity, LoginActivity::class.java)
                intent.putExtra("email", email)
                intent.putExtra("password", password)
                startActivity(intent)
                finish()
            }else {
                dialog?.dismiss()
            }
        }
        dialog?.show()
    }

    private fun showLoadingProcess(isLoading: Boolean) {
        binding?.btnRegister?.visibility = if (isLoading) View.GONE else View.VISIBLE
        binding?.tvHaveAccount?.visibility = if (isLoading) View.GONE else View.VISIBLE
        binding?.progressBarRegister?.visibility = if (isLoading) View.VISIBLE else View.GONE
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