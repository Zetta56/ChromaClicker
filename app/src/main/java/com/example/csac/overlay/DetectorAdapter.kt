package com.example.csac.overlay

import android.view.LayoutInflater
import android.view.SurfaceView
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.core.widget.doAfterTextChanged
import androidx.recyclerview.widget.RecyclerView
import com.example.csac.R
import com.example.csac.models.DetectorParcel

class DetectorAdapter(
    private val detectors: MutableList<DetectorParcel>,
    private val surface: SurfaceView
) : RecyclerView.Adapter<DetectorAdapter.ViewHolder>() {
    class ViewHolder(view: View) :  RecyclerView.ViewHolder(view) {
        val inputX: EditText = view.findViewById(R.id.inputX)
        val inputY: EditText = view.findViewById(R.id.inputY)
        val inputColor: EditText = view.findViewById(R.id.inputColor)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.detector_form, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.inputX.doAfterTextChanged { println("x changed") }
        holder.inputY.doAfterTextChanged { println("y changed") }
        holder.inputColor.doAfterTextChanged { println("color changed") }
    }

    override fun getItemCount(): Int {
        return detectors.size
    }


}