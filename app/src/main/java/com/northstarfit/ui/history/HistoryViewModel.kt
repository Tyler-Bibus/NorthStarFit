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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/** How the history list is ordered. */
enum class HistorySort(val label: String) {
    NEWEST("Newest first"),
    OLDEST("Oldest first"),
    VOLUME("Highest volume"),
    DURATION("Longest"),
    SETS("Most sets"),
}

/**
 * Streams saved workouts out of Room for the history list, with
 * client-side search (name or exercise) and sorting.
 */
class HistoryViewModel(private val repository: WorkoutRepository) : ViewModel() {

    private val _query = MutableStateFlow("")
    val query: StateFlow<String> = _query.asStateFlow()

    private val _sort = MutableStateFlow(HistorySort.NEWEST)
    val sort: StateFlow<HistorySort> = _sort.asStateFlow()

    val workouts: StateFlow<List<WorkoutWithMovements>> =
        combine(repository.observeWorkouts(), _query, _sort) { workouts, query, sort ->
            val trimmed = query.trim()
            val filtered = if (trimmed.isEmpty()) workouts else workouts.filter { w ->
                w.workout.name.contains(trimmed, ignoreCase = true) ||
                    w.movements.any {
                        it.movement.exercise.contains(trimmed, ignoreCase = true)
                    }
            }
            when (sort) {
                HistorySort.NEWEST -> filtered.sortedByDescending { it.workout.startedAt }
                HistorySort.OLDEST -> filtered.sortedBy { it.workout.startedAt }
                HistorySort.VOLUME -> filtered.sortedByDescending { it.totalVolume }
                HistorySort.DURATION -> filtered.sortedByDescending {
                    it.workout.endedAt - it.workout.startedAt
                }
                HistorySort.SETS -> filtered.sortedByDescending { it.totalSets }
            }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList(),
        )

    fun setQuery(query: String) {
        _query.value = query
    }

    fun setSort(sort: HistorySort) {
        _sort.value = sort
    }

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
