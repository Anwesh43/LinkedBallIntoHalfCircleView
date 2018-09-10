package com.anwesh.uiprojects.linkedballintohalfview

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import com.anwesh.uiprojects.ballintohalfview.BallIntoHalfView

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        BallIntoHalfView.create(this)
    }
}
