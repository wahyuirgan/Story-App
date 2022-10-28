package com.hokagelab.storyapp.data.source.remote.response

import com.google.gson.annotations.SerializedName

data class Response(

    @field:SerializedName("error")
    val error: Boolean,

    @field:SerializedName("message")
    val message: String
)