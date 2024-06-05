package com.northstarfit

import android.util.Log
import android.widget.LinearLayout


/**
 * subclass of Workout
 * Contains the current sets and type of lift
 */
open class Movement (workout: Workout, type: String, linLay: LinearLayout) {
    private var excercise: String
    private var sets: ArrayList<Set>
    private lateinit var thisWorkout: Workout
    private var linearLayout: LinearLayout

    init{
        Log.d("Movement", "New Movement Created")
        excercise = type
        sets = ArrayList<Set>()
        thisWorkout = workout
        linearLayout = linLay
        MovementVeiw(this, linLay.context, null)
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

    fun getWorkout(): Workout{
        return thisWorkout
    }

    fun getLinearLayout(): LinearLayout{
        return linearLayout
    }


    override fun toString(): String {
        return excercise
    }
}