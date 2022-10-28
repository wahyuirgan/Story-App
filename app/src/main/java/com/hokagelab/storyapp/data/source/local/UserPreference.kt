package com.hokagelab.storyapp.data.source.local

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class UserPreference private constructor(private val dataStore: DataStore<Preferences>) {

    suspend fun saveToken(token: String) {
        dataStore.edit { preferences ->
            preferences[USER_TOKEN] = token
        }
    }

    fun getToken(): Flow<String> {
        return dataStore.data.map { preferences ->
            preferences[USER_TOKEN] ?: ""
        }
    }

    suspend fun deleteToken() {
        dataStore.edit {
            it.clear()
        }
    }

    companion object {
        private val USER_TOKEN = stringPreferencesKey("token")

        @Volatile
        private var instance: UserPreference? = null

        fun getInstance(dataStore: DataStore<Preferences>): UserPreference {
            return instance ?: synchronized(this) {
                val instance = UserPreference(dataStore)
                this.instance = instance
                instance
            }
        }
    }
}