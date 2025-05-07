package edu.ap.be.replenishmachine.ui.replen.product_info

import edu.ap.be.replenishmachine.model.Product

/**
 * Represents the physical location of a product in the machine
 */
data class ProductLocation(
    val doorNumber: Int,
    val shelfNumber: Int,
    val row: String,
    val product: Product
) {
    fun getLocationString(): String {
        return "Door $doorNumber, Shelf $shelfNumber, Row $row"
    }

    fun getStockPercentage(): Int {
        // Return the stock percentage directly from the API
        return product.stockPercentage
    }
}