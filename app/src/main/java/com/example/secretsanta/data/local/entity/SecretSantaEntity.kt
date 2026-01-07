package com.example.secretsanta.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "secret_santas")
data class SecretSantaEntity(
    @PrimaryKey val id: String,
    val name: String,
    val deadline: Long,
    val participants: String, // JSON string
    val creatorId: String,
    val lastSync: Long = System.currentTimeMillis()
)