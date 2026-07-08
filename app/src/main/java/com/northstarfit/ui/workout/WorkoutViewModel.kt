package com.northstarfit.ui.workout

import android.media.AudioManager
import android.media.ToneGenerator
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.northstarfit.NorthStarFitApp
import com.northstarfit.data.ExerciseEntity
import com.northstarfit.data.WorkoutRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/**
 * One set as it's being edited. Weight and reps stay raw strings so the
 * user can type freely ("", ".", "12.5"); parsing happens when totals are
 * computed and when the workout is saved. [done] marks the set as
 * physically performed, which is what starts the rest timer.
 */
@Serializable
data class SetDraft(
    val id: Long,
    val weight: String = "",
    val reps: String = "",
    val done: Boolean = false,
) {
    val parsedWeight: Double? get() = weight.toDoubleOrNull()?.takeIf { it > 0 }
    val parsedReps: Int? get() = reps.toIntOrNull()?.takeIf { it > 0 }
    val isComplete: Boolean get() = parsedWeight != null && parsedReps != null
}

/** One movement in the in-progress workout with its sets. */
@Serializable
data class MovementDraft(
    val id: Long,
    val exercise: String,
    val muscles: String = "",
    val sets: List<SetDraft> = emptyList(),
)

/** Live totals shown while logging; only counts fully-entered sets. */
data class WorkoutTotals(val sets: Int = 0, val reps: Int = 0, val volume: Double = 0.0)

/** Countdown state for the rest timer, null when idle. */
data class RestTimer(val totalSeconds: Int, val remainingSeconds: Int)

/**
 * Holds the in-progress workout. State lives here (not in the UI) so it
 * survives rotation, and every edit is also written to a draft row in Room
 * so it survives the app being killed. Finishing (or discarding) the
 * workout clears the draft; finishing writes the real workout tables.
 */
class WorkoutViewModel(private val repository: WorkoutRepository) : ViewModel() {

    private var startedAt = System.currentTimeMillis()
    private var nextId = 0L

    /** Exercise library for the picker, kept sorted by the DAO. */
    val exercises: StateFlow<List<ExerciseEntity>> =
        repository.observeExercises().stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList(),
        )

    private val _movements = MutableStateFlow<List<MovementDraft>>(emptyList())
    val movements: StateFlow<List<MovementDraft>> = _movements.asStateFlow()

    /** Derived from movements, so the bottom bar always reflects the sets. */
    val totals: StateFlow<WorkoutTotals> = _movements
        .map { movements ->
            val complete = movements.flatMap { it.sets }.filter { it.isComplete }
            WorkoutTotals(
                sets = complete.size,
                reps = complete.sumOf { it.parsedReps!! },
                volume = complete.sumOf { it.parsedWeight!! * it.parsedReps!! },
            )
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), WorkoutTotals())

    private val _saving = MutableStateFlow(false)
    val saving: StateFlow<Boolean> = _saving.asStateFlow()

    // ---- Rest timer ----

    private val _restTimer = MutableStateFlow<RestTimer?>(null)
    val restTimer: StateFlow<RestTimer?> = _restTimer.asStateFlow()

    private val _restDurationSeconds = MutableStateFlow(90)
    val restDurationSeconds: StateFlow<Int> = _restDurationSeconds.asStateFlow()

    private var restJob: Job? = null

    fun setRestDuration(seconds: Int) {
        _restDurationSeconds.value = seconds.coerceIn(15, 600)
    }

    fun startRestTimer() {
        val total = _restDurationSeconds.value
        restJob?.cancel()
        restJob = viewModelScope.launch {
            _restTimer.value = RestTimer(total, total)
            for (remaining in total - 1 downTo 0) {
                delay(1_000)
                _restTimer.value = RestTimer(total, remaining)
            }
            playRestDoneTone()
            _restTimer.value = null
        }
    }

    fun skipRestTimer() {
        restJob?.cancel()
        _restTimer.value = null
    }

    private fun playRestDoneTone() {
        // ToneGenerator needs no Context or permissions; ignore audio failures.
        runCatching {
            val tone = ToneGenerator(AudioManager.STREAM_MUSIC, 80)
            tone.startTone(ToneGenerator.TONE_PROP_BEEP2, 400)
        }
    }

    // ---- Draft persistence ----

    init {
        viewModelScope.launch {
            repository.seedDefaultExercises()
            restoreDraft()
        }
    }

    /** Picks up where a killed session left off, if a draft exists. */
    private suspend fun restoreDraft() {
        val draft = repository.loadDraft() ?: return
        val restored = runCatching {
            Json.decodeFromString<List<MovementDraft>>(draft.movementsJson)
        }.getOrNull() ?: return

        startedAt = draft.startedAt
        _movements.value = restored
        nextId = (restored.flatMap { m -> m.sets.map { it.id } + m.id }.maxOrNull() ?: -1L) + 1
    }

    private fun persistDraft() {
        viewModelScope.launch {
            repository.saveDraft(startedAt, Json.encodeToString(_movements.value))
        }
    }

    // ---- Editing the in-progress workout ----

    fun addExercise(name: String, muscles: String) {
        viewModelScope.launch { repository.addExercise(name, muscles) }
    }

    fun addMovement(exercise: ExerciseEntity) {
        _movements.value += MovementDraft(
            id = nextId++,
            exercise = exercise.name,
            muscles = exercise.muscles,
        )
        persistDraft()
    }

    fun removeMovement(movementId: Long) {
        _movements.value = _movements.value.filterNot { it.id == movementId }
        persistDraft()
    }

    /** Reorders movements after a drag; indices are positions in the list. */
    fun moveMovement(fromIndex: Int, toIndex: Int) {
        val current = _movements.value
        if (fromIndex == toIndex ||
            fromIndex !in current.indices || toIndex !in current.indices
        ) return
        _movements.value = current.toMutableList().apply {
            add(toIndex, removeAt(fromIndex))
        }
        persistDraft()
    }

    /** New sets start from the previous set's numbers — the common case. */
    fun addSet(movementId: Long) {
        _movements.value = _movements.value.map { movement ->
            if (movement.id == movementId) {
                val last = movement.sets.lastOrNull()
                movement.copy(
                    sets = movement.sets + SetDraft(
                        id = nextId++,
                        weight = last?.weight.orEmpty(),
                        reps = last?.reps.orEmpty(),
                    )
                )
            } else movement
        }
        persistDraft()
    }

    fun removeSet(movementId: Long, setId: Long) {
        _movements.value = _movements.value.map { movement ->
            if (movement.id == movementId) {
                movement.copy(sets = movement.sets.filterNot { it.id == setId })
            } else movement
        }
        persistDraft()
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
        persistDraft()
    }

    /** Checking a set off as performed starts the rest countdown. */
    fun toggleSetDone(movementId: Long, setId: Long) {
        var nowDone = false
        _movements.value = _movements.value.map { movement ->
            if (movement.id != movementId) return@map movement
            movement.copy(sets = movement.sets.map { set ->
                if (set.id != setId) set
                else set.copy(done = !set.done).also { nowDone = it.done }
            })
        }
        persistDraft()
        if (nowDone) startRestTimer()
    }

    // ---- Finishing ----

    /** True when there is at least one fully-entered set worth saving. */
    val hasSavableSets: Boolean
        get() = _movements.value.any { m -> m.sets.any { it.isComplete } }

    /** Throws the in-progress workout away (including the saved draft). */
    fun discardWorkout(onDiscarded: () -> Unit) {
        skipRestTimer()
        _movements.value = emptyList()
        viewModelScope.launch {
            repository.clearDraft()
            onDiscarded()
        }
    }

    /**
     * Saves all movements that have at least one complete set under the
     * given name, clears the draft, then invokes [onSaved]. If nothing
     * worth saving was logged, just clears the draft.
     */
    fun finishWorkout(name: String, onSaved: () -> Unit) {
        if (_saving.value) return
        skipRestTimer()
        val toSave = _movements.value
            .map { m -> m.exercise to m.sets.filter { it.isComplete } }
            .filter { (_, sets) -> sets.isNotEmpty() }
            .map { (exercise, sets) ->
                exercise to sets.map { it.parsedWeight!! to it.parsedReps!! }
            }

        _saving.value = true
        viewModelScope.launch {
            if (toSave.isNotEmpty()) {
                repository.saveWorkout(
                    name = name,
                    startedAt = startedAt,
                    endedAt = System.currentTimeMillis(),
                    movements = toSave,
                )
            }
            repository.clearDraft()
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
