package com.northstarfit

import android.util.Log
import android.widget.LinearLayout


open class Workout(activity: WorkoutActivity, linLay: LinearLayout) {
    private var setsCompleted = 0
    private var totalVolume = 0.0
    private var excercises: ArrayList<Movement>
    private var linearLay: LinearLayout
    private var thisActivity: WorkoutActivity

    init{
        Log.d("Workout", "New Workout Started")
        excercises = ArrayList<Movement>()
        linearLay = linLay
        thisActivity = activity
    }

    /**
     * returns the ArrayList of movements
     */
    public fun getMovements(): ArrayList<Movement> {
        Log.d("Workout", "Returned Movements")
        return excercises
    }

    /**
     * iterates the number of sets completed
     */
    public open fun completeSet(){
        setsCompleted++
    }

    /**
     * adds the volume to the workout classes volume
     * @param vol
     */
    fun addVolume(vol: Double){
        totalVolume += vol
    }

    /**
     * returns the linear Layout of the workout
     */
    fun getLinearLayout(): LinearLayout {
        return linearLay
    }

    /**
     * creates and returns a new movement
     * @param type
     */
    fun newMovement(type: String): Movement {
        val newMovement = Movement(this, type, linearLay)
        excercises.add(newMovement)
        return newMovement
    }

    /**
     * removes movement from the ArrayList
     * also updates the sets and reps
     * @param movement
     */
    fun removeMovement(movement: Movement){
        excercises.remove(movement)
        calculateAndSet()
    }

    /**
     * Returns the Current WorkoutActivity
     */
    fun getActivity(): WorkoutActivity{
        return thisActivity
    }

    /**
     * Calculates and sets the weight, reps, volume, and sets
     *
     */
    fun calculateAndSet(){
        var totalWeight = 0.0
        var totalReps = 0
        var totalVolume = 0.0
        var totalSets = 0
        for(excercise in excercises){
            val sets = excercise.getSets()
            for (set in sets){
                if (set.getWeight().isNaN() || set.getReps() <= 0 || set.getWeight() <= 0){
                    continue
                }
                totalSets++
                totalWeight += set.getWeight()
                totalReps += set.getReps()
                totalVolume += set.getReps() * set.getWeight()
            }
        }
        thisActivity.setWeights(totalSets, totalReps, totalVolume)
    }


}