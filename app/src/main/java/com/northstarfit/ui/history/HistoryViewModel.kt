package com.northstarfit.ui.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.northstarfit.NorthStarFitApp
import com.northstarfit.data.WorkoutRepository
import com.northstarfit.data.WorkoutWithMovements
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/** Streams saved workouts out of Room for the history list. */
class HistoryViewModel(private val repository: WorkoutRepository) : ViewModel() {

    val workouts: StateFlow<List<WorkoutWithMovements>> =
        repository.observeWorkouts().stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList(),
        )

    fun deleteWorkout(workout: WorkoutWithMovements) {
        viewModelScope.launch {
            repository.deleteWorkout(workout.workout)
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = this[APPLICATION_KEY] as NorthStarFitApp
                HistoryViewModel(app.repository)
            }
        }
    }
}
