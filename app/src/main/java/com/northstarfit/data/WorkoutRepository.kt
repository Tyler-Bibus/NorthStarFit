package com.northstarfit.data

import kotlinx.coroutines.flow.Flow

/**
 * Single entry point to workout storage. Screens talk to this instead of
 * touching the DAO directly, which keeps room to swap or extend the data
 * source later (e.g. cloud sync) without touching UI code.
 */
class WorkoutRepository(private val dao: WorkoutDao) {

    /** All saved workouts, newest first, as a reactive stream. */
    fun observeWorkouts(): Flow<List<WorkoutWithMovements>> = dao.observeWorkouts()

    /**
     * Saves a finished workout.
     *
     * @param movements each movement as (exercise name, list of (weight, reps))
     */
    suspend fun saveWorkout(
        startedAt: Long,
        endedAt: Long,
        movements: List<Pair<String, List<Pair<Double, Int>>>>,
    ): Long = dao.insertFullWorkout(
        WorkoutEntity(startedAt = startedAt, endedAt = endedAt),
        movements,
    )

    suspend fun deleteWorkout(workout: WorkoutEntity) = dao.deleteWorkout(workout)
}
