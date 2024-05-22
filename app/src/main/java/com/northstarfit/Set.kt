package com.northstarfit

import android.util.AttributeSet
import android.util.Log
import android.widget.LinearLayout

class Set (Weight: Double, type: String, linLay: LinearLayout) : Movement(type, linLay) {
    private var currentWeight = 0.0
    private var currentReps = 0


    init {
        currentWeight = Weight
        this.setExcercise(type)
    }

    public fun changeWeight(weightNew: Double){
        Log.d("Movement", "Weight Changed")
        currentWeight = weightNew
    }

    override fun completeSet() {
        this.addVolume(currentWeight * currentReps)
        super.completeSet()
    }

    fun addSetToView(){
        SetView(this, this.getLinearLayout().context, null)
    }
}