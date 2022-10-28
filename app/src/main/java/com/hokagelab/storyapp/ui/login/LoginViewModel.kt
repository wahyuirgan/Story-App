package com.hokagelab.storyapp.ui.login

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hokagelab.storyapp.data.LoginRepository
import com.hokagelab.storyapp.data.PreferenceRepository
import kotlinx.coroutines.launch

class LoginViewModel(private val loginRepository: LoginRepository, private val preferenceRepository: PreferenceRepository) : ViewModel() {
    fun loginAccount(email: String, password: String) =
        loginRepository.loginAccount(email, password)
    fun saveToken(token: String) {
        viewModelScope.launch {
            preferenceRepository.saveToken(token)
        }
    }
    fun getToken(): LiveData<String> = preferenceRepository.getToken()
    fun deleteToken() {
        viewModelScope.launch {
            preferenceRepository.deleteToken()
        }
    }
}