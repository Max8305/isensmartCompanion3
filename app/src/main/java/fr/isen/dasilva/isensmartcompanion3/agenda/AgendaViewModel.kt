package fr.isen.dasilva.isensmartcompanion3.agenda

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

data class Event(val date: String, val title: String)

class AgendaViewModel(application: Application) : AndroidViewModel(application) {
    private val _events = MutableStateFlow<List<Event>>(emptyList())
    val events: StateFlow<List<Event>> = _events
}