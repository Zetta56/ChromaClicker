package com.example.csac.main


import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.csac.R


class MainActivity : AppCompatActivity() {
    var overlayVisible: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }
}