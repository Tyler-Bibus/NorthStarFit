package com.northstarfit

import android.os.Bundle
import android.util.Log

/**
 * subclass of Workout
 * Contains the current sets and type of lift
 */
open class Movement (type: String) : Workout() {
    private var excercise: String
    private var sets: ArrayList<Set>

    init{
        Log.d("Movement", "New Movement Created")
        excercise = type
        sets = ArrayList<Set>()
    }


    fun setExcercise(x: String){
        excercise = x
    }

    fun addSet(weight: Int){
        val newSet = Set(weight, excercise)
        sets.add(newSet)
    }

    fun removeSet(set: Set){
        sets.remove(set)
    }

    fun getSets(): ArrayList<Set> {
        return sets
    }

    override fun toString(): String {
        return excercise
    }
}