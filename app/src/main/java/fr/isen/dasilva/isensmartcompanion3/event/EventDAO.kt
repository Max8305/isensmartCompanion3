package fr.isen.dasilva.isensmartcompanion3.event

import androidx.room.*

@Dao
interface EventDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEvent(event: EventEntity)

    @Query("SELECT * FROM events")
    suspend fun getAllEvents(): List<EventEntity>

    @Query("SELECT * FROM events WHERE date = :date")
    suspend fun getEventsByDate(date: String): List<EventEntity>

    @Query("SELECT * FROM events WHERE title = :title AND date = :date")
    suspend fun getEventsByTitleAndDate(title: String, date: String): List<EventEntity>

    @Delete
    suspend fun deleteEvent(event: EventEntity)
}