package com.juniperphoton.myersplash.model

import com.juniperphoton.myersplash.cloudservice.CloudService

class UnsplashCategory {
    companion object {
        val FEATURED_CATEGORY_ID = 10000
        val NEW_CATEGORY_ID = 10001
        val RANDOM_CATEGORY_ID = 10002
        val SEARCH_ID = 10003

        val FEATURE_S = "Featured"
        val NEW_S = "New"
        val RANDOM_S = "Random"

        fun getSearchCategory(title: String): UnsplashCategory {
            val category = UnsplashCategory()
            category.id = UnsplashCategory.SEARCH_ID
            category.title = title

            return category
        }

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
        get() {
            when (id) {
                NEW_CATEGORY_ID -> return CloudService.PHOTO_URL
                FEATURED_CATEGORY_ID -> return CloudService.FEATURED_PHOTO_URL
                RANDOM_CATEGORY_ID -> return CloudService.RANDOM_PHOTOS_URL
                SEARCH_ID -> return CloudService.SEARCH_URL
                else -> return links?.photos
            }
        }

    val websiteUrl: String?
        get() = links?.html
}

class links {
    val self: String? = null
    val photos: String? = null
    val html: String? = null
}