package fr.isen.dasilva.isensmartcompanion3.event

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "events")
data class EventEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val date: String,
    val description: String
)