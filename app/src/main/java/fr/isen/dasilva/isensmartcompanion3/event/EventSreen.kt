package fr.isen.dasilva.isensmartcompanion3.event

import android.content.Intent
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.lazy.items
import androidx.compose.ui.Alignment
import androidx.lifecycle.viewmodel.compose.viewModel
import fr.isen.dasilva.isensmartcompanion3.event.EventViewModel
import fr.isen.dasilva.isensmartcompanion3.event.EventViewModelFactory

@Composable
fun EventScreen() {
    val TAG = "EventScreen"
    val context = LocalContext.current
    val viewModel: EventViewModel = viewModel(factory = EventViewModelFactory(context.applicationContext as android.app.Application))
    val events by viewModel.events.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    LaunchedEffect(Unit) {
        Log.d(TAG, "EventScreen: Lancement de fetchAndSaveEvents")
        viewModel.fetchAndSaveEvents()
    }

    if (isLoading) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
    } else {
        Log.d(TAG, "EventScreen: Affichage de ${events.size} événements")
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(events) { eventEntity ->
                EventItem(
                    event = eventEntity,
                    onClick = {
                        val intent = Intent(context, EventDetailActivity::class.java).apply {
                            putExtra("eventId", eventEntity.id.toString())
                            putExtra("title", eventEntity.title)
                            putExtra("date", eventEntity.date)
                            putExtra("description", eventEntity.description)
                        }
                        context.startActivity(intent)
                    }
                )
            }
        }
    }
}

@Composable
fun EventItem(event: EventEntity, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = event.title,
                style = MaterialTheme.typography.titleLarge
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = event.date,
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = event.description,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}
