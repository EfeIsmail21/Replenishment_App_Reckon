package edu.ap.be.replenishmachine.model

data class MachineProducts(
    val doors: Map<Int, Map<Int, List<Product>>>,
    val structure: String,
    val currency: Currency,
    val code: String,
)