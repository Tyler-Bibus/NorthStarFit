package com.northstarfit

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.Spinner
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.core.view.isVisible

/**
 * This class is the overall WorkoutActivity that holds all the movements
 * @author Tyler Bibus
 */
open class WorkoutActivity : ComponentActivity() {
    private lateinit var tvTotalSets: TextView
    private lateinit var spExcercise: Spinner
    private lateinit var tvVolume: TextView
    private lateinit var tvTotalReps: TextView
    private lateinit var linearLayout: LinearLayout
    private lateinit var btAddMovement: Button
    private lateinit var btEndWorkout: Button
    private lateinit var svSets: ScrollView
    private var setsCompleted = 0
    private var totalVolume = 0.0
    private var totalReps = 0
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.log_workout)
        svSets = findViewById(R.id.svSets)
        linearLayout = LinearLayout(this)
        linearLayout.orientation = LinearLayout.VERTICAL
        svSets.removeAllViews()
        svSets.addView(linearLayout)
        svSets.isVisible = true
        val thisWorkout = Workout(this, linearLayout)
        var currMovement: Movement

        btAddMovement = findViewById(R.id.btAddMovement)
        tvTotalSets = findViewById(R.id.tvSetsCompleted)
        tvVolume = findViewById(R.id.tvTotalVolume)
        tvTotalReps = findViewById(R.id.tvTotalReps)
        spExcercise = findViewById(R.id.spExcercise)
        btEndWorkout = findViewById(R.id.btEndWorkout)


        //This button will create a new workout/movement set
        btAddMovement.setOnClickListener{
            val newMovement = thisWorkout.newMovement(spExcercise.selectedItem.toString())
            Log.d("WorkoutActivity", newMovement.toString())
            currMovement = newMovement
        }

        //This button will end the workout
        btEndWorkout.setOnClickListener{
            //TODO save this workout somewhere...
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }
    }

    /** This function will set the weights on the display
     * @param totalSets
     * @param totalReps
     * @param totalVolume
     */
    fun setWeights(totalSets: Int, totalReps: Int, totalVolume: Double){
        tvVolume.text = "Volume: " + "%.2f".format(totalVolume)
        tvTotalReps.text = "Reps: " + totalReps.toString()
        tvTotalSets.text = "Sets: " + totalSets.toString()
    }
}