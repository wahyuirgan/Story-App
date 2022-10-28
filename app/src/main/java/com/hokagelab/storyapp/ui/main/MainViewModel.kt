package com.hokagelab.storyapp.ui.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.cachedIn
import com.hokagelab.storyapp.data.StoryRepository

class MainViewModel(private val storyRepository: StoryRepository) : ViewModel() {
    fun getStories(token: String) =
        storyRepository.getStories(token).cachedIn(viewModelScope)
}