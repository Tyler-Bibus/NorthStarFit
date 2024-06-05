package com.northstarfit

import android.util.Log
import android.widget.LinearLayout


/**
 * subclass of Workout
 * Contains the current sets and type of lift
 */
open class Movement (type: String, linLay: LinearLayout) : Workout(linLay) {
    private var excercise: String
    private var sets: ArrayList<Set>

    init{
        Log.d("Movement", "New Movement Created")
        excercise = type
        sets = ArrayList<Set>()
        MovementVeiw(this, this.getLinearLayout().context, null)
    }


    fun setExcercise(x: String){
        excercise = x
    }

    fun addSet(lin: LinearLayout){
        val newSet = Set(lin, this)
        sets.add(newSet)
        newSet.addSetToView()
    }

    fun removeSet(set: Set){
        sets.remove(set)
    }

    fun removeAllSets(){
        for(set in sets){
            set.removeFromView()
        }
        sets.clear()
    }

    fun getSets(): ArrayList<Set> {
        return sets
    }


    override fun toString(): String {
        return excercise
    }
}