package com.example.visionmate.room.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.visionmate.room.entity.LogEntry

@Dao
interface LogDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertLog(logEntry: LogEntry)

    @Query("SELECT * FROM logs WHERE id = :id")
    fun getById(id: Int): LogEntry

    @Query("SELECT * FROM logs")
    fun getAllLogs(): List<LogEntry>
}