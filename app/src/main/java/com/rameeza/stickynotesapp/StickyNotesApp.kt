package com.rameeza.stickynotesapp

import android.app.Application
import com.rameeza.stickynotesapp.di.AppComponent
import com.rameeza.stickynotesapp.di.DaggerAppComponent

class StickyNotesApp : Application() {
    val appComponent: AppComponent by lazy {
        DaggerAppComponent.factory().create(this)
    }
}
