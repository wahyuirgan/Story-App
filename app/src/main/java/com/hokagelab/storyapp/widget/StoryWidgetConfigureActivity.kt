package com.hokagelab.storyapp.widget

import android.app.Dialog
import android.appwidget.AppWidgetManager
import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
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
import com.hokagelab.storyapp.R
import com.hokagelab.storyapp.data.Resource
import com.hokagelab.storyapp.databinding.ActivityStoryWidgetConfigureBinding
import com.hokagelab.storyapp.ui.login.LoginViewModel
import com.hokagelab.storyapp.utils.LoginViewModelFactory
import com.hokagelab.storyapp.utils.Utils

@RequiresApi(Build.VERSION_CODES.O)
class StoryWidgetConfigureActivity : AppCompatActivity() {

    private var _binding: ActivityStoryWidgetConfigureBinding? = null
    private val binding get() = _binding
    private var appWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID
    private var dialog: Dialog? = null
    private val loginViewModel: LoginViewModel by viewModels {
        LoginViewModelFactory.getInstance(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        _binding = ActivityStoryWidgetConfigureBinding.inflate(layoutInflater)
        setContentView(binding?.root)

        dialog = Dialog(this@StoryWidgetConfigureActivity)

        val extras = intent.extras
        if (extras != null) {
            appWidgetId = extras.getInt(
                AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID
            )
        }

        if (appWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            finish()
            return
        }

        loginViewModel.getToken().observe(this) {
            if (it.isNotEmpty()) {
                showWidget()
            }
        }

        setButtonLoginEnable()
        binding?.etEmailWidget?.addTextChangedListener(loginTextWatcher)
        binding?.etPasswordWidget?.addTextChangedListener(loginTextWatcher)

        binding?.ivShowHidePassWidget?.setOnClickListener {
            if (binding?.ivShowHidePassWidget?.isSelected == true) {
                binding?.ivShowHidePassWidget?.isSelected = false
                binding?.ivShowHidePassWidget?.setImageResource(R.drawable.ic_visibility_off)
                binding?.etPasswordWidget?.transformationMethod =
                    PasswordTransformationMethod.getInstance()
            } else {
                binding?.ivShowHidePassWidget?.isSelected = true
                binding?.ivShowHidePassWidget?.setImageResource(R.drawable.ic_visibility)
                binding?.etPasswordWidget?.transformationMethod =
                    HideReturnsTransformationMethod.getInstance()
            }
        }

        handleLogin()
    }

    private val loginTextWatcher: TextWatcher = object : TextWatcher {
        override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
        override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
            setButtonLoginEnable()
        }
        override fun afterTextChanged(s: Editable) {}
    }

    private fun setButtonLoginEnable() {
        binding?.btnLoginWidget?.isEnabled =
            binding?.etEmailWidget?.text.toString().isNotEmpty() &&
                    binding?.etPasswordWidget?.text.toString().isNotEmpty() &&
                    binding?.etPasswordWidget?.text.toString().length >= 6 &&
                    Utils.checkEmailValid(binding?.etEmailWidget?.text.toString())
        if (binding?.btnLoginWidget?.isEnabled == true) {
            binding?.btnLoginWidget?.setBackgroundResource(R.drawable.blue_button_active_background)
        } else {
            binding?.btnLoginWidget?.setBackgroundResource(R.drawable.blue_button_disable_background)
        }
    }

    private fun handleLogin() {
        binding?.btnLoginWidget?.setOnClickListener {
            val email = binding?.etEmailWidget?.text.toString()
            val password = binding?.etPasswordWidget?.text.toString()

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
        binding?.btnLoginWidget?.visibility = if (isLoading) View.GONE else View.VISIBLE
        binding?.progressBarLoginWidget?.visibility = if (isLoading) View.VISIBLE else View.GONE
    }

    private fun showWidget() {
        val appWidgetManager = AppWidgetManager.getInstance(this)
        StoryWidget.updateAppWidget(this, appWidgetManager, appWidgetId)

        val resultValue = Intent().apply {
            putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
        }
        setResult(RESULT_OK, resultValue)
        finish()
    }
}