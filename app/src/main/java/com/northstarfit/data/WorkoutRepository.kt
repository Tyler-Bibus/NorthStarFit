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
        name: String,
        startedAt: Long,
        endedAt: Long,
        movements: List<Pair<String, List<Pair<Double, Int>>>>,
    ): Long = workoutDao.insertFullWorkout(
        WorkoutEntity(name = name.trim(), startedAt = startedAt, endedAt = endedAt),
        movements,
    )

    suspend fun deleteWorkout(workout: WorkoutEntity) = workoutDao.deleteWorkout(workout)

    // ---- Exercise library ----

    fun observeExercises(): Flow<List<ExerciseEntity>> = exerciseDao.observeExercises()

    /** Adds an exercise; duplicates are ignored. Blank names are rejected. */
    suspend fun addExercise(name: String, muscles: String = "") {
        val trimmed = name.trim()
        if (trimmed.isNotEmpty()) {
            exerciseDao.insert(ExerciseEntity(name = trimmed, muscles = muscles.trim()))
        }
    }

    suspend fun deleteExercise(exercise: ExerciseEntity) = exerciseDao.delete(exercise)

    /** Idempotent: relies on the unique name index, so it can run every launch. */
    suspend fun seedDefaultExercises() {
        exerciseDao.insertAll(
            DEFAULT_EXERCISES.map { (name, muscles) ->
                ExerciseEntity(name = name, muscles = muscles)
            }
        )
        // Rows seeded before the muscles column existed start out blank.
        DEFAULT_EXERCISES.forEach { (name, muscles) ->
            exerciseDao.backfillMuscles(name, muscles)
        }
    }

    // ---- In-progress draft ----

    suspend fun loadDraft(): DraftEntity? = workoutDao.getDraft()

    suspend fun saveDraft(startedAt: Long, movementsJson: String) =
        workoutDao.upsertDraft(DraftEntity(startedAt = startedAt, movementsJson = movementsJson))

    suspend fun clearDraft() = workoutDao.clearDraft()

    companion object {
        /** The original four lifts plus staples, tagged with muscles worked. */
        private val DEFAULT_EXERCISES = listOf(
            "Bench press" to "Chest, Triceps, Front Delts",
            "Deadlift" to "Hamstrings, Glutes, Lower Back",
            "Squat" to "Quads, Glutes, Core",
            "Bent Row" to "Lats, Upper Back, Biceps",
            "Overhead Press" to "Shoulders, Triceps",
            "Pull Up" to "Lats, Biceps, Core",
            "Lat Pulldown" to "Lats, Biceps",
            "Leg Press" to "Quads, Glutes",
            "Barbell Curl" to "Biceps, Forearms",
            "Tricep Pushdown" to "Triceps",
            "Leg Curl" to "Hamstrings",
            "Calf Raise" to "Calves",
        )
    }
}
