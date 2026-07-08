package com.northstarfit

import android.app.Application
import com.northstarfit.data.WorkoutDatabase
import com.northstarfit.data.WorkoutRepository

/**
 * Application class that owns the app-wide dependencies (database and
 * repository). ViewModels grab the repository from here via their factories,
 * so there is a single database instance for the whole process.
 */
class NorthStarFitApp : Application() {

    val repository: WorkoutRepository by lazy {
        val db = WorkoutDatabase.getInstance(this)
        WorkoutRepository(db.workoutDao(), db.exerciseDao())
    }
}
