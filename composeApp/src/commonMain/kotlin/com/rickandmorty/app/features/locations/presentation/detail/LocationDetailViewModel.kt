package com.rickandmorty.app.features.locations.presentation.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rickandmorty.app.core.util.DataError
import com.rickandmorty.app.core.util.Result
import com.rickandmorty.app.features.locations.domain.usecase.GetLocationDetailUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class LocationDetailViewModel(
    private val locationId: Int,
    private val getLocationDetailUseCase: GetLocationDetailUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(LocationDetailState())
    val state: StateFlow<LocationDetailState> = _state.asStateFlow()

    init {
        loadLocation()
    }

    fun onAction(action: LocationDetailAction) {
        when (action) {
            is LocationDetailAction.OnRetry -> loadLocation()
            is LocationDetailAction.OnBackClick -> {}
            is LocationDetailAction.OnResidentClick -> {}
        }
    }

    private fun loadLocation() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, errorMessage = null) }
            when (val result = getLocationDetailUseCase.getLocation(locationId)) {
                is Result.Success -> {
                    val location = result.data
                    _state.update {
                        it.copy(
                            location = location,
                            isLoading = false,
                            errorMessage = null
                        )
                    }
                    if (location.residentIds.isNotEmpty()) {
                        loadResidents(location.residentIds)
                    }
                }
                is Result.Error -> {
                    _state.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = formatErrorMessage(result.error)
                        )
                    }
                }
            }
        }
    }

    private fun loadResidents(residentIds: List<Int>) {
        viewModelScope.launch {
            _state.update { it.copy(isLoadingResidents = true) }
            when (val result = getLocationDetailUseCase.getResidents(residentIds)) {
                is Result.Success -> {
                    _state.update {
                        it.copy(
                            residents = result.data,
                            isLoadingResidents = false
                        )
                    }
                }
                is Result.Error -> {
                    _state.update { it.copy(isLoadingResidents = false) }
                }
            }
        }
    }

    private fun formatErrorMessage(error: DataError.Network): String {
        return when (error) {
            DataError.Network.NO_INTERNET -> "No dimensional connection available."
            DataError.Network.NOT_FOUND -> "Location not found in planetary matrix."
            DataError.Network.REQUEST_TIMEOUT -> "Coordinate scan timed out."
            DataError.Network.SERVER_ERROR -> "Citadel planetary database error."
            DataError.Network.SERIALIZATION -> "Cartographic data corrupted."
            DataError.Network.UNKNOWN -> "Unknown anomaly encountered."
        }
    }
}
