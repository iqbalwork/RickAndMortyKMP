package com.rickandmorty.app.features.characters.presentation.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rickandmorty.app.core.util.DataError
import com.rickandmorty.app.core.util.Result
import com.rickandmorty.app.features.characters.domain.model.Character
import com.rickandmorty.app.features.characters.domain.model.CharacterFilter
import com.rickandmorty.app.features.characters.domain.usecase.GetCharactersUseCase
import com.rickandmorty.app.features.characters.domain.usecase.GetFavoriteCharactersUseCase
import com.rickandmorty.app.features.characters.domain.usecase.ToggleCharacterFavoriteUseCase
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@OptIn(FlowPreview::class)
class CharactersViewModel(
    private val getCharactersUseCase: GetCharactersUseCase,
    private val toggleFavoriteUseCase: ToggleCharacterFavoriteUseCase,
    private val getFavoritesUseCase: GetFavoriteCharactersUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(CharactersState())
    val state: StateFlow<CharactersState> = _state.asStateFlow()

    private val searchQueryFlow = MutableStateFlow("")
    private var loadJob: Job? = null

    init {
        observeFavorites()
        observeSearch()
        loadInitialCharacters()
    }

    fun onAction(action: CharactersAction) {
        when (action) {
            is CharactersAction.OnSearchQueryChange -> {
                _state.update { it.copy(searchQuery = action.query) }
                searchQueryFlow.value = action.query
            }
            is CharactersAction.OnStatusFilterSelect -> {
                val newStatus = if (_state.value.selectedStatus == action.status) null else action.status
                _state.update { it.copy(selectedStatus = newStatus) }
                loadInitialCharacters()
            }
            is CharactersAction.OnGenderFilterSelect -> {
                val newGender = if (_state.value.selectedGender == action.gender) null else action.gender
                _state.update { it.copy(selectedGender = newGender) }
                loadInitialCharacters()
            }
            is CharactersAction.OnSpeciesFilterChange -> {
                _state.update { it.copy(speciesFilter = action.species) }
                loadInitialCharacters()
            }
            is CharactersAction.OnTypeFilterChange -> {
                _state.update { it.copy(typeFilter = action.type) }
                loadInitialCharacters()
            }
            is CharactersAction.OnClearFilters -> {
                _state.update {
                    it.copy(
                        selectedStatus = null,
                        selectedGender = null,
                        speciesFilter = null,
                        typeFilter = null
                    )
                }
                loadInitialCharacters()
            }
            is CharactersAction.OnOpenFilterSheet -> {
                _state.update { it.copy(isFilterSheetVisible = true) }
            }
            is CharactersAction.OnDismissFilterSheet -> {
                _state.update { it.copy(isFilterSheetVisible = false) }
            }
            is CharactersAction.OnLoadNextPage -> {
                loadNextPage()
            }
            is CharactersAction.OnRefresh -> {
                refresh()
            }
            is CharactersAction.OnToggleFavorite -> {
                toggleFavorite(action.character)
            }
            is CharactersAction.OnRetry -> {
                loadInitialCharacters()
            }
            is CharactersAction.OnCharacterClick -> {
                // Handled by UI callback
            }
        }
    }

    private fun observeFavorites() {
        getFavoritesUseCase()
            .onEach { favorites ->
                val favIds = favorites.map { it.id }.toSet()
                _state.update { it.copy(favoriteIds = favIds) }
            }
            .launchIn(viewModelScope)
    }

    private fun observeSearch() {
        searchQueryFlow
            .debounce(400L)
            .distinctUntilChanged()
            .onEach {
                loadInitialCharacters()
            }
            .launchIn(viewModelScope)
    }

    private fun currentFilter(): CharacterFilter? {
        val currentState = _state.value
        val name = currentState.searchQuery.takeIf { it.isNotBlank() }
        val status = currentState.selectedStatus
        val gender = currentState.selectedGender
        val species = currentState.speciesFilter?.takeIf { it.isNotBlank() }
        val type = currentState.typeFilter?.takeIf { it.isNotBlank() }

        return if (name != null || status != null || gender != null || species != null || type != null) {
            CharacterFilter(
                name = name,
                status = status,
                gender = gender,
                species = species,
                type = type
            )
        } else {
            null
        }
    }

    private fun loadInitialCharacters() {
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            _state.update {
                it.copy(
                    isLoading = true,
                    errorMessage = null,
                    currentPage = 1,
                    endReached = false
                )
            }

            when (val result = getCharactersUseCase(page = 1, filter = currentFilter())) {
                is Result.Success -> {
                    _state.update {
                        it.copy(
                            characters = result.data,
                            isLoading = false,
                            errorMessage = null,
                            endReached = result.data.isEmpty()
                        )
                    }
                }
                is Result.Error -> {
                    val message = formatErrorMessage(result.error)
                    _state.update {
                        it.copy(
                            characters = emptyList(),
                            isLoading = false,
                            errorMessage = message
                        )
                    }
                }
            }
        }
    }

    private fun loadNextPage() {
        val currentState = _state.value
        if (currentState.isLoading || currentState.isLoadingMore || currentState.endReached) {
            return
        }

        viewModelScope.launch {
            val nextPage = currentState.currentPage + 1
            _state.update { it.copy(isLoadingMore = true) }

            when (val result = getCharactersUseCase(page = nextPage, filter = currentFilter())) {
                is Result.Success -> {
                    val newCharacters = result.data
                    _state.update {
                        it.copy(
                            characters = it.characters + newCharacters,
                            currentPage = nextPage,
                            isLoadingMore = false,
                            endReached = newCharacters.isEmpty()
                        )
                    }
                }
                is Result.Error -> {
                    _state.update {
                        it.copy(
                            isLoadingMore = false,
                            endReached = true
                        )
                    }
                }
            }
        }
    }

    private fun refresh() {
        viewModelScope.launch {
            _state.update { it.copy(isRefreshing = true, errorMessage = null) }
            when (val result = getCharactersUseCase(page = 1, filter = currentFilter())) {
                is Result.Success -> {
                    _state.update {
                        it.copy(
                            characters = result.data,
                            isRefreshing = false,
                            currentPage = 1,
                            endReached = result.data.isEmpty(),
                            errorMessage = null
                        )
                    }
                }
                is Result.Error -> {
                    _state.update {
                        it.copy(
                            isRefreshing = false,
                            errorMessage = formatErrorMessage(result.error)
                        )
                    }
                }
            }
        }
    }

    private fun toggleFavorite(character: Character) {
        viewModelScope.launch {
            toggleFavoriteUseCase(character)
        }
    }

    private fun formatErrorMessage(error: DataError.Network): String {
        return when (error) {
            DataError.Network.NO_INTERNET -> "No internet connection detected across dimensions."
            DataError.Network.NOT_FOUND -> "No characters found matching the query."
            DataError.Network.REQUEST_TIMEOUT -> "Interdimensional request timed out."
            DataError.Network.SERVER_ERROR -> "Citadel server anomaly encountered."
            DataError.Network.SERIALIZATION -> "Dimensional data corruption detected."
            DataError.Network.UNKNOWN -> "An unknown anomaly occurred in the multiverse."
        }
    }
}
