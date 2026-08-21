package com.rickandmorty.app.features.locations.di

import com.rickandmorty.app.features.locations.data.remote.LocationApi
import com.rickandmorty.app.features.locations.data.repository.LocationRepositoryImpl
import com.rickandmorty.app.features.locations.domain.repository.LocationRepository
import com.rickandmorty.app.features.locations.domain.usecase.GetLocationDetailUseCase
import com.rickandmorty.app.features.locations.domain.usecase.GetLocationsUseCase
import com.rickandmorty.app.features.locations.presentation.detail.LocationDetailViewModel
import com.rickandmorty.app.features.locations.presentation.list.LocationsViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val locationModule = module {
    single { LocationApi(httpClient = get()) }
    single<LocationRepository> { LocationRepositoryImpl(api = get(), characterRepository = get()) }

    single { GetLocationsUseCase(repository = get()) }
    single { GetLocationDetailUseCase(repository = get()) }

    viewModelOf(::LocationsViewModel)
    viewModel { (locationId: Int) ->
        LocationDetailViewModel(
            locationId = locationId,
            getLocationDetailUseCase = get()
        )
    }
}
