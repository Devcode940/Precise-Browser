package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "bookmarks")
data class BookmarkEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val url: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "history_items")
data class HistoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val url: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "passwords")
data class PasswordEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val site: String,
    val url: String,
    val username: String,
    val encryptedPass: String,
    val notes: String = "",
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "downloads")
data class DownloadEntity(
    @PrimaryKey val id: String, // dl1, dl2, etc.
    val url: String,
    val filename: String,
    val status: String, // downloading, paused, completed, failed, cancelled
    val receivedBytes: Long,
    val totalSize: Long,
    val downloadSpeed: Double, // bytes per second
    val etaSeconds: Long,
    val progressPercent: Float,
    val timestamp: Long = System.currentTimeMillis(),
    val isTurbo: Boolean = false
)

@Entity(tableName = "user_settings")
data class UserSettingEntity(
    @PrimaryKey val key: String,
    val value: String
)
