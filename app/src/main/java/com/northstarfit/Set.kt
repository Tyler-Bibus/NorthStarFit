package com.northstarfit

import android.util.AttributeSet
import android.util.Log
import android.widget.LinearLayout

class Set (Weight: Double, reps: Int, type: String, linLay: LinearLayout) : Movement(type, linLay) {
    private var currentWeight = 0.0
    private var currentReps = 0


    init {
        currentWeight = Weight
        currentReps = reps
        this.setExcercise(type)
    }

    fun changeWeight(weightNew: Double){
        Log.d("Movement", "Weight Changed")
        currentWeight = weightNew
    }

    fun changeReps(newReps: Int){
        Log.d("Set", "Reps Changed")
        currentReps = newReps
    }


    override fun completeSet() {
        this.addVolume(currentWeight * currentReps)
        super.completeSet()
    }

    fun getReps(): Int {
        return currentReps
    }

    fun getWeight(): Double {
        return currentWeight
    }

    fun addSetToView(){
        SetView(this, this.getLinearLayout().context, null)
    }
}