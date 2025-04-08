package fr.isen.dasilva.isensmartcompanion3.history

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class HistoryViewModel(application: Application) : AndroidViewModel(application) {
    private val dao = MessageDatabase.getDatabase(application).messageDao()

    private val _messages = MutableStateFlow<List<Message>>(emptyList())
    val messages: StateFlow<List<Message>> = _messages

    init {
        viewModelScope.launch {
            _messages.value = dao.getAllMessages()
        }
    }

    fun addHistory(question: String, answer: String) {
        val newMessage = Message(
            question = question,
            response = answer,
            timestamp = System.currentTimeMillis()
        )
        viewModelScope.launch {
            dao.insertMessage(newMessage) // Insère le message dans la base de données
            _messages.value = dao.getAllMessages() // Mets à jour la liste des messages
        }
    }

    fun deleteMessage(message: Message) {
        viewModelScope.launch {
            dao.deleteMessage(message) // Supprime le message
            _messages.value = dao.getAllMessages() // Mets à jour la liste des messages
        }
    }

    fun clearAll() {
        viewModelScope.launch {
            dao.clearAll() // Supprime tous les messages
            _messages.value = emptyList() // Mets à jour la liste des messages
        }
    }
}
