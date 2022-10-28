package com.hokagelab.storyapp.data

import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import com.hokagelab.storyapp.data.source.local.UserPreference

class PreferenceRepository (
    private val userPreference: UserPreference
)
{
    suspend fun saveToken(token: String) {
        userPreference.saveToken(token)
    }

    fun getToken(): LiveData<String> = userPreference.getToken().asLiveData()

    suspend fun deleteToken() = userPreference.deleteToken()

    companion object {
        @Volatile
        private var instance: PreferenceRepository? = null
        fun getInstance(
            userPreference: UserPreference
        ): PreferenceRepository = instance ?: synchronized(this) {
            instance ?: PreferenceRepository(userPreference)
        }.also { instance = it }
    }
}