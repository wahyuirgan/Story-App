package com.hokagelab.storyapp.data

import androidx.lifecycle.LiveData
import androidx.lifecycle.liveData
import androidx.paging.*
import com.hokagelab.storyapp.data.source.local.entity.StoryEntity
import com.hokagelab.storyapp.data.source.local.room.StoryDatabase
import com.hokagelab.storyapp.data.source.remote.network.ApiService
import com.hokagelab.storyapp.data.source.remote.response.Response
import okhttp3.MultipartBody
import okhttp3.RequestBody
import org.json.JSONObject
import retrofit2.HttpException

class StoryRepository(
    private val apiService: ApiService,
    private val storyDatabase: StoryDatabase
){
    @OptIn(ExperimentalPagingApi::class)
    fun getStories(token: String): LiveData<PagingData<StoryEntity>> {
        return Pager(
            config = PagingConfig(
                pageSize = 10
            ),
            remoteMediator = StoryRemoteMediator(storyDatabase, apiService, token),
            pagingSourceFactory = {
                storyDatabase.storyDao().getStories()
            }
        ).liveData
    }

    fun uploadStory(
        token: String,
        photo: MultipartBody.Part,
        description: RequestBody,
        lat: RequestBody?,
        lon: RequestBody?
    ): LiveData<Resource<Response>> =
        liveData {
            emit(Resource.Loading)
            try {
                val response = apiService.uploadStories(token, photo, description, lat, lon)
                emit(Resource.Success(response))
            } catch (e: Exception) {
                when (e) {
                    is HttpException -> {
                        val message = e.getErrorMessage()
                        if (message != null) {
                            emit(Resource.Error(message))
                        }
                    }
                    else -> {
                        emit(Resource.Error(e.message.toString()))
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
        private var instance: StoryRepository? = null
        fun getInstance(
            apiService: ApiService,
            storyDatabase: StoryDatabase
        ): StoryRepository = instance ?: synchronized(this) {
            instance ?: StoryRepository(apiService, storyDatabase)
        }.also { instance = it }
    }
}