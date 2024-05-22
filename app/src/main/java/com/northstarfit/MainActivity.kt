package com.northstarfit

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Spinner
import android.widget.TextView
import androidx.activity.ComponentActivity
import com.northstarfit.Movement



class MainActivity : ComponentActivity() {
    private lateinit var btSubmit: Button
    private lateinit var tvTotalSets: TextView
    private lateinit var spExcercise: Spinner
    private lateinit var etWeight: EditText
    private lateinit var tvVolume: TextView
    private lateinit var etReps: EditText
    private lateinit var tvTotalReps: TextView
    private var setsCompleted = 0
    private var totalVolume = 0.0
    private var totalReps = 0
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.log_workout)
        btSubmit = findViewById(R.id.btSubmitExcercise)
        tvTotalSets = findViewById(R.id.tvSetsCompleted)
        etWeight = findViewById(R.id.etWeight)
        tvVolume = findViewById(R.id.tvTotalVolume)
        etReps = findViewById(R.id.etReps)
        tvTotalReps = findViewById(R.id.tvTotalReps)
        val jazz = ArrayList<Movement>()
        setWeights()
        val benchPress = Movement("Bench Press")
        benchPress.addSet(10)
        val benchSets = benchPress.getSets()
        benchSets.get(0).changeWeight(100)
        jazz.add(benchPress)
        for(m : Movement in jazz){
            Log.d("MainActivity", m.toString() + ": Current Movements")
        }
        benchPress.removeSet(benchSets.get(0))


        btSubmit.setOnClickListener{
            Log.d("MainActivity", "Workout Submitted")
            setWeights()
        }

    }

    private fun setWeights() {
        if (etWeight.text.isEmpty()){
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
