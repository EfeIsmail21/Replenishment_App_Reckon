package edu.ap.be.replenishmachine.model

import java.util.Date

data class Organization(
    val id: String,
    val authId: String,
    val label: String,
    val products: Int,
    val machines: Int,
    val createdAt: Date,
    val active: Boolean
)