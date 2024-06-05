package com.northstarfit

import android.util.Log
import android.widget.LinearLayout


open class Workout(linLay: LinearLayout) : WorkoutActivity() {
    private var setsCompleted = 0
    private var totalVolume = 0.0
    private var excercises: ArrayList<Movement>
    private var linearLay: LinearLayout

    init{
        Log.d("Workout", "New Workout Started")
        excercises = ArrayList<Movement>()
        linearLay = linLay
    }

    public fun getMovements(): ArrayList<Movement> {
        Log.d("Workout", "Returned Movements")
        return excercises
    }
    public open fun completeSet(){
        setsCompleted++
    }

    fun addVolume(vol: Double){
        totalVolume += vol
    }

    fun getLinearLayout(): LinearLayout {
        return linearLay
    }

    fun newMovement(type: String): Movement {
        val newMovement = Movement(type, linearLay)
        excercises.add(newMovement)
        return newMovement
    }

    fun removeMovement(movement: Movement){
        excercises.remove(movement)
    }


}