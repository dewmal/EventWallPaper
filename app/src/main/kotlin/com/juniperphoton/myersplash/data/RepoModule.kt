package com.juniperphoton.myersplash.data

import com.juniperphoton.myersplash.model.UnsplashCategory
import dagger.Module
import dagger.Provides

@Module
class RepoModule(private val pos: Int,
                 private val view: Contract.MainView) {
    @Provides
    fun providesCategory(): UnsplashCategory {
        return when (pos) {
            0 -> UnsplashCategory.featuredCategory
            1 -> UnsplashCategory.newCategory
            2 -> UnsplashCategory.randomCategory
            else -> UnsplashCategory.searchCategory
        }
    }

    @Provides
    fun providesView(): Contract.MainView {
        return view
    }
}