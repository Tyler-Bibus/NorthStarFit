package com.northstarfit

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.LinearLayout


/**
 * subclass of Workout
 * Contains the current sets and type of lift
 */
open class Movement (type: String, linLay: LinearLayout) : Workout(linLay){
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

    fun addSet(weight: Double){
        val newSet = Set(weight, excercise, this.getLinearLayout())
        sets.add(newSet)
        newSet.addSetToView()
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