package edu.ap.be.replenishmachine.ui.machine.list

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import edu.ap.be.replenishmachine.R
import edu.ap.be.replenishmachine.model.Machine

class OrganizationAdapter(
    private val organizations: Map<String, List<Machine>>,
    private val onMachineSelected: (Machine) -> Unit,
    private val selectedMachine: Machine? 
) : RecyclerView.Adapter<OrganizationAdapter.ViewHolder>() {

    private val organizationNames = organizations.keys.toList()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(
            R.layout.item_organization, parent, false
        )
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val orgName = organizationNames[position]
        val machinesInOrg = organizations[orgName] ?: emptyList()

        val selectedMachineIndexInOrg = machinesInOrg.indexOf(selectedMachine)

        holder.bind(orgName, machinesInOrg, selectedMachineIndexInOrg, onMachineSelected)
    }

    override fun getItemCount(): Int = organizations.size

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val organizationHeaderText: TextView = view.findViewById(R.id.organizationHeaderText)
        private val machinesRecyclerView: RecyclerView = view.findViewById(R.id.machinesRecyclerView) 

        fun bind(
            organizationName: String,
            machines: List<Machine>,
            selectedMachineIndex: Int, 
            onMachineSelected: (Machine) -> Unit
        ) {
            organizationHeaderText.text = organizationName

            machinesRecyclerView.layoutManager = LinearLayoutManager(
                itemView.context, LinearLayoutManager.HORIZONTAL, false
            )

            val machineAdapter = MachineAdapter(machines, selectedMachineIndex, onMachineSelected)
            machinesRecyclerView.adapter = machineAdapter

            if (selectedMachineIndex != -1) {
                machinesRecyclerView.scrollToPosition(selectedMachineIndex)
            }
        }
    }
}