package edu.ap.be.replenishmachine.model

data class Product(
    val productId: String,
    val productMachine: String,
    val maxStock: Int,
    val price: Double = 0.0,
    val hasProductPrice: Boolean = true,
    val name: String,
    val imageUrl: String,
    val ean: String,
    val sku: String,
    val categories: List<String>,
    val row: String,
    val code: Int,
    val createdAt: String,
    val stock: Int = 0,
    val stockPercentage: Int = 0,
    val weight: Int = 0
)