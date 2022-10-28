package com.hokagelab.storyapp.data.source.local.room

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.hokagelab.storyapp.data.source.local.entity.RemoteEntity

@Dao
interface RemoteDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertKeys(remoteKey: List<RemoteEntity>)

    @Query("SELECT * FROM tbl_remote_key WHERE id = :id")
    suspend fun getRemoteId(id: String): RemoteEntity?

    @Query("DELETE FROM tbl_remote_key")
    suspend fun deleteRemoteKeys()
}