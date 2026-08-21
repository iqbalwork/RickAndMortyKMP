package com.rickandmorty.app.features.locations.presentation.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rickandmorty.app.core.util.DataError
import com.rickandmorty.app.core.util.Result
import com.rickandmorty.app.features.locations.domain.model.LocationFilter
import com.rickandmorty.app.features.locations.domain.usecase.GetLocationsUseCase
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
class LocationsViewModel(
    private val getLocationsUseCase: GetLocationsUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(LocationsState())
    val state: StateFlow<LocationsState> = _state.asStateFlow()

    private val searchQueryFlow = MutableStateFlow("")
    private var loadJob: Job? = null

    init {
        observeSearch()
        loadInitialLocations()
    }

    fun onAction(action: LocationsAction) {
        when (action) {
            is LocationsAction.OnSearchQueryChange -> {
                _state.update { it.copy(searchQuery = action.query) }
                searchQueryFlow.value = action.query
            }
            is LocationsAction.OnTypeFilterSelect -> {
                val newType = if (_state.value.typeFilter == action.type) null else action.type
                _state.update { it.copy(typeFilter = newType) }
                loadInitialLocations()
            }
            is LocationsAction.OnDimensionFilterSelect -> {
                val newDim = if (_state.value.dimensionFilter == action.dimension) null else action.dimension
                _state.update { it.copy(dimensionFilter = newDim) }
                loadInitialLocations()
            }
            is LocationsAction.OnClearFilters -> {
                _state.update {
                    it.copy(
                        typeFilter = null,
                        dimensionFilter = null
                    )
                }
                loadInitialLocations()
            }
            is LocationsAction.OnOpenFilterSheet -> {
                _state.update { it.copy(isFilterSheetVisible = true) }
            }
            is LocationsAction.OnDismissFilterSheet -> {
                _state.update { it.copy(isFilterSheetVisible = false) }
            }
            is LocationsAction.OnLoadNextPage -> {
                loadNextPage()
            }
            is LocationsAction.OnRefresh -> {
                refresh()
            }
            is LocationsAction.OnRetry -> {
                loadInitialLocations()
            }
            is LocationsAction.OnLocationClick -> {
                // Handled in navigation callback
            }
        }
    }

    private fun observeSearch() {
        searchQueryFlow
            .debounce(400L)
            .distinctUntilChanged()
            .onEach {
                loadInitialLocations()
            }
            .launchIn(viewModelScope)
    }

    private fun currentFilter(): LocationFilter? {
        val currentState = _state.value
        val name = currentState.searchQuery.takeIf { it.isNotBlank() }
        val type = currentState.typeFilter?.takeIf { it.isNotBlank() }
        val dimension = currentState.dimensionFilter?.takeIf { it.isNotBlank() }

        return if (name != null || type != null || dimension != null) {
            LocationFilter(
                name = name,
                type = type,
                dimension = dimension
            )
        } else {
            null
        }
    }

    private fun loadInitialLocations() {
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

            when (val result = getLocationsUseCase(page = 1, filter = currentFilter())) {
                is Result.Success -> {
                    _state.update {
                        it.copy(
                            locations = result.data,
                            isLoading = false,
                            errorMessage = null,
                            endReached = result.data.isEmpty()
                        )
                    }
                }
                is Result.Error -> {
                    _state.update {
                        it.copy(
                            locations = emptyList(),
                            isLoading = false,
                            errorMessage = formatErrorMessage(result.error)
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

            when (val result = getLocationsUseCase(page = nextPage, filter = currentFilter())) {
                is Result.Success -> {
                    val newLocations = result.data
                    _state.update {
                        it.copy(
                            locations = it.locations + newLocations,
                            currentPage = nextPage,
                            isLoadingMore = false,
                            endReached = newLocations.isEmpty()
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
            when (val result = getLocationsUseCase(page = 1, filter = currentFilter())) {
                is Result.Success -> {
                    _state.update {
                        it.copy(
                            locations = result.data,
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

    private fun formatErrorMessage(error: DataError.Network): String {
        return when (error) {
            DataError.Network.NO_INTERNET -> "No dimensional connection available."
            DataError.Network.NOT_FOUND -> "No celestial bodies or dimensions located."
            DataError.Network.REQUEST_TIMEOUT -> "Interdimensional scan timed out."
            DataError.Network.SERVER_ERROR -> "Citadel stellar registry error."
            DataError.Network.SERIALIZATION -> "Cartographic data corruption detected."
            DataError.Network.UNKNOWN -> "Unknown spatial anomaly occurred."
        }
    }
}
