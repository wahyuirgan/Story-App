package com.hokagelab.storyapp.utils

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.hokagelab.storyapp.data.StoryRepository
import com.hokagelab.storyapp.ui.main.MainViewModel
import com.hokagelab.storyapp.ui.uploadstory.AddStoryViewModel

class StoryViewModelFactory private constructor(private val storyRepository: StoryRepository) :
    ViewModelProvider.NewInstanceFactory() {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(MainViewModel::class.java) -> {
                MainViewModel(storyRepository) as T
            }
            modelClass.isAssignableFrom(AddStoryViewModel::class.java) -> {
                AddStoryViewModel(storyRepository) as T
            }
            else -> throw IllegalArgumentException("Unknown ViewModel Class:" + modelClass.name)
        }

    }

    companion object {
        @Volatile
        private var instance: StoryViewModelFactory? = null
        fun getInstance(context: Context): StoryViewModelFactory = instance ?: synchronized(this) {
            instance ?: StoryViewModelFactory(Utils.providerStoryRepository(context))
        }.also { instance = it }
    }
}