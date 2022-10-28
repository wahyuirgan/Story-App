package com.hokagelab.storyapp.utils

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import android.widget.RemoteViewsService
import androidx.core.os.bundleOf
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import com.bumptech.glide.Glide
import com.hokagelab.storyapp.R
import com.hokagelab.storyapp.data.source.local.UserPreference
import com.hokagelab.storyapp.data.source.local.entity.StoryEntity
import com.hokagelab.storyapp.data.source.remote.network.ApiConfig
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

internal class StackRemoteViewsFactory(private val mContext: Context) : RemoteViewsService.RemoteViewsFactory {

    private val stories = ArrayList<StoryEntity>()
    private val Context.dataStore: DataStore<Preferences> by preferencesDataStore("preference")

    override fun onCreate() {

    }

    override fun onDataSetChanged(): Unit = runBlocking {
        val userPreference = UserPreference.getInstance(mContext.dataStore)
        val tokenAuth = mContext.getString(R.string.header_api, userPreference.getToken().first())

        try {
            val responseData = ApiConfig.getApiService().stories(tokenAuth, 1, 10)
            for (data in responseData.listStory) {
                val story = StoryEntity(
                    id = data.id,
                    name = data.name,
                    description = data.description,
                    photoUrl = data.photoUrl,
                    createdAt = data.createdAt,
                    lat = data.lat,
                    lon = data.lon
                )
                stories.add(story)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onDestroy() {

    }

    override fun getCount(): Int = stories.size

    @SuppressLint("RemoteViewLayout")
    override fun getViewAt(position: Int): RemoteViews {
        val remoteViews = RemoteViews(mContext.packageName, R.layout.story_widget_item)

        val imageStory = Glide.with(mContext)
            .asBitmap()
            .load(stories[position].photoUrl)
            .submit()
            .get()

        remoteViews.setImageViewBitmap(R.id.ivStoryPhotoWidget, imageStory)

        val extras = bundleOf(
            "id" to stories[position].id,
            "name" to stories[position].name,
            "description" to stories[position].description,
            "photoUrl" to stories[position].photoUrl,
            "createdAt" to stories[position].createdAt
        )

        val fillInIntent = Intent().apply {
            putExtras(extras)
        }

        remoteViews.setOnClickFillInIntent(R.id.ivStoryPhotoWidget, fillInIntent)
        return remoteViews
    }

    override fun getLoadingView(): RemoteViews? = null

    override fun getViewTypeCount(): Int = 1

    override fun getItemId(i: Int): Long = 0

    override fun hasStableIds(): Boolean = false

}