package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "personal_events")
data class PersonalEventEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val description: String = "",
    val gregorianDate: String, // ISO format: YYYY-MM-DD
    val hijriDate: String = "",
    val time: String = "", // e.g. "14:30"
    val category: String = "Personal", // Personal, Family, Work, Religious, Reminder
    val isFavorite: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)
