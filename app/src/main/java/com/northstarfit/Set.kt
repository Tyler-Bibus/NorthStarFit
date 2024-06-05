package com.northstarfit

import android.util.Log
import android.widget.LinearLayout

class Set(linLay: LinearLayout, currentMovement: Movement){
    private var currentWeight = 0.0
    private var currentReps = 0
    private lateinit var lay: LinearLayout
    private lateinit var movement: Movement
    private lateinit var setView: SetView

    init {
        lay = linLay
        movement = currentMovement
    }

    fun setWeight(weightNew: Double){
        Log.d("Movement", "Weight Changed")
        currentWeight = weightNew
        calculateTotals()
    }

    fun setReps(newReps: Int){
        Log.d("Set", "Reps Changed")
        currentReps = newReps
        calculateTotals()
    }

    fun getReps(): Int {
        return currentReps
    }

    fun getWeight(): Double {
        return currentWeight
    }

    fun getLinearLayout(): LinearLayout{
        return lay
    }
    fun getMovement(): Movement{
        return movement
    }

    fun addSetToView(){
       setView = SetView(this, lay.context, null)
    }

    fun removeFromView(){
        setView.deleteSetFromView()
    }

    override fun toString(): String {
        return movement.toString()
    }

    private fun calculateTotals(){
        this.getMovement().getWorkout().calculateAndSet()
    }
}