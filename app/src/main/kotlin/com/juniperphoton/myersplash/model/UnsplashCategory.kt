package com.juniperphoton.myersplash.model

import com.juniperphoton.myersplash.cloudservice.CloudService

@Suppress("unused")
class UnsplashCategory {
    companion object {
        val FEATURED_CATEGORY_ID = 10000
        val NEW_CATEGORY_ID = 10001
        val RANDOM_CATEGORY_ID = 10002
        val SEARCH_ID = 10003

        val FEATURE_S = "Featured"
        val NEW_S = "New"
        val RANDOM_S = "Random"

        val featuredCategory: UnsplashCategory
            get() {
                return UnsplashCategory().apply {
                    id = UnsplashCategory.FEATURED_CATEGORY_ID
                    title = UnsplashCategory.FEATURE_S
                }
            }

        val newCategory: UnsplashCategory
            get() {
                return UnsplashCategory().apply {
                    id = UnsplashCategory.NEW_CATEGORY_ID
                    title = UnsplashCategory.NEW_S
                }
            }

        val randomCategory: UnsplashCategory
            get() {
                return UnsplashCategory().apply {
                    id = UnsplashCategory.RANDOM_CATEGORY_ID
                    title = UnsplashCategory.RANDOM_S
                }
            }

        val searchCategory: UnsplashCategory
            get() {
                return UnsplashCategory().apply {
                    id = SEARCH_ID
                }
            }
    }

    private val photo_count: Int = 0
    private val links: links? = null

    var id: Int = 0
    var title: String? = null

    val requestUrl: String?
        get() = when (id) {
            NEW_CATEGORY_ID -> CloudService.PHOTO_URL
            FEATURED_CATEGORY_ID -> CloudService.FEATURED_PHOTO_URL
            RANDOM_CATEGORY_ID -> CloudService.RANDOM_PHOTOS_URL
            SEARCH_ID -> CloudService.SEARCH_URL
            else -> links?.photos
        }

    val websiteUrl: String?
        get() = links?.html
}

@Suppress("unused")
class links {
    val self: String? = null
    val photos: String? = null
    val html: String? = null
}