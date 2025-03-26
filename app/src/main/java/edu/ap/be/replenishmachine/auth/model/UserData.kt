package edu.ap.be.replenishmachine.auth.model

data class UserData(
    val userId: String,
    val username: String,
    val email: String,
    val locale: String,
    val createdBy: CreatedBy,
    val active: Boolean,
    val createdAt: String,
    val updatedAt: String,
    val toDelete: Boolean,
    val currency: String,
    val entityId: String,
    val entityName: String,
    val organizationId: String,
    val organizationName: String,
    val role: UserRole
)