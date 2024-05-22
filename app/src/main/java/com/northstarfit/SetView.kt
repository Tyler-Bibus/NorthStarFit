package com.northstarfit

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView

class SetView (set: Set, context: Context, attrs: AttributeSet?)  : View(context, attrs){

    private lateinit var tvExcercise: TextView
    init {
        val inflater = LayoutInflater.from(this.context)
        val view = inflater.inflate(R.layout.set_layout, null, false)

        var linearLayout = set.getLinearLayout()
        var excercise = set.toString()
        tvExcercise = view.findViewById(R.id.tvExcercise)
        tvExcercise.text = excercise
        linearLayout.addView(view)

    }

}