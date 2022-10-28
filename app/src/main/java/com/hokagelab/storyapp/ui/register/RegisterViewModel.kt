package com.hokagelab.storyapp.ui.register

import androidx.lifecycle.ViewModel
import com.hokagelab.storyapp.data.RegisterRepository

class RegisterViewModel(private val registerRepository: RegisterRepository) : ViewModel() {
    fun createAccount(name: String, email: String, password: String) =
        registerRepository.registerAccount(name, email, password)
}