package com.hokagelab.storyapp.utils

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.hokagelab.storyapp.data.LoginRepository
import com.hokagelab.storyapp.data.PreferenceRepository
import com.hokagelab.storyapp.ui.login.LoginViewModel

class LoginViewModelFactory private constructor(private val loginRepository: LoginRepository, private val preferenceRepository: PreferenceRepository) :
    ViewModelProvider.NewInstanceFactory() {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(LoginViewModel::class.java) -> {
                LoginViewModel(loginRepository, preferenceRepository) as T
            }
            else -> throw IllegalArgumentException("Unknown ViewModel Class:" + modelClass.name)
        }

    }

    companion object {
        @Volatile
        private var instance: LoginViewModelFactory? = null
        fun getInstance(context: Context): LoginViewModelFactory = instance ?: synchronized(this) {
            instance ?: LoginViewModelFactory(Utils.providerLoginRepository(), Utils.providerPreferenceRepository(context))
        }.also { instance = it }
    }
}