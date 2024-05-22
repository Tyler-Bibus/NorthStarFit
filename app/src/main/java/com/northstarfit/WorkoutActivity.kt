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

open class WorkoutActivity : ComponentActivity() {
    private lateinit var btSubmit: Button
    private lateinit var tvTotalSets: TextView
    private lateinit var spExcercise: Spinner
    private lateinit var etWeight: EditText
    private lateinit var tvVolume: TextView
    private lateinit var etReps: EditText
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
        val thisWorkout = Workout(linearLayout)
        var currMovement = thisWorkout.newMovement("bruh")

        btAddMovement = findViewById(R.id.btAddMovement)
        btSubmit = findViewById(R.id.btSubmitExcercise)
        tvTotalSets = findViewById(R.id.tvSetsCompleted)
        etWeight = findViewById(R.id.etWeight)
        tvVolume = findViewById(R.id.tvTotalVolume)
        etReps = findViewById(R.id.etReps)
        tvTotalReps = findViewById(R.id.tvTotalReps)
        spExcercise = findViewById(R.id.spExcercise)
        btEndWorkout = findViewById(R.id.btEndWorkout)
        setWeights()

        btAddMovement.setOnClickListener{
            val newMovement = Movement(spExcercise.selectedItem.toString(), linearLayout)
            Log.d("WorkoutActivity", newMovement.toString())
            currMovement = newMovement
        }

        btSubmit.setOnClickListener{
            Log.d("MainActivity", "Workout Submitted")
            setWeights()
            if (etWeight.text.isEmpty() || etReps.text.isEmpty()){
                currMovement.addSet(0.0, 0)
                return@setOnClickListener
            }
            currMovement.addSet(etWeight.text.toString().toDouble(), etReps.text.toString().toInt())
        }

        btEndWorkout.setOnClickListener{
            //TODO save this workout somewhere...
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }
    }

    private fun setWeights() {
        if (etWeight.text.isEmpty() || etReps.text.isEmpty()){
            tvTotalSets.text = "Sets: " + setsCompleted.toString()
            tvVolume.text = "Volume: " + totalVolume.toString()
            tvTotalReps.text = "Reps: " + totalReps.toString()
            return
        }
        setsCompleted++
        totalReps += etReps.text.toString().toInt()
        totalVolume += etWeight.text.toString().toDouble() * etReps.text.toString().toInt()
        tvTotalSets.text = "Sets: " + setsCompleted.toString()
        tvVolume.text = "Volume: " + "%.2f".format(totalVolume)
        tvTotalReps.text = "Reps: " + totalReps.toString()
    }
}