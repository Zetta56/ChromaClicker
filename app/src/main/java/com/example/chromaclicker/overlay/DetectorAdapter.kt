package com.example.chromaclicker.overlay

import android.annotation.SuppressLint
import android.content.*
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.Button
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
    private val lines: MutableList<Line>,
    private val container: ViewGroup,
    private val clickerCenter: Array<Float>
) : RecyclerView.Adapter<DetectorAdapter.ViewHolder>() {

    class ViewHolder(view: View) :  RecyclerView.ViewHolder(view) {
        val dipperButton: ImageButton? = view.findViewById(R.id.dipperButton)
        val inputX: EditText? = view.findViewById(R.id.inputX)
        val inputY: EditText? = view.findViewById(R.id.inputY)
        val inputColor: EditText? = view.findViewById(R.id.inputColor)
        val crossButton: ImageButton? = view.findViewById(R.id.crossButton)
        val addButton: Button? = view.findViewById(R.id.addButton)
    }

    class DipperReceiver(private val holder: ViewHolder, private val event: MotionEvent) : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            holder.inputX!!.setText(event.x.toInt().toString())
            holder.inputY!!.setText(event.y.toInt().toString())
            holder.inputColor!!.setText(intent.getStringExtra("color"))
            LocalBroadcastManager.getInstance(context).unregisterReceiver(this)
        }
    }

    private val menu = container.findViewById<LinearLayout>(R.id.menu)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(viewType, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        if(position == lines.size) {
            holder.addButton!!.setOnClickListener { addDetector() }
        } else {
            val line = lines[position]
            holder.dipperButton!!.setOnClickListener { toggleDipper(holder) }
            holder.inputX!!.doAfterTextChanged { redrawX(line, holder.inputX) }
            holder.inputY!!.doAfterTextChanged { redrawY(line, holder.inputY) }
            holder.inputColor!!.doAfterTextChanged { redrawColor(line, holder.inputColor.text.toString()) }
            holder.crossButton!!.setOnClickListener { removeDetector(line, position) }

            // Set default values
            holder.inputX.setText(line.endX.toInt().toString())
            holder.inputY.setText(line.endY.toInt().toString())
            holder.inputColor.setText(line.color)
        }
    }

    override fun getItemViewType(position: Int): Int {
        return if (position == lines.size) R.layout.button_add_detector else R.layout.item_detector
    }

    override fun getItemCount(): Int {
        return lines.size + 1
    }

    private fun addDetector() {
        val line = Line(context, null)
        line.startX = clickerCenter[0]
        line.startY = clickerCenter[1]
        line.invalidate()
        container.addView(line, 0)
        lines += line
        notifyItemInserted(lines.size - 1)
    }

    private fun redrawX(line: Line, input: EditText) {
        val text = input.text.toString()
        if(text.isNotEmpty() && 0 <= text.toFloat() && text.toFloat() <= getScreenWidth()) {
            line.endX = text.toFloat()
            line.invalidate()
        }
    }

    private fun redrawY(line: Line, input: EditText) {
        val text = input.text.toString()
        if(text.isNotEmpty() && 0 <= text.toFloat() && text.toFloat() <= getScreenHeight()) {
            line.endY = text.toFloat()
            line.invalidate()
        }
    }

    private fun redrawColor(line: Line, color: String) {
        val hexRegex = Regex("^#[0-9A-Fa-f]{6}\$")
        if(color.matches(hexRegex)) {
            line.color = color
            line.invalidate()
        }
    }

    private fun removeDetector(line: Line, position: Int) {
        container.removeView(line)
        lines.removeAt(position)
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