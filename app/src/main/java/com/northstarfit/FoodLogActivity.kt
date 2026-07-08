package com.northstarfit

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.activity.ComponentActivity

class FoodLogActivity : ComponentActivity(){

    private lateinit var btBack: Button
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.foodlog_layout)

        btBack = findViewById(R.id.btFoodToHome)

        btBack.setOnClickListener{
            //TODO save this log somewhere...
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }
    }

}