package com.northstarfit

import android.util.Log


open class Workout {
    private var setsCompleted = 0
    private var totalVolume = 0
    private var excercises: ArrayList<Movement>

    init{
        Log.d("Workout", "New Workout Started")
        excercises = ArrayList<Movement>()
    }

    public fun getMovements(): ArrayList<Movement> {
        Log.d("Workout", "Returned Movements")
        return excercises
    }
    public fun completeSet(){
        setsCompleted++
    }


}