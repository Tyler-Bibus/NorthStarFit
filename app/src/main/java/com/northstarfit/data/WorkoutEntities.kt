package com.northstarfit.data

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.Relation

/**
 * One completed workout session. Movements and sets hang off it via
 * foreign keys, so deleting a workout cascades to everything under it.
 */
@Entity(tableName = "workouts")
data class WorkoutEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    /** Epoch millis when the workout was started. */
    val startedAt: Long,
    /** Epoch millis when the workout was finished. */
    val endedAt: Long,
)

/** One exercise performed within a workout (e.g. "Bench press"). */
@Entity(
    tableName = "movements",
    foreignKeys = [
        ForeignKey(
            entity = WorkoutEntity::class,
            parentColumns = ["id"],
            childColumns = ["workoutId"],
            onDelete = ForeignKey.CASCADE,
        )
    ],
    indices = [Index("workoutId")],
)
data class MovementEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val workoutId: Long,
    val exercise: String,
)

/** One set of a movement: how much weight for how many reps. */
@Entity(
    tableName = "sets",
    foreignKeys = [
        ForeignKey(
            entity = MovementEntity::class,
            parentColumns = ["id"],
            childColumns = ["movementId"],
            onDelete = ForeignKey.CASCADE,
        )
    ],
    indices = [Index("movementId")],
)
data class SetEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val movementId: Long,
    val weight: Double,
    val reps: Int,
)

/** A movement together with all of its sets, as read back from the database. */
data class MovementWithSets(
    @Embedded val movement: MovementEntity,
    @Relation(parentColumn = "id", entityColumn = "movementId")
    val sets: List<SetEntity>,
)

/** A full workout with every movement and set, for the history screen. */
data class WorkoutWithMovements(
    @Embedded val workout: WorkoutEntity,
    @Relation(entity = MovementEntity::class, parentColumn = "id", entityColumn = "workoutId")
    val movements: List<MovementWithSets>,
) {
    val totalSets: Int get() = movements.sumOf { it.sets.size }
    val totalReps: Int get() = movements.sumOf { m -> m.sets.sumOf { it.reps } }
    val totalVolume: Double get() = movements.sumOf { m -> m.sets.sumOf { it.weight * it.reps } }
}
