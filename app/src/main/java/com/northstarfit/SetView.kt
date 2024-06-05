package com.northstarfit

import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView

class SetView (set: Set, context: Context, attrs: AttributeSet?)  : View(context, attrs){

    private var tvExcercise: TextView
    private lateinit var etSetWeight: EditText
    private lateinit var etSetReps: EditText
    private lateinit var btDeleteSet: ImageButton
    private lateinit var linearLayout: LinearLayout
    private var thisSet: Set
    private val view: View

    init {
        val inflater = LayoutInflater.from(this.context)
        view = inflater.inflate(R.layout.set_layout, null, false)
        thisSet = set
        linearLayout = set.getLinearLayout()
        var excercise = set.toString()
        tvExcercise = view.findViewById(R.id.tvExcercise)
        tvExcercise.text = excercise
        etSetWeight = view.findViewById(R.id.etSetWeight)
        etSetReps = view.findViewById(R.id.etSetReps)
        btDeleteSet = view.findViewById(R.id.btDeleteSet)
        linearLayout.addView(view)

        btDeleteSet.setOnClickListener{
            linearLayout.removeView(view)
            set.getMovement().removeSet(set)
        }
        etSetWeight.addTextChangedListener( object: TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                if (s.isNullOrBlank()){
                    set.setWeight(0.0)
                    return
                }
                set.setWeight(s.toString().toDouble())
            }

        })

        etSetReps.addTextChangedListener( object: TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                if (s.isNullOrBlank()){
                    set.setReps(0)
                    return
                }
                set.setReps(s.toString().toInt())
            }

        })


    }

    fun deleteSetFromView(){
        linearLayout.removeView(view)
    }



}