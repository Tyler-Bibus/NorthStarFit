package com.northstarfit.ui.progress

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.northstarfit.NorthStarFitApp
import com.northstarfit.data.WorkoutRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

/** Which number is charted per workout. */
enum class ProgressMetric(val label: String) {
    MAX_WEIGHT("Max weight"),
    VOLUME("Volume"),
}

/** One charted observation: a workout's date and the metric's value. */
data class ProgressPoint(val timestamp: Long, val value: Double)

/**
 * Derives per-exercise progress series from saved workouts: for every
 * workout containing the chosen exercise, either the heaviest set or the
 * exercise's total volume that day.
 */
class ProgressViewModel(repository: WorkoutRepository) : ViewModel() {

    private val workouts = repository.observeWorkouts()

    private val _selectedExercise = MutableStateFlow<String?>(null)
    val selectedExercise: StateFlow<String?> = _selectedExercise.asStateFlow()

    private val _metric = MutableStateFlow(ProgressMetric.MAX_WEIGHT)
    val metric: StateFlow<ProgressMetric> = _metric.asStateFlow()

    /** Every exercise that appears in at least one saved workout. */
    val exerciseNames: StateFlow<List<String>> = workouts
        .combine(_selectedExercise) { all, _ ->
            all.flatMap { w -> w.movements.map { it.movement.exercise } }
                .distinct()
                .sorted()
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val points: StateFlow<List<ProgressPoint>> =
        combine(workouts, _selectedExercise, _metric) { all, selected, metric ->
            val exercise = selected
                ?: all.flatMap { w -> w.movements.map { it.movement.exercise } }
                    .distinct().sorted().firstOrNull()
                ?: return@combine emptyList()

            all.mapNotNull { workout ->
                val sets = workout.movements
                    .filter { it.movement.exercise == exercise }
                    .flatMap { it.sets }
                if (sets.isEmpty()) return@mapNotNull null
                val value = when (metric) {
                    ProgressMetric.MAX_WEIGHT -> sets.maxOf { it.weight }
                    ProgressMetric.VOLUME -> sets.sumOf { it.weight * it.reps }
                }
                ProgressPoint(workout.workout.startedAt, value)
            }.sortedBy { it.timestamp }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun selectExercise(name: String) {
        _selectedExercise.value = name
    }

    fun setMetric(metric: ProgressMetric) {
        _metric.value = metric
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = this[APPLICATION_KEY] as NorthStarFitApp
                ProgressViewModel(app.repository)
            }
        }
    }
}
