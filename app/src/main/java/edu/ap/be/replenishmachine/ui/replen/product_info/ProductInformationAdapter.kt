package edu.ap.be.replenishmachine.ui.replen.product_info

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import edu.ap.be.replenishmachine.R

/**
 * Adapter for displaying product information optimized for Vuzix Blade 2 (480x480 screen)
 */
class ProductInformationAdapter(
    private val context: Context,
    private val productLocations: List<ProductLocation>
) : RecyclerView.Adapter<ProductInformationAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.replen_on_product, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val productLocation = productLocations[position]
        val product = productLocation.product

        // Set location information
        holder.tvLocation.text = productLocation.getLocationString()
        
        // Set product name and EAN
        holder.tvProductName.text = product.name
        holder.tvProductEan.text = "EAN: ${product.ean}"
        
        // Set stock information
        val stockText = "${product.weight}/${product.maxStock}"
        holder.tvStockInfo.text = stockText
        
        // Set percentage
        val percentage = productLocation.getStockPercentage()
        holder.progressBar.progress = percentage
        holder.tvPercentage.text = "$percentage%"

    }

    override fun getItemCount(): Int = productLocations.size


    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvLocation: TextView = itemView.findViewById(R.id.tv_location)
        val tvProductName: TextView = itemView.findViewById(R.id.tv_product_name)
        val tvProductEan: TextView = itemView.findViewById(R.id.tv_product_ean)
        val tvStockInfo: TextView = itemView.findViewById(R.id.tv_stock_info)
        val tvPercentage: TextView = itemView.findViewById(R.id.tv_percentage)
        val progressBar: ProgressBar = itemView.findViewById(R.id.progress_bar)
        val ivProductImage: ImageView = itemView.findViewById(R.id.iv_product_image)
    }

}
