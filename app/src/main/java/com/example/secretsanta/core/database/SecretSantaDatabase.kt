package com.example.secretsanta.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.secretsanta.data.local.dao.UserDao
import com.example.secretsanta.data.local.dao.SecretSantaDao
import com.example.secretsanta.data.local.entity.UserEntity
import com.example.secretsanta.data.local.entity.SecretSantaEntity

@Database(
    entities = [UserEntity::class, SecretSantaEntity::class],
    version = 4,
    exportSchema = false
)
abstract class SecretSantaDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun secretSantaDao(): SecretSantaDao
}
