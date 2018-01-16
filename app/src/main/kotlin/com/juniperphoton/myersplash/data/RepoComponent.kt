package com.juniperphoton.myersplash.data

import dagger.Component

@Component(modules = [(RepoModule::class)])
interface RepoComponent {
    fun inject(presenter: MainListPresenter)
}