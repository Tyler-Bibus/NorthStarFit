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


class MainActivity : ComponentActivity() {

    private lateinit var btWorkout: Button
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.home_layout)

        btWorkout = findViewById(R.id.btStartWorkout)
        btWorkout.setOnClickListener{
            val intent = Intent(this, WorkoutActivity::class.java)
            startActivity(intent)
        }
    }
}
