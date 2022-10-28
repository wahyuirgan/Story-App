package com.hokagelab.storyapp.data

import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import androidx.room.withTransaction
import com.hokagelab.storyapp.data.source.local.entity.RemoteEntity
import com.hokagelab.storyapp.data.source.local.entity.StoryEntity
import com.hokagelab.storyapp.data.source.local.room.StoryDatabase
import com.hokagelab.storyapp.data.source.remote.network.ApiService

@OptIn(ExperimentalPagingApi::class)
class StoryRemoteMediator(
    private val storyDatabase: StoryDatabase,
    private val apiService: ApiService,
    private val token: String
) : RemoteMediator<Int, StoryEntity>() {

    override suspend fun initialize(): InitializeAction {
        return InitializeAction.LAUNCH_INITIAL_REFRESH
    }

    override suspend fun load(
        loadType: LoadType,
        state: PagingState<Int, StoryEntity>
    ): MediatorResult {
        val page = when (loadType) {
            LoadType.REFRESH -> {
                val remoteKeys = getRemoteKeyClosestToCurrentPosition(state)
                remoteKeys?.nextPage?.minus(1) ?: INITIAL_PAGE_INDEX
            }
            LoadType.PREPEND -> {
                val remoteKeys = getRemoteKeyForFirstItem(state)
                val prevKey = remoteKeys?.prevPage
                    ?: return MediatorResult.Success(endOfPaginationReached = remoteKeys != null)
                prevKey
            }
            LoadType.APPEND -> {
                val remoteKeys = getRemoteKeyForLastItem(state)
                val nextKey = remoteKeys?.nextPage
                    ?: return MediatorResult.Success(endOfPaginationReached = remoteKeys != null)
                nextKey
            }
        }

        return try {
            val responseData = apiService.stories(token, page, state.config.pageSize)
            val endOfPaginationReached = responseData.listStory.isEmpty()

            val stories = ArrayList<StoryEntity>()
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

            storyDatabase.withTransaction {
                if (loadType == LoadType.REFRESH) {
                    storyDatabase.remoteDao().deleteRemoteKeys()
                    storyDatabase.storyDao().deleteStories()
                }
                val prevPage = if (page == 1) null else page - 1
                val nextPage = if (endOfPaginationReached) null else page + 1
                val keys = stories.map {
                    RemoteEntity(id = it.id, prevPage = prevPage, nextPage = nextPage)
                }
                storyDatabase.remoteDao().insertKeys(keys)
                storyDatabase.storyDao().insertStories(stories)
            }
            MediatorResult.Success(endOfPaginationReached = endOfPaginationReached)
        } catch (exception: Exception) {
            MediatorResult.Error(exception)
        }
    }

    private suspend fun getRemoteKeyForLastItem(state: PagingState<Int, StoryEntity>): RemoteEntity? {
        return state.pages.lastOrNull { it.data.isNotEmpty() }?.data?.lastOrNull()?.let { data ->
            storyDatabase.remoteDao().getRemoteId(data.id)
        }
    }

    private suspend fun getRemoteKeyForFirstItem(state: PagingState<Int, StoryEntity>): RemoteEntity? {
        return state.pages.firstOrNull { it.data.isNotEmpty() }?.data?.firstOrNull()?.let { data ->
            storyDatabase.remoteDao().getRemoteId(
                data.id
            )
        }
    }

    private suspend fun getRemoteKeyClosestToCurrentPosition(state: PagingState<Int, StoryEntity>): RemoteEntity? {
        return state.anchorPosition?.let { position ->
            state.closestItemToPosition(position)?.id?.let { id ->
                storyDatabase.remoteDao().getRemoteId(id)
            }
        }
    }

    private companion object {
        const val INITIAL_PAGE_INDEX = 1
    }
}