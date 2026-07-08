package com.northstarfit.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ExerciseDao {

    @Query("SELECT * FROM exercises ORDER BY name COLLATE NOCASE")
    fun observeExercises(): Flow<List<ExerciseEntity>>

    /** Ignores duplicates (unique index on name), so seeding is idempotent. */
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(exercise: ExerciseEntity): Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(exercises: List<ExerciseEntity>)

    /**
     * Fills in muscle data for exercises that predate the muscles column
     * (seeded rows from schema v2). Only touches rows still blank.
     */
    @Query("UPDATE exercises SET muscles = :muscles WHERE name = :name AND muscles = ''")
    suspend fun backfillMuscles(name: String, muscles: String)

    @Delete
    suspend fun delete(exercise: ExerciseEntity)
}
