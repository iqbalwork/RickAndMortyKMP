package com.rickandmorty.app.features.episodes.di

import com.rickandmorty.app.features.episodes.data.remote.EpisodeApi
import com.rickandmorty.app.features.episodes.data.repository.EpisodeRepositoryImpl
import com.rickandmorty.app.features.episodes.domain.repository.EpisodeRepository
import com.rickandmorty.app.features.episodes.domain.usecase.GetEpisodeDetailUseCase
import com.rickandmorty.app.features.episodes.domain.usecase.GetEpisodesUseCase
import com.rickandmorty.app.features.episodes.domain.usecase.GetFavoriteEpisodesUseCase
import com.rickandmorty.app.features.episodes.domain.usecase.IsEpisodeFavoriteUseCase
import com.rickandmorty.app.features.episodes.domain.usecase.ToggleEpisodeFavoriteUseCase
import com.rickandmorty.app.features.episodes.presentation.detail.EpisodeDetailViewModel
import com.rickandmorty.app.features.episodes.presentation.list.EpisodesViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val episodeModule = module {
    single { EpisodeApi(httpClient = get()) }
    single<EpisodeRepository> {
        EpisodeRepositoryImpl(
            api = get(),
            episodeDao = get(),
            characterRepository = get()
        )
    }

    single { GetEpisodesUseCase(repository = get()) }
    single { GetEpisodeDetailUseCase(repository = get()) }
    single { ToggleEpisodeFavoriteUseCase(repository = get()) }
    single { GetFavoriteEpisodesUseCase(repository = get()) }
    single { IsEpisodeFavoriteUseCase(repository = get()) }

    viewModelOf(::EpisodesViewModel)
    viewModel { (episodeId: Int) ->
        EpisodeDetailViewModel(
            episodeId = episodeId,
            getEpisodeDetailUseCase = get(),
            toggleFavoriteUseCase = get(),
            isFavoriteUseCase = get()
        )
    }
}
