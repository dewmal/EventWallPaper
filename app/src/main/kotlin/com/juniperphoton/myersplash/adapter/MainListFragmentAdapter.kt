package com.juniperphoton.myersplash.adapter

import android.graphics.RectF
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentStatePagerAdapter
import android.view.View

import com.juniperphoton.myersplash.fragment.MainListFragment
import com.juniperphoton.myersplash.model.UnsplashCategory
import com.juniperphoton.myersplash.model.UnsplashImage
import com.juniperphoton.myersplash.presenter.MainListPresenter

class MainListFragmentAdapter(private var callback: ((RectF, UnsplashImage, View) -> Unit)?,
                              fm: FragmentManager)
    : FragmentStatePagerAdapter(fm) {
    override fun getItem(position: Int): Fragment {
        val fragment = MainListFragment()
        val presenter = MainListPresenter(fragment)
        fragment.setPresenter(presenter)
        when (position) {
            0 -> fragment.setCategory(UnsplashCategory.featuredCategory, callback)
            1 -> fragment.setCategory(UnsplashCategory.newCategory, callback)
            2 -> fragment.setCategory(UnsplashCategory.randomCategory, callback)
        }
        return fragment
    }

    override fun getCount(): Int {
        return 3
    }
}
