package fr.isen.dasilva.isensmartcompanion3.history

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider


class HistoryViewModelFactory(private val application: Application ): ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(HistoryViewModel::class.java)) {
            return HistoryViewModel(application) as T // Utilise l'Application pour créer HistoryViewModel
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}