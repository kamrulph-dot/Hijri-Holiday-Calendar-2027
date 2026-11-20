package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.local.entity.UserSettingsEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UserSettingsDao {

    @Query("SELECT * FROM user_settings WHERE id = 1 LIMIT 1")
    fun getSettings(): Flow<UserSettingsEntity?>

    @Query("SELECT * FROM user_settings WHERE id = 1 LIMIT 1")
    suspend fun getSettingsOnce(): UserSettingsEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveSettings(settings: UserSettingsEntity)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertDefaultIfMissing(settings: UserSettingsEntity)

    @Query("UPDATE user_settings SET selectedCountryCode = :countryCode WHERE id = 1")
    suspend fun updateCountry(countryCode: String)

    @Query("UPDATE user_settings SET hijriOffset = :offset WHERE id = 1")
    suspend fun updateHijriOffset(offset: Int)

    @Query("UPDATE user_settings SET languageCode = :lang WHERE id = 1")
    suspend fun updateLanguage(lang: String)

    @Query("UPDATE user_settings SET firstDayOfWeek = :firstDay WHERE id = 1")
    suspend fun updateFirstDayOfWeek(firstDay: Int)

    @Query("UPDATE user_settings SET darkMode = :mode WHERE id = 1")
    suspend fun updateDarkMode(mode: String)

    @Query("UPDATE user_settings SET holidayReminderEnabled = :enabled WHERE id = 1")
    suspend fun updateHolidayReminderEnabled(enabled: Boolean)
}
