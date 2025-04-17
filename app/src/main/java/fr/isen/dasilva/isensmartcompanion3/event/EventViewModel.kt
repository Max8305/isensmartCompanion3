package fr.isen.dasilva.isensmartcompanion3.event

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import fr.isen.dasilva.isensmartcompanion3.history.MessageDatabase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class EventViewModel(application: Application) : AndroidViewModel(application) {
    private val TAG = "EventViewModel"
    private val database = MessageDatabase.getDatabase(application)
    private val eventDao = database.eventDao()

    private val _events = MutableStateFlow<List<EventEntity>>(emptyList())
    val events: StateFlow<List<EventEntity>> = _events

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    init {
        loadEvents()
    }

    private fun loadEvents() {
        viewModelScope.launch {
            try {
                val loadedEvents = eventDao.getAllEvents()
                Log.d(TAG, "Événements chargés depuis la base de données: ${loadedEvents.size}")
                _events.value = loadedEvents
            } catch (e: Exception) {
                Log.e(TAG, "Erreur lors du chargement des événements: ${e.message}")
            }
        }
    }

    fun addEvent(event: EventEntity) {
        viewModelScope.launch {
            try {
                // Vérifier si l'événement existe déjà
                val existingEvents = eventDao.getEventsByTitleAndDate(event.title, event.date)
                if (existingEvents.isEmpty()) {
                    eventDao.insertEvent(event)
                    Log.d(TAG, "Nouvel événement ajouté: ${event.title}")
                } else {
                    Log.d(TAG, "Événement déjà existant, ignoré: ${event.title}")
                }
                loadEvents() // Recharger la liste des événements
            } catch (e: Exception) {
                Log.e(TAG, "Erreur lors de l'ajout de l'événement: ${e.message}")
            }
        }
    }

    fun fetchAndSaveEvents() {
        _isLoading.value = true
        Log.d(TAG, "Début de la récupération des événements depuis l'API")
        
        RetrofitClient.instance.getEvents().enqueue(object : Callback<List<Event>> {
            override fun onResponse(call: Call<List<Event>>, response: Response<List<Event>>) {
                Log.d(TAG, "Réponse reçue de l'API: ${response.code()}")
                
                if (response.isSuccessful) {
                    val events = response.body()
                    Log.d(TAG, "Nombre d'événements reçus: ${events?.size ?: 0}")
                    
                    if (events.isNullOrEmpty()) {
                        Log.w(TAG, "Aucun événement reçu de l'API")
                        _isLoading.value = false
                        return
                    }
                    
                    viewModelScope.launch {
                        try {
                            // Convertir les Event en EventEntity
                            val eventEntities = events.map { event ->
                                EventEntity(
                                    title = event.title,
                                    date = event.date,
                                    description = event.description
                                )
                            }
                            
                            // Sauvegarder dans la base de données en évitant les doublons
                            var newEventsCount = 0
                            eventEntities.forEach { eventEntity ->
                                // Vérifier si l'événement existe déjà
                                val existingEvents = eventDao.getEventsByTitleAndDate(eventEntity.title, eventEntity.date)
                                if (existingEvents.isEmpty()) {
                                    eventDao.insertEvent(eventEntity)
                                    newEventsCount++
                                }
                            }
                            
                            Log.d(TAG, "Nouveaux événements sauvegardés dans la base de données: $newEventsCount")
                            
                            // Recharger les événements depuis la base de données
                            loadEvents()
                        } catch (e: Exception) {
                            Log.e(TAG, "Erreur lors de la sauvegarde des événements: ${e.message}")
                        } finally {
                            _isLoading.value = false
                        }
                    }
                } else {
                    Log.e(TAG, "Erreur de réponse de l'API: ${response.code()}")
                    _isLoading.value = false
                }
            }

            override fun onFailure(call: Call<List<Event>>, t: Throwable) {
                Log.e(TAG, "Erreur lors de l'appel API: ${t.message}")
                _isLoading.value = false
            }
        })
    }

    fun getEventsByDate(date: String) {
        viewModelScope.launch {
            try {
                val eventsForDate = eventDao.getEventsByDate(date)
                Log.d(TAG, "Événements trouvés pour la date $date: ${eventsForDate.size}")
                _events.value = eventsForDate
            } catch (e: Exception) {
                Log.e(TAG, "Erreur lors de la récupération des événements par date: ${e.message}")
            }
        }
    }
} 