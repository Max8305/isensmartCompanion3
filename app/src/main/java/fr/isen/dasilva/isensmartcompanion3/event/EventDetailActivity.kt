package fr.isen.dasilva.isensmartcompanion3.event

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import fr.isen.dasilva.isensmartcompanion3.ui.theme.Isensmartcompanion3Theme


class EventDetailActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Récupération des données envoyées par l'Intent
        val eventId = intent.getStringExtra("eventId") ?: "ID non disponible"
        val title = intent.getStringExtra("title") ?: "Titre non disponible"
        val date = intent.getStringExtra("date") ?: "Date non disponible"
        val description = intent.getStringExtra("description") ?: "Description non disponible"

        setContent {
            Isensmartcompanion3Theme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    EventDetailScreen(eventId = eventId, title = title, date = date, description = description, modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }
}

@Composable
fun EventDetailScreen(
    eventId: String,
    modifier: Modifier = Modifier,
    title: String,
    date: String,
    description: String
) {

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Top
    ) {
        Card(
            modifier = modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            shape = MaterialTheme.shapes.medium
        ) {
            Column(modifier = modifier.padding(8.dp)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.primary,
                    textAlign = TextAlign.Center,
                )
                Spacer(modifier = modifier.height(1.dp))
                Text(
                    text = date,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = modifier.height(1.dp))
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}
