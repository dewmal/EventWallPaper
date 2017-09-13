package com.juniperphoton.myersplash.model

import com.google.gson.annotations.SerializedName

class SearchResult {
    @SerializedName("results")
    val list: MutableList<UnsplashImage>? = null
}
