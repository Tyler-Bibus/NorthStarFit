package com.northstarfit.data

import kotlinx.coroutines.flow.Flow

/**
 * Single entry point to workout storage. Screens talk to this instead of
 * touching the DAOs directly, which keeps room to swap or extend the data
 * source later (e.g. cloud sync) without touching UI code.
 */
class WorkoutRepository(
    private val workoutDao: WorkoutDao,
    private val exerciseDao: ExerciseDao,
) {

    // ---- Finished workouts ----

    /** All saved workouts, newest first, as a reactive stream. */
    fun observeWorkouts(): Flow<List<WorkoutWithMovements>> = workoutDao.observeWorkouts()

    /** One saved workout with all movements and sets, or null if deleted. */
    fun observeWorkout(workoutId: Long): Flow<WorkoutWithMovements?> =
        workoutDao.observeWorkout(workoutId)

    /**
     * Saves a finished workout.
     *
     * @param movements each movement as (exercise name, list of (weight, reps))
     */
    suspend fun saveWorkout(
        startedAt: Long,
        endedAt: Long,
        movements: List<Pair<String, List<Pair<Double, Int>>>>,
    ): Long = workoutDao.insertFullWorkout(
        WorkoutEntity(startedAt = startedAt, endedAt = endedAt),
        movements,
    )

    suspend fun deleteWorkout(workout: WorkoutEntity) = workoutDao.deleteWorkout(workout)

    // ---- Exercise library ----

    fun observeExercises(): Flow<List<ExerciseEntity>> = exerciseDao.observeExercises()

    /** Adds an exercise; duplicates are ignored. Blank names are rejected. */
    suspend fun addExercise(name: String) {
        val trimmed = name.trim()
        if (trimmed.isNotEmpty()) {
            exerciseDao.insert(ExerciseEntity(name = trimmed))
        }
    }

    suspend fun deleteExercise(exercise: ExerciseEntity) = exerciseDao.delete(exercise)

    /** Idempotent: relies on the unique name index, so it can run every launch. */
    suspend fun seedDefaultExercises() {
        exerciseDao.insertAll(DEFAULT_EXERCISES.map { ExerciseEntity(name = it) })
    }

    // ---- In-progress draft ----

    suspend fun loadDraft(): DraftEntity? = workoutDao.getDraft()

    suspend fun saveDraft(startedAt: Long, movementsJson: String) =
        workoutDao.upsertDraft(DraftEntity(startedAt = startedAt, movementsJson = movementsJson))

    suspend fun clearDraft() = workoutDao.clearDraft()

    companion object {
        /** The original four lifts plus a few staples. */
        private val DEFAULT_EXERCISES = listOf(
            "Bench press",
            "Deadlift",
            "Squat",
            "Bent Row",
            "Overhead Press",
            "Pull Up",
            "Lat Pulldown",
            "Leg Press",
        )
    }
}
