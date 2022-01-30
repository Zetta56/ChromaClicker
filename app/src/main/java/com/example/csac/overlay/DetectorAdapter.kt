package com.example.csac.overlay

import android.content.res.Resources
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.core.widget.doAfterTextChanged
import androidx.recyclerview.widget.RecyclerView
import com.example.csac.R
import com.example.csac.databinding.OverlayCanvasBinding
import com.example.csac.models.DetectorParcel

class DetectorAdapter(
    private val detectors: MutableList<DetectorView>,
    private val detectorParcels: MutableList<DetectorParcel>
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
        holder.inputX.doAfterTextChanged {
            val x = holder.inputX.text.toString()
            if(x.isNotEmpty() && 0 <= x.toFloat() && x.toFloat() <= Resources.getSystem().displayMetrics.widthPixels) {
                detectors[position].endX = x.toFloat()
                detectors[position].invalidate()
            }
        }
        holder.inputY.doAfterTextChanged {
            val y = holder.inputY.text.toString()
            if(y.isNotEmpty() && 0 <= y.toFloat() && y.toFloat() <= Resources.getSystem().displayMetrics.heightPixels) {
                detectors[position].endY = y.toFloat()
                detectors[position].invalidate()
            }
        }
        holder.inputColor.doAfterTextChanged {
            val color = "#${holder.inputColor.text}"
            // parseColor inside try...catch makes sure inputted color-string is valid
            try {
                Color.parseColor(color)
                detectors[position].color = color
                detectors[position].invalidate()
            } catch(e: IllegalArgumentException) {}
        }
    }

    override fun getItemCount(): Int {
        return detectors.size
    }
}