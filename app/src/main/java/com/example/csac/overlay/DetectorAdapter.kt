package com.example.csac.overlay

import android.content.res.Resources
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageButton
import androidx.core.widget.doAfterTextChanged
import androidx.recyclerview.widget.RecyclerView
import com.example.csac.R

class DetectorAdapter(
    private val detectorViews: MutableList<DetectorView>,
    private val drawing: ViewGroup
) : RecyclerView.Adapter<DetectorAdapter.ViewHolder>() {

    class ViewHolder(view: View) :  RecyclerView.ViewHolder(view) {
        val inputX: EditText = view.findViewById(R.id.inputX)
        val inputY: EditText = view.findViewById(R.id.inputY)
        val inputColor: EditText = view.findViewById(R.id.inputColor)
        val inputDelete: ImageButton = view.findViewById(R.id.inputDelete)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.detector_form, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val detectorView = detectorViews[position]

        // Set default values
        holder.inputX.setText(detectorView.endX.toInt().toString())
        holder.inputY.setText(detectorView.endY.toInt().toString())
        holder.inputColor.setText(detectorView.color.substring(1)) // Starting at index 1 excludes hashbang

        // Set input handlers
        holder.inputX.doAfterTextChanged {
            val x = holder.inputX.text.toString()
            if(x.isNotEmpty() && 0 <= x.toFloat() && x.toFloat() <= Resources.getSystem().displayMetrics.widthPixels) {
                detectorView.endX = x.toFloat()
                detectorView.invalidate()
            }
        }
        holder.inputY.doAfterTextChanged {
            val y = holder.inputY.text.toString()
            if(y.isNotEmpty() && 0 <= y.toFloat() && y.toFloat() <= Resources.getSystem().displayMetrics.heightPixels) {
                detectorView.endY = y.toFloat()
                detectorView.invalidate()
            }
        }
        holder.inputColor.doAfterTextChanged {
            val color = "#${holder.inputColor.text}"
            // parseColor inside try...catch makes sure inputted color-string is valid
            try {
                Color.parseColor(color)
                detectorView.color = color
                detectorView.invalidate()
            } catch(e: IllegalArgumentException) {}
        }
        holder.inputColor.setOnFocusChangeListener { _, hasFocus: Boolean ->
            if(hasFocus) {
                holder.inputColor.setSelection(holder.inputColor.length())
            }
        }
        holder.inputDelete.setOnClickListener {
            drawing.removeView(detectorView)
            detectorViews.removeAt(position)
            notifyItemRemoved(position)
            notifyItemRangeChanged(position, itemCount)
        }
    }

    override fun getItemCount(): Int {
        return detectorViews.size
    }
}