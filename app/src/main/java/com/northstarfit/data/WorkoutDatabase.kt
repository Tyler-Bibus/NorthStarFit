package com.northstarfit.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [
        WorkoutEntity::class,
        MovementEntity::class,
        SetEntity::class,
        ExerciseEntity::class,
        DraftEntity::class,
    ],
    version = 3,
    exportSchema = true,
)
abstract class WorkoutDatabase : RoomDatabase() {

    abstract fun workoutDao(): WorkoutDao
    abstract fun exerciseDao(): ExerciseDao

    companion object {
        @Volatile
        private var instance: WorkoutDatabase? = null

        /** v1 -> v2: adds the exercise library and the in-progress draft table. */
        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS `exercises` " +
                        "(`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `name` TEXT NOT NULL)"
                )
                db.execSQL(
                    "CREATE UNIQUE INDEX IF NOT EXISTS `index_exercises_name` ON `exercises` (`name`)"
                )
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS `workout_drafts` " +
                        "(`id` INTEGER NOT NULL, `startedAt` INTEGER NOT NULL, " +
                        "`movementsJson` TEXT NOT NULL, PRIMARY KEY(`id`))"
                )
            }
        }

        /** v2 -> v3: exercises gain muscle tags, workouts gain a name. */
        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE `exercises` ADD COLUMN `muscles` TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE `workouts` ADD COLUMN `name` TEXT NOT NULL DEFAULT ''")
            }
        }

        fun getInstance(context: Context): WorkoutDatabase =
            instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    WorkoutDatabase::class.java,
                    "northstarfit.db",
                )
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3)
                    .build()
                    .also { instance = it }
            }
    }
}
