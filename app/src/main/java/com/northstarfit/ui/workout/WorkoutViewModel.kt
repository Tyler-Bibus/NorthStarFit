package com.northstarfit.ui.workout

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.northstarfit.NorthStarFitApp
import com.northstarfit.data.WorkoutRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * One set as it's being edited. Weight and reps stay raw strings so the
 * user can type freely ("", ".", "12.5"); parsing happens when totals are
 * computed and when the workout is saved.
 */
data class SetDraft(
    val id: Long,
    val weight: String = "",
    val reps: String = "",
) {
    val parsedWeight: Double? get() = weight.toDoubleOrNull()?.takeIf { it > 0 }
    val parsedReps: Int? get() = reps.toIntOrNull()?.takeIf { it > 0 }
    val isComplete: Boolean get() = parsedWeight != null && parsedReps != null
}

/** One movement in the in-progress workout with its sets. */
data class MovementDraft(
    val id: Long,
    val exercise: String,
    val sets: List<SetDraft> = emptyList(),
)

/** Live totals shown while logging; only counts fully-entered sets. */
data class WorkoutTotals(val sets: Int = 0, val reps: Int = 0, val volume: Double = 0.0)

/**
 * Holds the in-progress workout. State lives here (not in the UI), so it
 * survives rotation; on finish the whole workout is written to Room.
 */
class WorkoutViewModel(private val repository: WorkoutRepository) : ViewModel() {

    private val startedAt = System.currentTimeMillis()
    private var nextId = 0L

    private val _movements = MutableStateFlow<List<MovementDraft>>(emptyList())
    val movements: StateFlow<List<MovementDraft>> = _movements.asStateFlow()

    private val _saving = MutableStateFlow(false)
    val saving: StateFlow<Boolean> = _saving.asStateFlow()

    val totals: WorkoutTotals
        get() {
            val complete = _movements.value.flatMap { it.sets }.filter { it.isComplete }
            return WorkoutTotals(
                sets = complete.size,
                reps = complete.sumOf { it.parsedReps!! },
                volume = complete.sumOf { it.parsedWeight!! * it.parsedReps!! },
            )
        }

    fun addMovement(exercise: String) {
        _movements.value += MovementDraft(id = nextId++, exercise = exercise)
    }

    fun removeMovement(movementId: Long) {
        _movements.value = _movements.value.filterNot { it.id == movementId }
    }

    fun addSet(movementId: Long) {
        _movements.value = _movements.value.map { movement ->
            if (movement.id == movementId) {
                movement.copy(sets = movement.sets + SetDraft(id = nextId++))
            } else movement
        }
    }

    fun removeSet(movementId: Long, setId: Long) {
        _movements.value = _movements.value.map { movement ->
            if (movement.id == movementId) {
                movement.copy(sets = movement.sets.filterNot { it.id == setId })
            } else movement
        }
    }

    fun updateSet(movementId: Long, setId: Long, weight: String? = null, reps: String? = null) {
        _movements.value = _movements.value.map { movement ->
            if (movement.id != movementId) return@map movement
            movement.copy(sets = movement.sets.map { set ->
                if (set.id != setId) set
                else set.copy(
                    weight = weight ?: set.weight,
                    reps = reps ?: set.reps,
                )
            })
        }
    }

    /**
     * Saves all movements that have at least one complete set, then invokes
     * [onSaved]. If nothing worth saving was logged, skips the write.
     */
    fun finishWorkout(onSaved: () -> Unit) {
        if (_saving.value) return
        val toSave = _movements.value
            .map { m -> m.exercise to m.sets.filter { it.isComplete } }
            .filter { (_, sets) -> sets.isNotEmpty() }
            .map { (exercise, sets) ->
                exercise to sets.map { it.parsedWeight!! to it.parsedReps!! }
            }

        if (toSave.isEmpty()) {
            onSaved()
            return
        }

        _saving.value = true
        viewModelScope.launch {
            repository.saveWorkout(
                startedAt = startedAt,
                endedAt = System.currentTimeMillis(),
                movements = toSave,
            )
            onSaved()
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = this[APPLICATION_KEY] as NorthStarFitApp
                WorkoutViewModel(app.repository)
            }
        }
    }
}
