package com.hokagelab.storyapp.utils

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.hokagelab.storyapp.data.RegisterRepository
import com.hokagelab.storyapp.ui.register.RegisterViewModel

class RegisterViewModelFactory private constructor(private val registerRepository: RegisterRepository) :
    ViewModelProvider.NewInstanceFactory() {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(RegisterViewModel::class.java) -> {
                RegisterViewModel(registerRepository) as T
            }
            else -> throw IllegalArgumentException("Unknown ViewModel Class:" + modelClass.name)
        }

    }

    companion object {
        @Volatile
        private var instance: RegisterViewModelFactory? = null
        fun getInstance(): RegisterViewModelFactory = instance ?: synchronized(this) {
            instance ?: RegisterViewModelFactory(Utils.providerRegisterRepository())
        }.also { instance = it }
    }
}