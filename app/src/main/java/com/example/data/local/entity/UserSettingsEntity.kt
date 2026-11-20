package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_settings")
data class UserSettingsEntity(
    @PrimaryKey
    val id: Int = 1,
    val selectedCountryCode: String = "SA",
    val hijriOffset: Int = 0, // -2 to +2 days
    val languageCode: String = "en", // "en" or "ar"
    val firstDayOfWeek: Int = 6, // 6 = Saturday, 7 = Sunday, 1 = Monday
    val darkMode: String = "system", // "light", "dark", "system"
    val holidayReminderEnabled: Boolean = false
)
