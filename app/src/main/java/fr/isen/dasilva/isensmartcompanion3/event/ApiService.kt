package fr.isen.dasilva.isensmartcompanion3.event

import retrofit2.http.GET
import retrofit2.Call

interface ApiService {
    @GET("events.json")
    fun getEvents(): Call<List<Event>>
}