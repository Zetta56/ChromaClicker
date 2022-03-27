package com.example.csac.overlay

import android.content.ClipboardManager
import android.content.Context
import android.content.res.Resources
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.core.widget.doAfterTextChanged
import androidx.recyclerview.widget.RecyclerView
import com.example.csac.R

class DetectorAdapter(
    private val context: Context,
    private val detectorViews: MutableList<DetectorView>,
    private val container: ViewGroup
) : RecyclerView.Adapter<DetectorAdapter.ViewHolder>() {

    class ViewHolder(view: View) :  RecyclerView.ViewHolder(view) {
        val pasteButton: ImageButton = view.findViewById(R.id.pasteButton)
        val inputX: EditText = view.findViewById(R.id.inputX)
        val inputY: EditText = view.findViewById(R.id.inputY)
        val inputColor: EditText = view.findViewById(R.id.inputColor)
        val crossButton: ImageButton = view.findViewById(R.id.crossButton)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.detector_form, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val detectorView = detectorViews[position]
        addListeners(holder, detectorView, position)

        // Set default values
        holder.inputX.setText(detectorView.endX.toInt().toString())
        holder.inputY.setText(detectorView.endY.toInt().toString())
        holder.inputColor.setText(detectorView.color)
    }

    override fun getItemCount(): Int {
        return detectorViews.size
    }

    private fun addListeners(holder: ViewHolder, detectorView: DetectorView, position: Int) {
        holder.pasteButton.setOnClickListener {
            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val pasteString = clipboard.primaryClip?.getItemAt(0)?.text.toString()
            if(validatePaste(pasteString)) {
                val pasteArray = pasteString.split(",").toTypedArray()
                holder.inputX.setText(pasteArray[0])
                holder.inputY.setText(pasteArray[1])
                holder.inputColor.setText(pasteArray[2])
            } else {
                Toast.makeText(context, "Invalid paste format. Please use 'Integer,Integer,#RRGGBB'", Toast.LENGTH_SHORT).show()
            }
        }
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
            val color = "${holder.inputColor.text}"
            val hexRegex = Regex("^#[0-9A-Fa-f]{6}\$")
            if(color.matches(hexRegex)) {
                Color.parseColor(color)
                detectorView.color = color
                detectorView.invalidate()
            }
        }
        holder.crossButton.setOnClickListener {
            container.removeView(detectorView)
            detectorViews.removeAt(position)
            notifyItemRemoved(position)
            notifyItemRangeChanged(position, itemCount)
        }
    }

    private fun validatePaste(paste: String?): Boolean {
        if(paste == null) {
            return false
        }
        val pasteArray = paste.split(",").toTypedArray()
        if(pasteArray.size != 3) {
            return false
        }
        val numberRegex = Regex("^\\d+$")
        if(!pasteArray[0].matches(numberRegex) || !pasteArray[1].matches(numberRegex)) {
            return false
        }
        val hexRegex = Regex("^#[0-9A-Fa-f]{6}\$")
        if(!pasteArray[2].matches(hexRegex)) {
            return false
        }
        return true
    }
}