package edu.ap.be.replenishmachine.ui.machine.list

// Essential Imports
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.util.Log 
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import edu.ap.be.replenishmachine.R
import edu.ap.be.replenishmachine.model.Machine
import edu.ap.be.replenishmachine.ui.machine.access.MachineAccessActivity

class MachineAdapter(
    private val machines: List<Machine>,
    private val selectedPositionInOrg: Int, 
    private val onMachineSelected: (Machine) -> Unit
) : RecyclerView.Adapter<MachineAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(
            R.layout.item_machine, parent, false
        )
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val machine = machines[position]
        val isSelected = position == selectedPositionInOrg

        holder.machineCodeTextView.text = machine.code
        holder.organizationTextView.text = machine.organization

        if (isSelected) {
            holder.selectionBorder.visibility = View.VISIBLE
            holder.statusIndicator.visibility = View.VISIBLE
            holder.selectionIndicator.visibility = View.VISIBLE
            holder.machineCardContent.setBackgroundColor(Color.parseColor("#333333"))
            holder.machineCodeTextView.setTextColor(Color.parseColor("#FFFFFF"))
            holder.organizationTextView.setTextColor(Color.parseColor("#FFFFFF"))

            try {
                val vibrator = holder.itemView.context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator?
                if (vibrator?.hasVibrator() == true) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        vibrator.vibrate(VibrationEffect.createOneShot(100, VibrationEffect.DEFAULT_AMPLITUDE))
                    } else {
                        @Suppress("DEPRECATION")
                        vibrator.vibrate(100)
                    }
                }
            } catch (e: Exception) {
                Log.e("MachineAdapter", "Vibration failed", e) 
            }

        } else {
            holder.selectionBorder.visibility = View.GONE
            holder.statusIndicator.visibility = View.GONE
            holder.selectionIndicator.visibility = View.GONE
            holder.machineCardContent.setBackgroundColor(Color.parseColor("#222222"))
            holder.machineCodeTextView.setTextColor(Color.parseColor("#DDDDDD"))
            holder.organizationTextView.setTextColor(Color.parseColor("#AAAAAA"))
        }

        holder.itemView.setOnClickListener {
            val intent = Intent(holder.itemView.context, MachineAccessActivity::class.java).apply {
                putExtra("machine_id", machine.id)
                putExtra("machine_code", machine.code)
                putExtra("machine_organization", machine.organization)
            }
            holder.itemView.context.startActivity(intent)
            onMachineSelected(machine)
        }
    }

    override fun getItemCount(): Int = machines.size

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val machineCodeTextView: TextView = view.findViewById(R.id.machineCodeTextView)
        val organizationTextView: TextView = view.findViewById(R.id.organizationTextView)
        val machineIconImageView: ImageView = view.findViewById(R.id.machineIconImageView)
        val selectionBorder: View = view.findViewById(R.id.selectionBorder)
        val statusIndicator: TextView = view.findViewById(R.id.statusIndicator)
        val selectionIndicator: ImageView = view.findViewById(R.id.selectionIndicator)
        val machineCardContent: LinearLayout = view.findViewById(R.id.machineCardContent)
    }
}