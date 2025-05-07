package edu.ap.be.replenishmachine.model

data class Currency(
    val id: String,
    val name: String,
    val symbol: String,
    val isoName: String,
    val createdAt: String,
    val updatedAt: String
)