package com.juniperphoton.myersplash.model

import com.google.gson.annotations.SerializedName

import java.io.Serializable

class UnsplashUser : Serializable {
    @SerializedName("id")
    var id: String? = null

    @SerializedName("username")
    var userName: String? = null

    @SerializedName("name")
    var name: String? = null

    @SerializedName("links")
    var links: ProfileUrl? = null

    val homeUrl: String?
        get() = links?.html
}

class ProfileUrl : Serializable {
    @SerializedName("self")
    val self: String? = null

    @SerializedName("html")
    val html: String? = null

    @SerializedName("photos")
    val photos: String? = null

    @SerializedName("likes")
    val likes: String? = null

    @SerializedName("portfolio")
    val portfolio: String? = null
}