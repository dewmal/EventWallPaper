package com.juniperphoton.myersplash.data

import dagger.Component

@Component(modules = arrayOf(RepoModule::class))
interface RepoComponent {
    fun inject(presenter: MainListPresenter)
}