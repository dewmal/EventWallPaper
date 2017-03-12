package com.juniperphoton.myersplash.model

import com.google.gson.annotations.SerializedName

import java.io.Serializable

class UnsplashUser : Serializable {
    @SerializedName("id")
    private val mId: String? = null

    @SerializedName("username")
    private val mUserName: String? = null

    @SerializedName("name")
    val name: String? = null

    @SerializedName("links")
    private val mLinks: ProfileUrl? = null

    val homeUrl: String?
        get() = mLinks!!.mHtml
}

class ProfileUrl : Serializable {
    @SerializedName("self")
    val mSelf: String? = null

    @SerializedName("html")
    val mHtml: String? = null

    @SerializedName("photos")
    val mPhotos: String? = null

    @SerializedName("likes")
    val mLikes: String? = null

    @SerializedName("portfolio")
    val mPortfolio: String? = null
}