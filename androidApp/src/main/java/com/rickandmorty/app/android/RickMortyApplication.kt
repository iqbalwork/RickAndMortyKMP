package com.rickandmorty.app.android

import android.app.Application
import com.rickandmorty.app.di.initializeKoin
import org.koin.android.ext.koin.androidContext

class RickMortyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        initializeKoin {
            androidContext(this@RickMortyApplication)
        }
    }
}

