package fr.isen.dasilva.isensmartcompanion3.event

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.Column

@Composable
fun EventDetailScreen(event: Event) {
    Column(modifier = Modifier.padding(16.dp)) {
        Text(text = event.title, style = MaterialTheme.typography.headlineLarge)
        Text(text = event.date, style = MaterialTheme.typography.bodyMedium)
        Text(text = event.description, style = MaterialTheme.typography.bodySmall)
    }

}