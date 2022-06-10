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

/**
 * Manages a list of detector views with a [context] object. Each item in this adapter corresponds
 * to a line in [lines], as well as its row in the detector menu. This will also add new detectors
 * to the desired [container] and set their starting positions to the parent clicker's
 * [center][clickerCenter].
 */
class DetectorAdapter(
    private val context: Context,
    private val lines: MutableList<Line>,
    private val container: ViewGroup,
    private val clickerCenter: Array<Float>
) : RecyclerView.Adapter<DetectorAdapter.ViewHolder>() {

    /** Holds the views for each individual detector item. */
    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val dipperButton: ImageButton? = view.findViewById(R.id.dipperButton)
        val inputX: EditText? = view.findViewById(R.id.inputX)
        val inputY: EditText? = view.findViewById(R.id.inputY)
        val inputColor: EditText? = view.findViewById(R.id.inputColor)
        val crossButton: ImageButton? = view.findViewById(R.id.crossButton)
        val addButton: Button? = view.findViewById(R.id.addButton)
    }

    /**
     * This receives the color sent by the [AutoClickService]. Then, this will paste the color and
     * the [event]'s coordinates into the [holder]'s inputs. This also cleans up by unregistering
     * itself and showing the menu (possibly hidden by [toggleDipper]).
     */
    inner class DipperReceiver(
        private val holder: ViewHolder,
        private val event: MotionEvent
    ) : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            holder.inputX!!.setText(event.x.toInt().toString())
            holder.inputY!!.setText(event.y.toInt().toString())
            holder.inputColor!!.setText(intent.getStringExtra("color"))
            menu.visibility = View.VISIBLE
            LocalBroadcastManager.getInstance(context).unregisterReceiver(this)
        }
    }

    private val menu = container.findViewById<LinearLayout>(R.id.menu)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(viewType, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        // Display the "add" button if this is the last item
        if(position == lines.size) {
            holder.addButton!!.setOnClickListener { addDetector() }
        // Otherwise, display a detector
        } else {
            val line = lines[position]
            holder.dipperButton!!.setOnClickListener { toggleDipper(holder) }
            // Re-render detector lines when an input changes
            holder.inputX!!.doAfterTextChanged { redrawX(line, holder.inputX) }
            holder.inputY!!.doAfterTextChanged { redrawY(line, holder.inputY) }
            holder.inputColor!!.doAfterTextChanged { redrawColor(line, holder.inputColor.text.toString()) }
            holder.crossButton!!.setOnClickListener { removeDetector(line, position) }

            // Set default input values
            holder.inputX.setText(line.endX.toInt().toString())
            holder.inputY.setText(line.endY.toInt().toString())
            holder.inputColor.setText(line.color)
        }
    }

    override fun getItemViewType(position: Int): Int {
        // Return an "add" button if this is the last view. Otherwise, return a detector
        return if (position == lines.size) R.layout.button_add_detector else R.layout.item_detector
    }

    override fun getItemCount(): Int {
        // Return number of detector lines + 1, to account for the "add" button
        return lines.size + 1
    }

    /** Adds a detector line to [lines] and displays it as a child to the [container]. */
    private fun addDetector() {
        val line = Line(context, null)
        line.startX = clickerCenter[0]
        line.startY = clickerCenter[1]
        // Render the line
        line.invalidate()
        container.addView(line, 0)
        lines += line
        notifyItemInserted(lines.size - 1)
    }

    /**
     * Sets the [line]'s x-position to the [input] value if it is not empty and is within screen
     * bounds.
     */
    private fun redrawX(line: Line, input: EditText) {
        val text = input.text.toString()
        // Checks if input is not empty and is within screen bounds
        if(text.isNotEmpty() && 0 <= text.toFloat() && text.toFloat() <= getScreenWidth()) {
            line.endX = text.toFloat()
            line.invalidate()
        }
    }

    /**
     * Sets the [line]'s y-position to the [input] value if it is not empty and is within screen
     * bounds.
     */
    private fun redrawY(line: Line, input: EditText) {
        val text = input.text.toString()
        // Checks if input is not empty and is within screen bounds
        if(text.isNotEmpty() && 0 <= text.toFloat() && text.toFloat() <= getScreenHeight()) {
            line.endY = text.toFloat()
            line.invalidate()
        }
    }

    /** Sets the [line]'s color to the provided [color] if the color is in the format #RRGGBB */
    private fun redrawColor(line: Line, color: String) {
        val hexRegex = Regex("^#[0-9A-Fa-f]{6}\$")
        if(color.matches(hexRegex)) {
            line.color = color
            line.invalidate()
        }
    }

    /** Removes the detector [line] and notifies the adapter that the item at its [position] changed */
    private fun removeDetector(line: Line, position: Int) {
        container.removeView(line)
        lines.removeAt(position)
        notifyItemRemoved(position)
        notifyItemRangeChanged(position, itemCount)
    }

    /** Hides the [menu] and calls [dip] on the user's next click. */
    @SuppressLint("ClickableViewAccessibility")
    private fun toggleDipper(holder: ViewHolder) {
        // Check if the auto-clicker was initialized with a projection
        if(AutoClickService.instance?.projection != null) {
            // Hide the menu
            menu.visibility = View.INVISIBLE
            // On click (cursor up), dip the selected pixel and remove this listener
            container.setOnTouchListener { _, event ->
                // Returns whether the touch event was handled
                if(event.action == MotionEvent.ACTION_UP) {
                    dip(holder, event)
                    // Unregister this listener after the next click
                    container.setOnTouchListener(null)
                    true
                } else {
                    false
                }
            }
        }
    }

    /**
     * Asks the auto-clicker to detect and broadcast the pixel color at the [event]'s position.
     * This also sets up a receiver to accept and handle the broadcasted color.
     * */
    private fun dip(holder: ViewHolder, event: MotionEvent) {
        // Make a DipperReceiver to handle the color broadcasted by the AutoCLickService
        LocalBroadcastManager.getInstance(context).registerReceiver(
            DipperReceiver(holder, event),
            IntentFilter("receive_pixel_color")
        )
        val intent = Intent(context, AutoClickService::class.java)
        intent.action = "send_pixel_color"
        // Supply coordinates with origins below the status bar
        intent.putExtra("x", event.x.toInt())
        intent.putExtra("y", event.y.toInt())
        // Launch the auto-clicker
        context.startService(intent)
    }
}