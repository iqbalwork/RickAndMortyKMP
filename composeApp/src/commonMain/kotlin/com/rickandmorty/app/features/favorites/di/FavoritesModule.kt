package com.rickandmorty.app.features.favorites.di

import com.rickandmorty.app.features.favorites.presentation.FavoritesViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val favoritesModule = module {
    viewModelOf(::FavoritesViewModel)
}
