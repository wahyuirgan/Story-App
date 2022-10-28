package com.hokagelab.storyapp.data.source.remote.response

import com.google.gson.annotations.SerializedName

object LoginResponse{

    data class Response (
        @field:SerializedName("error")
        val error: Boolean,

        @field:SerializedName("message")
        val message: String,

        @field:SerializedName("loginResult")
        val loginResult: ResultItem
    )

    data class ResultItem(
        @field:SerializedName("userId")
        val userId: String,

        @field:SerializedName("title")
        val title: String,

        @field:SerializedName("name")
        val name: String,

        @SerializedName("token")
        val token: String
    )
}