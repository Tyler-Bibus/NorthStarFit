package com.northstarfit

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView

class SetView (set: Set, context: Context, attrs: AttributeSet?)  : View(context, attrs){

    private var tvExcercise: TextView
    private lateinit var etSetWeight: EditText
    private lateinit var etSetReps: EditText
    private lateinit var btDeleteSet: ImageButton

    init {
        val inflater = LayoutInflater.from(this.context)
        val view = inflater.inflate(R.layout.set_layout, null, false)

        var linearLayout = set.getLinearLayout()
        var excercise = set.toString()
        tvExcercise = view.findViewById(R.id.tvExcercise)
        tvExcercise.text = excercise
        etSetWeight = view.findViewById(R.id.etSetWeight)
        etSetReps = view.findViewById(R.id.etSetReps)
        btDeleteSet = view.findViewById(R.id.btDeleteSet)
        etSetWeight.setText(set.getWeight().toString())
        etSetReps.setText(set.getReps().toString())
        linearLayout.addView(view)

        btDeleteSet.setOnClickListener{
            linearLayout.removeView(view)
            set.removeSet(set)
        }
    }



}