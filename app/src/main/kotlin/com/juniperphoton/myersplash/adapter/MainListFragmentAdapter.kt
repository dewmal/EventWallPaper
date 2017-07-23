package com.juniperphoton.myersplash.adapter

import android.graphics.RectF
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentStatePagerAdapter
import android.view.View
import com.juniperphoton.myersplash.App
import com.juniperphoton.myersplash.data.DaggerRepoComponent
import com.juniperphoton.myersplash.data.MainListPresenter
import com.juniperphoton.myersplash.data.RepoModule
import com.juniperphoton.myersplash.fragment.MainListFragment
import com.juniperphoton.myersplash.model.UnsplashImage

class MainListFragmentAdapter(private var callback: ((RectF, UnsplashImage, View) -> Unit)?,
                              fm: FragmentManager
) : FragmentStatePagerAdapter(fm) {
    override fun getItem(position: Int): Fragment {
        val fragment = MainListFragment()

        val presenter = MainListPresenter()
        val component = DaggerRepoComponent.builder().repoModule(RepoModule(App.instance, position, fragment)).build()
        component.inject(presenter)

        fragment.setPresenter(presenter)
        fragment.onClickPhotoItem = callback

        return fragment
    }

    override fun getCount(): Int {
        return 3
    }
}
