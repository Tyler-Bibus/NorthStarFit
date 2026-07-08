package com.northstarfit

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.PersistableBundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Spinner
import android.widget.TextView
import androidx.activity.ComponentActivity
import com.northstarfit.Movement
import kotlinx.coroutines.android.awaitFrame

/**
 * This class is the main title screen of the app holding all other class types
 * @author Tyler Bibus
 *
 */
class MainActivity : ComponentActivity() {

    private lateinit var btWorkout: Button
    private lateinit var btFoodLog: Button
    private lateinit var btStore: Button
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.home_layout)

        //assigns buttons
        btWorkout = findViewById(R.id.btStartWorkout)
        btFoodLog = findViewById(R.id.btFoodLog)
        btStore = findViewById(R.id.btStore)

        //starts a workout
        btWorkout.setOnClickListener{
            val workoutIntent = Intent(this, WorkoutActivity::class.java)
            startActivity(workoutIntent)
        }

        //starts food log activity
        btFoodLog.setOnClickListener{
            val foodIntent = Intent(this, FoodLogActivity::class.java)
            startActivity(foodIntent)
        }
    }
}
