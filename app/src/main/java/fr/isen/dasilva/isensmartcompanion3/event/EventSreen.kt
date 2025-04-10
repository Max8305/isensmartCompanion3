package fr.isen.dasilva.isensmartcompanion3.event

import android.content.Intent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.foundation.lazy.items

@Composable
fun EventScreen() {

    var events by remember { mutableStateOf<List<Event>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    val context = LocalContext.current

    // Charger les événements au démarrage
    LaunchedEffect(Unit) {
        RetrofitClient.instance.getEvents().enqueue(object : Callback<List<Event>> {
            override fun onResponse(call: Call<List<Event>>, response: Response<List<Event>>) {
                if (response.isSuccessful) {
                    events = response.body() ?: emptyList()
                }
                isLoading = false
            }

            override fun onFailure(call: Call<List<Event>>, t: Throwable) {
                isLoading = false
            }
        })
    }

    // Affichage de l'indicateur de chargement ou des événements
    if (isLoading) {
        CircularProgressIndicator()
    } else {
        LazyColumn {
            items(events) { event ->
                EventItem(event = event, onClick = {
                    val intent = Intent(context, EventDetailActivity::class.java).apply {
                        putExtra("title", event.title)
                        putExtra("date", event.date)
                        putExtra("description", event.description)
                    }
                    context.startActivity(intent)
                })
            }
        }
    }
}

@Composable
fun EventItem(event: Event, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = event.title, style = MaterialTheme.typography.headlineMedium)
            Text(text = event.date, style = MaterialTheme.typography.bodyMedium)
        }
    }
}
