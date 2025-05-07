package edu.ap.be.replenishmachine.model

data class Door(
    val doorNumber: Int,
    val shelves: Map<Int, List<Product>>
)