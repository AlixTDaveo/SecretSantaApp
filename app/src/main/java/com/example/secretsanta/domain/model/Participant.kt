package com.example.secretsanta.domain.model

data class Participant(
    val id: String,
    val name: String,
    val email: String,
    val userId: String? = null,
    val invitationStatus: InvitationStatus = InvitationStatus.PENDING,
    val isOrganizer: Boolean = false
)
enum class InvitationStatus {
    PENDING,    // Invitation envoyée, pas encore acceptée
    ACCEPTED,   // A créé son compte / rejoint
    DECLINED    // A refusé l'invitation
}