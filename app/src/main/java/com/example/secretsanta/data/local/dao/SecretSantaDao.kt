package com.example.secretsanta.data.local.dao

import androidx.room.*
import com.example.secretsanta.data.local.entity.SecretSantaEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SecretSantaDao {
    @Query("SELECT * FROM secret_santas WHERE creatorId = :userId")
    fun getSecretSantasByUser(userId: String): Flow<List<SecretSantaEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSecretSanta(secretSanta: SecretSantaEntity)

    @Query("DELETE FROM secret_santas")
    suspend fun clearAll()
}