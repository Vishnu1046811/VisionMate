package com.example.visionmate.room.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "logs")
data class LogEntry(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val text: String,  // Actual log content
    val tokenized: String // Store the FAISS vector as a JSON string
)