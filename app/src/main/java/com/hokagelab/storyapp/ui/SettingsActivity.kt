package com.hokagelab.storyapp.ui

import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.view.Window
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.ListPreference
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.hokagelab.storyapp.R
import com.hokagelab.storyapp.databinding.SettingsActivityBinding
import com.hokagelab.storyapp.ui.login.LoginActivity
import com.hokagelab.storyapp.ui.login.LoginViewModel
import com.hokagelab.storyapp.utils.LoginViewModelFactory
import com.hokagelab.storyapp.utils.Utils

class SettingsActivity : AppCompatActivity() {

    private var _binding: SettingsActivityBinding? = null
    private val binding get() = _binding

    private val loginViewModel: LoginViewModel by viewModels {
        LoginViewModelFactory.getInstance(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        _binding = SettingsActivityBinding.inflate(layoutInflater)
        setContentView(binding?.root)

        binding?.btnSettingLogout?.setOnClickListener {
            showLogoutDialog()
        }

        if (savedInstanceState == null) {
            supportFragmentManager
                .beginTransaction()
                .replace(R.id.settings, SettingsFragment())
                .commit()
        }
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    class SettingsFragment : PreferenceFragmentCompat() {

        private var localizationChange: Preference? = null

        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.root_preferences, rootKey)

            localizationChange = findPreference(getString(R.string.title_language))

            val localizationIntent = Intent(Settings.ACTION_LOCALE_SETTINGS)
            localizationChange?.intent = localizationIntent

            listenAppThemeChanges()
        }

        private fun listenAppThemeChanges() {
            findPreference<ListPreference>(getString(R.string.key_app_theme))?.apply {
                onPreferenceChangeListener = Preference.OnPreferenceChangeListener { _, newValue ->
                    Utils.applyTheme(newValue as String)
                    true
                }
            }
        }
    }

    private fun showLogoutDialog() {
        val dialog = Dialog(this@SettingsActivity)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(false)
        dialog.setContentView(R.layout.layout_dialog)
        val body = dialog.findViewById(R.id.tvDialogInformation) as TextView
        body.text = getString(R.string.message_logout)
        val iconDialog = dialog.findViewById(R.id.ivDialogLogo) as ImageView
        iconDialog.setImageResource(R.drawable.ic_logout_dialog)
        val layoutDialogConfirm = dialog.findViewById(R.id.layoutConfirmation) as LinearLayout
        layoutDialogConfirm.visibility = View.VISIBLE
        val confirmBtnYes = dialog.findViewById(R.id.btnDialogYes) as Button
        val confirmBtnNo = dialog.findViewById(R.id.btnDialogNo) as Button
        confirmBtnYes.setOnClickListener {
            loginViewModel.deleteToken()
            val loginIntent = Intent(this@SettingsActivity, LoginActivity::class.java)
            startActivity(loginIntent)
            finish()
        }
        confirmBtnNo.setOnClickListener {
            dialog.dismiss()
        }
        dialog.show()
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}