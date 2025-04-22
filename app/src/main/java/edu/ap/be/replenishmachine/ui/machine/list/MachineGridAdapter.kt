package edu.ap.be.replenishmachine.ui.machine.list

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView
import edu.ap.be.replenishmachine.R
import edu.ap.be.replenishmachine.model.Machine

class MachineGridAdapter(
    private val context: Context,
    private val machines: List<Machine>
) : BaseAdapter() {

    private val inflater: LayoutInflater = 
        context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

    override fun getCount(): Int = machines.size

    override fun getItem(position: Int): Machine = machines[position]

    override fun getItemId(position: Int): Long = position.toLong()

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val view: View
        val holder: ViewHolder

        if (convertView == null) {
            view = inflater.inflate(R.layout.item_machine_grid, parent, false)
            holder = ViewHolder()
            holder.machineCodeTextView = view.findViewById(R.id.machineCodeTextView)
            holder.organizationTextView = view.findViewById(R.id.organizationTextView)
            holder.machineIconImageView = view.findViewById(R.id.machineIconImageView)
            view.tag = holder
        } else {
            view = convertView
            holder = view.tag as ViewHolder
        }

        val machine = getItem(position)
        
        holder.machineCodeTextView.text = machine.code
        holder.organizationTextView.text = machine.organization
        
        return view
    }

    private class ViewHolder {
        lateinit var machineCodeTextView: TextView
        lateinit var organizationTextView: TextView
        lateinit var machineIconImageView: ImageView
    }
}