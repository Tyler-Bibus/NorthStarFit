package com.northstarfit

import android.util.Log

class Set (Weight: Int, type: String) : Movement(type) {
    private var currentWeight = 0


    init {
        currentWeight = Weight
        this.setExcercise(type)
    }

    public fun changeWeight(weightNew: Int){
        Log.d("Movement", "Weight Changed")
        currentWeight = weightNew
    }
}