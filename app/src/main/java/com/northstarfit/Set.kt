package com.northstarfit

import android.util.Log
import android.widget.LinearLayout

class Set(linLay: LinearLayout, currentMovement: Movement){
    private var currentWeight = 0.0
    private var currentReps = 0
    private var lay: LinearLayout
    private var movement: Movement
    private lateinit var setView: SetView

    init {
        lay = linLay
        movement = currentMovement

    }

    /**
     * Sets the weight in the current Set
     * @param weightNew
     */
    fun setWeight(weightNew: Double){
        Log.d("Movement", "Weight Changed")
        currentWeight = weightNew
        calculateTotals()
    }

    /**
     * Sets the Reps of the current Set
     * @param newReps
     */
    fun setReps(newReps: Int){
        Log.d("Set", "Reps Changed")
        currentReps = newReps
        calculateTotals()
    }

    /**
     * Returns the Reps of the Set
     */
    fun getReps(): Int {
        return currentReps
    }

    /**
     * Returns the Weight of the Set
     */
    fun getWeight(): Double {
        return currentWeight
    }

    /**
     * returns current LinearLayout
     */
    fun getLinearLayout(): LinearLayout{
        return lay
    }

    /**
     * Returns the current movement
     */
    fun getMovement(): Movement{
        return movement
    }

    /**
     * Adds the current set to the view
     */
    fun addSetToView(){
       setView = SetView(this, lay.context, null)
    }

    /**
     * Removes the current set to the view
     */
    fun removeFromView(){
        setView.deleteSetFromView()
    }

    /**
     * Override of string, returns the movementType
     */
    override fun toString(): String {
        return movement.toString()
    }

    /**
     * Calculates the total for all the sets
     */
    private fun calculateTotals(){
        this.getMovement().getWorkout().calculateAndSet()
    }
}