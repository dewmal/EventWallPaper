package com.juniperphoton.myersplash.model

import com.juniperphoton.myersplash.cloudservice.Request

@Suppress("unused")
class UnsplashCategory {
    companion object {
        val FEATURED_CATEGORY_ID = 10000
        val NEW_CATEGORY_ID = 10001
        val RANDOM_CATEGORY_ID = 10002
        val SEARCH_ID = 10003

        val FEATURE = "Featured"
        val NEW = "New"
        val RANDOM = "Random"

        val featuredCategory: UnsplashCategory
            get() {
                return UnsplashCategory().apply {
                    id = UnsplashCategory.FEATURED_CATEGORY_ID
                    title = UnsplashCategory.FEATURE
                }
            }

        val newCategory: UnsplashCategory
            get() {
                return UnsplashCategory().apply {
                    id = UnsplashCategory.NEW_CATEGORY_ID
                    title = UnsplashCategory.NEW
                }
            }

        val randomCategory: UnsplashCategory
            get() {
                return UnsplashCategory().apply {
                    id = UnsplashCategory.RANDOM_CATEGORY_ID
                    title = UnsplashCategory.RANDOM
                }
            }

        val searchCategory: UnsplashCategory
            get() {
                return UnsplashCategory().apply {
                    id = SEARCH_ID
                }
            }
    }

    private val photoCount: Int = 0
    private val links: Links? = null

    var id: Int = 0
    var title: String? = null

    val requestUrl: String?
        get() = when (id) {
            NEW_CATEGORY_ID -> Request.PHOTO_URL
            FEATURED_CATEGORY_ID -> Request.FEATURED_PHOTO_URL
            RANDOM_CATEGORY_ID -> Request.RANDOM_PHOTOS_URL
            SEARCH_ID -> Request.SEARCH_URL
            else -> links?.photos
        }

    val websiteUrl: String?
        get() = links?.html
}

@Suppress("unused")
class Links {
    val self: String? = null
    val photos: String? = null
    val html: String? = null
}