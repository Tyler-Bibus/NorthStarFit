package com.northstarfit

import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.TextView
import androidx.activity.ComponentActivity



class MainActivity : ComponentActivity() {
    private lateinit var btSubmit: Button
    private lateinit var tvTotalSets: TextView
    private lateinit var spExcercise: Spinner
    private lateinit var etWeight: EditText
    private lateinit var tvVolume: TextView
    private var setsCompleted = 0
    private var totalVolume = 0.0
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.log_workout)
        btSubmit = findViewById(R.id.btSubmitExcercise)
        tvTotalSets = findViewById(R.id.tvSetsCompleted)
        etWeight = findViewById(R.id.etWeight)
        tvVolume = findViewById(R.id.tvTotalVolume)
        setWeights()


        btSubmit.setOnClickListener{
            Log.d("MainActivity", "Workout Submitted")
            setWeights()
        }

    }

    private fun setWeights() {
        if (etWeight.text.isEmpty()){
            tvTotalSets.text = "Sets: " + setsCompleted.toString()
            tvVolume.text = "Volume: " + totalVolume.toString()
            return
        }
        setsCompleted++
        totalVolume += etWeight.text.toString().toDouble()
        tvTotalSets.text = "Sets: " + setsCompleted.toString()
        tvVolume.text = "Volume: " + "%.2f".format(totalVolume)
    }


}
