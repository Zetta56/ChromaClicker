package com.example.csac.activity


import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.csac.R
import com.example.csac.models.Save
import kotlinx.serialization.json.Json
import java.io.File
import java.io.FileOutputStream
import java.io.ObjectOutputStream


class MainActivity : AppCompatActivity() {
    var overlayVisible: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
//        val inputStream = File(filesDir, "save-test name").inputStream()
//        val inputString = inputStream.bufferedReader().use { it.readText() }
//        println(inputString)
    }

//    override fun onSaveInstanceState(outState: Bundle) {
//        super.onSaveInstanceState(outState)
//        outState.putBoolean("overlayVisible", overlayVisible)
//    }
//
//    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
//        super.onRestoreInstanceState(savedInstanceState)
//        overlayVisible = savedInstanceState.getBoolean("overlayVisible")
//    }
}