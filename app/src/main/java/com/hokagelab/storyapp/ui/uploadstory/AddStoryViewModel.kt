package com.hokagelab.storyapp.ui.uploadstory

import androidx.lifecycle.ViewModel
import com.hokagelab.storyapp.data.StoryRepository
import okhttp3.MultipartBody
import okhttp3.RequestBody

class AddStoryViewModel(private val storyRepository: StoryRepository) : ViewModel() {
    fun uploadStory(
        token: String,
        image: MultipartBody.Part,
        description: RequestBody,
        lat: RequestBody?,
        lon: RequestBody?
    ) = storyRepository.uploadStory("Bearer $token", image, description, lat, lon)
}