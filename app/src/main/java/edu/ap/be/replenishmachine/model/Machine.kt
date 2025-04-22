package edu.ap.be.replenishmachine.model


/**
 * Model representing a machine entity
 */
data class Machine(
    val id: String,
    val organization: String,
    val code: String,
    val createdAt: String
)