package com.hokagelab.storyapp.data.source.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tbl_remote_key")
data class RemoteEntity (
    @PrimaryKey
    val id: String,
    val prevPage: Int?,
    val nextPage: Int?
)