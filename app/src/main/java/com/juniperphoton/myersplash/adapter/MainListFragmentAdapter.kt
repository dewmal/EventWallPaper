package com.juniperphoton.myersplash.adapter

import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentStatePagerAdapter

import com.juniperphoton.myersplash.fragment.MainListFragment
import com.juniperphoton.myersplash.model.UnsplashCategory

class MainListFragmentAdapter(private val callback: MainListFragment.Callback, fm: FragmentManager) : FragmentStatePagerAdapter(fm) {

    override fun getItem(position: Int): Fragment {
        val fragment = MainListFragment()
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
