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

/** Streams a single saved workout for the detail screen. */
class WorkoutDetailViewModel(
    repository: WorkoutRepository,
    workoutId: Long,
) : ViewModel() {

    /** Null while loading, and again if the workout gets deleted. */
    val workout: StateFlow<WorkoutWithMovements?> =
        repository.observeWorkout(workoutId).stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = null,
        )

    companion object {
        fun factory(workoutId: Long): ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = this[APPLICATION_KEY] as NorthStarFitApp
                WorkoutDetailViewModel(app.repository, workoutId)
            }
        }
    }
}
