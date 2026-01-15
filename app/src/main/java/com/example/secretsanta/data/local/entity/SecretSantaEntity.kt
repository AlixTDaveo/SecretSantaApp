package com.example.secretsanta.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "secret_santas")
data class SecretSantaEntity(
    @PrimaryKey val id: String,
    val name: String,
    val deadline: Long,
    val participants: String, // JSON
    val creatorId: String,
    val drawDone: Boolean = false,
    val assignments: String = "{}",
    val lastSync: Long = System.currentTimeMillis(),
    val imageUrl: String? = null,
    val budget: String? = null, // NOUVEAU
    val description: String? = null // NOUVEAU
)