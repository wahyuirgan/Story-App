package com.hokagelab.storyapp.data


import androidx.lifecycle.LiveData
import androidx.lifecycle.liveData
import com.hokagelab.storyapp.data.Resource.*
import com.hokagelab.storyapp.data.source.remote.network.ApiService
import com.hokagelab.storyapp.data.source.remote.response.LoginResponse
import org.json.JSONObject
import retrofit2.HttpException

class LoginRepository (
    private val apiService: ApiService
)
{
    fun loginAccount(
        email: String,
        password: String
    ): LiveData<Resource<LoginResponse.Response>> =
        liveData {
            emit(Loading)
            try {
                val response = apiService.login(email, password)
                emit(Success(response))
            } catch (e: Exception) {
                when (e) {
                    is HttpException -> {
                        val message = e.getErrorMessage()
                        if (message != null) {
                            emit(Error(message))
                        }
                    }
                    else -> {
                        emit(Error(e.message.toString()))
                    }
                }
            }
        }

    private fun HttpException.getErrorMessage(): String? {
        val response = this.response()?.errorBody()?.string()
        val jsonObject = response?.let { JSONObject(it) }
        return jsonObject?.getString("message")
    }

    companion object {
        @Volatile
        private var instance: LoginRepository? = null
        fun getInstance(
            apiService: ApiService
        ): LoginRepository = instance ?: synchronized(this) {
            instance ?: LoginRepository(apiService)
        }.also { instance = it }
    }
}