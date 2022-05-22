package com.example.chromaclicker.overlay

import android.annotation.SuppressLint
import android.content.*
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageButton
import android.widget.LinearLayout
import androidx.core.widget.doAfterTextChanged
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.recyclerview.widget.RecyclerView
import com.example.chromaclicker.R
import com.example.chromaclicker.autoclick.AutoClickService
import com.example.chromaclicker.getScreenHeight
import com.example.chromaclicker.getScreenWidth

class DetectorAdapter(
    private val context: Context,
    private val detectorViews: MutableList<DetectorView>,
    private val container: ViewGroup
) : RecyclerView.Adapter<DetectorAdapter.ViewHolder>() {

    class ViewHolder(view: View) :  RecyclerView.ViewHolder(view) {
        val dipperButton: ImageButton = view.findViewById(R.id.dipperButton)
        val inputX: EditText = view.findViewById(R.id.inputX)
        val inputY: EditText = view.findViewById(R.id.inputY)
        val inputColor: EditText = view.findViewById(R.id.inputColor)
        val crossButton: ImageButton = view.findViewById(R.id.crossButton)
    }

    class DipperReceiver(private val holder: ViewHolder, private val event: MotionEvent) : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            holder.inputX.setText(event.x.toInt().toString())
            holder.inputY.setText(event.y.toInt().toString())
            holder.inputColor.setText(intent.getStringExtra("color"))
            LocalBroadcastManager.getInstance(context).unregisterReceiver(this)
        }
    }

    private val menu = container.findViewById<LinearLayout>(R.id.menu)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.detector_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val detectorView = detectorViews[position]
        holder.dipperButton.setOnClickListener { toggleDipper(holder) }
        holder.inputX.doAfterTextChanged { redrawX(detectorView, holder.inputX) }
        holder.inputY.doAfterTextChanged { redrawY(detectorView, holder.inputY) }
        holder.inputColor.doAfterTextChanged { redrawColor(detectorView, holder.inputColor.text.toString()) }
        holder.crossButton.setOnClickListener { removeDetector(detectorView, position) }

        // Set default values
        holder.inputX.setText(detectorView.endX.toInt().toString())
        holder.inputY.setText(detectorView.endY.toInt().toString())
        holder.inputColor.setText(detectorView.color)
    }

    override fun getItemCount(): Int {
        return detectorViews.size
    }

    private fun redrawX(detectorView: DetectorView, input: EditText) {
        val text = input.text.toString()
        if(text.isNotEmpty() && 0 <= text.toFloat() && text.toFloat() <= getScreenWidth()) {
            detectorView.endX = text.toFloat()
            detectorView.invalidate()
        }
    }

    private fun redrawY(detectorView: DetectorView, input: EditText) {
        val text = input.text.toString()
        if(text.isNotEmpty() && 0 <= text.toFloat() && text.toFloat() <= getScreenHeight()) {
            detectorView.endY = text.toFloat()
            detectorView.invalidate()
        }
    }

    private fun redrawColor(detectorView: DetectorView, color: String) {
        val hexRegex = Regex("^#[0-9A-Fa-f]{6}\$")
        if(color.matches(hexRegex)) {
            detectorView.color = color
            detectorView.invalidate()
        }
    }

    private fun removeDetector(detectorView: DetectorView, position: Int) {
        container.removeView(detectorView)
        detectorViews.removeAt(position)
        notifyItemRemoved(position)
        notifyItemRangeChanged(position, itemCount)
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun toggleDipper(holder: ViewHolder) {
        if(AutoClickService.instance?.projection != null) {
            menu.visibility = View.INVISIBLE
            container.setOnTouchListener { _, event ->
                if(event.action == MotionEvent.ACTION_UP) {
                    dip(holder, event)
                    menu.visibility = View.VISIBLE
                    container.setOnTouchListener(null)
                    true
                } else {
                    false
                }
            }
        }
    }

    private fun dip(holder: ViewHolder, event: MotionEvent) {
        LocalBroadcastManager.getInstance(context).registerReceiver(
            DipperReceiver(holder, event),
            IntentFilter("receive_pixel_color")
        )
        val intent = Intent(context, AutoClickService::class.java)
        // This starts from top of app (below status bar)
        intent.putExtra("x", event.x.toInt())
        intent.putExtra("y", event.y.toInt())
        intent.action = "get_pixel_color"
        context.startService(intent)
    }
}