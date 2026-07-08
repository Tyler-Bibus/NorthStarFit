package com.northstarfit.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
interface WorkoutDao {

    @Insert
    suspend fun insertWorkout(workout: WorkoutEntity): Long

    @Insert
    suspend fun insertMovement(movement: MovementEntity): Long

    @Insert
    suspend fun insertSets(sets: List<SetEntity>)

    @Delete
    suspend fun deleteWorkout(workout: WorkoutEntity)

    @Transaction
    @Query("SELECT * FROM workouts ORDER BY startedAt DESC")
    fun observeWorkouts(): Flow<List<WorkoutWithMovements>>

    /**
     * Persists a finished workout with all of its movements and sets in a
     * single transaction, wiring up the generated foreign keys.
     */
    @Transaction
    suspend fun insertFullWorkout(
        workout: WorkoutEntity,
        movements: List<Pair<String, List<Pair<Double, Int>>>>,
    ): Long {
        val workoutId = insertWorkout(workout)
        for ((exercise, sets) in movements) {
            val movementId = insertMovement(
                MovementEntity(workoutId = workoutId, exercise = exercise)
            )
            insertSets(
                sets.map { (weight, reps) ->
                    SetEntity(movementId = movementId, weight = weight, reps = reps)
                }
            )
        }
        return workoutId
    }
}
