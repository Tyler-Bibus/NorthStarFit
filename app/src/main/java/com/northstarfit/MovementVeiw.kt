package com.northstarfit

import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView

class MovementVeiw (movement: Movement, context: Context, attrs: AttributeSet?)  : View(context, attrs){

    private var tvExcercise: TextView
    private lateinit var btAddSet: ImageButton

    private lateinit var btDeleteMovement: ImageButton
    private lateinit var subLinearLayout: LinearLayout

    init {
        val inflater = LayoutInflater.from(this.context)
        val view = inflater.inflate(R.layout.movement_layout, null, false)

        var linearLayout = movement.getLinearLayout()
        var excercise = movement.toString()
        val thisMovement = movement
        tvExcercise = view.findViewById(R.id.tv_movementName)
        tvExcercise.text = excercise
        btAddSet = view.findViewById(R.id.btAddSet)
        btDeleteMovement = view.findViewById(R.id.btDeleteMovement)

        subLinearLayout = view.findViewById(R.id.llSetContainer)

        linearLayout.addView(view)

        //Removes movement
        btDeleteMovement.setOnClickListener{
            linearLayout.removeView(view)
            Log.d("MovementView", "Removing Movement")
            thisMovement.removeAllSets()
            movement.getWorkout().removeMovement(movement)
        }

        //adds set
        btAddSet.setOnClickListener{
            thisMovement.addSet(subLinearLayout)
        }
    }
}