package fr.isen.dasilva.isensmartcompanion3.event

import retrofit2.http.GET
import retrofit2.Call
import retrofit2.http.Query

interface ApiService {
    @GET("events.json")
    fun getEvents(): Call<List<Event>>

    // Cette méthode permet de récupérer des événements filtrés par date
    @GET("events.json")
    fun getEventsForDate(@Query("date") date: String): Call<List<Event>>
}