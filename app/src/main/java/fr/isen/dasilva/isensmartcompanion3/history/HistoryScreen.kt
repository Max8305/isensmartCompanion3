package fr.isen.dasilva.isensmartcompanion3.history

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun HistoryScreen(viewModel: HistoryViewModel) {
    // Collecte les messages depuis le ViewModel
    val messages = viewModel.messages.collectAsState().value

    Column(modifier = Modifier.padding(16.dp)) {
        Text("Historique des questions et réponses", style = MaterialTheme.typography.headlineMedium)

        // Affiche chaque message avec la question, la réponse et le timestamp
        messages.forEach { message ->
            Column(modifier = Modifier.padding(vertical = 8.dp)) {
                Text(
                    text = "Question: ${message.question}",
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
                Text(
                    text = "Réponse: ${message.response}",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
                Text(
                    text = "Timestamp: ${formatTimestamp(message.timestamp)}",
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }
        }
    }
}

// Fonction utilitaire pour formater le timestamp
fun formatTimestamp(timestamp: Long): String {
    val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault())
    return dateFormat.format(Date(timestamp))
}

