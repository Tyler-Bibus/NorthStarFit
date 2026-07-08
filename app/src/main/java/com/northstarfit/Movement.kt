package com.northstarfit

import android.util.Log
import android.widget.LinearLayout


/**
 * subclass of Workout
 * Contains the current sets and type of lift
 */
open class Movement (workout: Workout, type: String, linLay: LinearLayout) {
    private var exercise: String
    private var sets: ArrayList<Set>
    private lateinit var thisWorkout: Workout
    private var linearLayout: LinearLayout

    init{
        Log.d("Movement", "New Movement Created")
        exercise = type
        sets = ArrayList<Set>()
        thisWorkout = workout
        linearLayout = linLay
        linearLayout.orientation = LinearLayout.VERTICAL
        MovementVeiw(this, linLay.context, null)
    }


    /**
     * Sets the exercise string
     */
    fun setExcercise(x: String){
        exercise = x
    }

    /**
     * adds Set to array list and updates
     * @param lin
     */
    fun addSet(lin: LinearLayout){
        val newSet = Set(lin, this)
        sets.add(newSet)
        newSet.addSetToView()
    }

    /**
     * Removes the set in the ArrayList and updates
     * @param set
     */
    fun removeSet(set: Set){
        sets.remove(set)
        thisWorkout.calculateAndSet()
    }

    /**
     * Removes all the sets from the movement
     */
    fun removeAllSets(){
        for(set in sets){
            set.removeFromView()
        }
        sets.clear()
    }

    /**
     * returns the sets ArrayLiset
     * @return ArrayList<Set>
     */
    fun getSets(): ArrayList<Set> {
        return sets
    }

    /**
     * returns the current workout
     */
    fun getWorkout(): Workout{
        return thisWorkout
    }

    /**
     * returns the linearLayout
     */
    fun getLinearLayout(): LinearLayout{
        return linearLayout
    }

    /**
     * Returns the current exercise
     */
    override fun toString(): String {
        return exercise
    }
}