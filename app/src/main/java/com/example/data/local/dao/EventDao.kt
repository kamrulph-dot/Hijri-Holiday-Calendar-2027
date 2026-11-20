package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.local.entity.PersonalEventEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface EventDao {

    @Query("SELECT * FROM personal_events ORDER BY gregorianDate ASC, time ASC")
    fun getAllEvents(): Flow<List<PersonalEventEntity>>

    @Query("SELECT * FROM personal_events WHERE gregorianDate = :dateStr ORDER BY time ASC")
    fun getEventsForDate(dateStr: String): Flow<List<PersonalEventEntity>>

    @Query("SELECT * FROM personal_events WHERE isFavorite = 1 ORDER BY gregorianDate ASC")
    fun getFavoriteEvents(): Flow<List<PersonalEventEntity>>

    @Query("SELECT * FROM personal_events WHERE title LIKE '%' || :query || '%' OR description LIKE '%' || :query || '%'")
    fun searchEvents(query: String): Flow<List<PersonalEventEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEvent(event: PersonalEventEntity): Long

    @Update
    suspend fun updateEvent(event: PersonalEventEntity)

    @Delete
    suspend fun deleteEvent(event: PersonalEventEntity)

    @Query("DELETE FROM personal_events WHERE id = :id")
    suspend fun deleteEventById(id: Long)
}
