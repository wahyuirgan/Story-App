package com.hokagelab.storyapp.data.source.local.room

import androidx.paging.PagingSource
import androidx.room.*
import com.hokagelab.storyapp.data.source.local.entity.StoryEntity

@Dao
interface StoryDao {
    @Query("SELECT * FROM tbl_story")
    fun getStories(): PagingSource<Int, StoryEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStories(story: List<StoryEntity>)

    @Query("DELETE FROM tbl_story")
    suspend fun deleteStories()
}