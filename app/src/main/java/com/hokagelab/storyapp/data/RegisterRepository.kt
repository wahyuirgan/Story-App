package com.hokagelab.storyapp.data


import androidx.lifecycle.LiveData
import androidx.lifecycle.liveData
import com.hokagelab.storyapp.data.Resource.*
import com.hokagelab.storyapp.data.source.remote.network.ApiService
import com.hokagelab.storyapp.data.source.remote.response.Response
import org.json.JSONObject
import retrofit2.HttpException

class RegisterRepository (
    private val apiService: ApiService
)
{
    fun registerAccount(
        name: String,
        email: String,
        password: String
    ): LiveData<Resource<Response>> =
        liveData {
            emit(Loading)
            try {
                val response = apiService.register(name, email, password)
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
        private var instance: RegisterRepository? = null
        fun getInstance(
            apiService: ApiService
        ): RegisterRepository = instance ?: synchronized(this) {
            instance ?: RegisterRepository(apiService)
        }.also { instance = it }
    }
}