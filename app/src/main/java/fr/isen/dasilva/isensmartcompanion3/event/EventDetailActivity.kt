package fr.isen.dasilva.isensmartcompanion3.event

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.app.NotificationCompat
import fr.isen.dasilva.isensmartcompanion3.R
import fr.isen.dasilva.isensmartcompanion3.ui.theme.Isensmartcompanion3Theme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class EventDetailActivity : ComponentActivity() {
    companion object {
        private const val TAG = "EventDetailActivity"
        private const val CHANNEL_ID = "event_reminder_channel"
        private const val NOTIFICATION_ID = 1
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        createNotificationChannel()
        enableEdgeToEdge()

        val eventId = intent.getStringExtra("eventId") ?: "ID non disponible"
        val title = intent.getStringExtra("title") ?: "Titre non disponible"
        val date = intent.getStringExtra("date") ?: "Date non disponible"
        val description = intent.getStringExtra("description") ?: "Description non disponible"

        setContent {
            Isensmartcompanion3Theme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    EventDetailScreen(
                        eventId = eventId,
                        title = title,
                        date = date,
                        description = description,
                        modifier = Modifier.padding(innerPadding),
                        onScheduleNotification = { eventTitle ->
                            scheduleNotification(eventTitle)
                        }
                    )
                }
            }
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Rappels d'événements"
            val descriptionText = "Notifications pour les rappels d'événements"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
                enableVibration(true)
                enableLights(true)
            }
            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
            Log.d(TAG, "Canal de notification créé: $CHANNEL_ID")
        }
    }

    private fun scheduleNotification(eventTitle: String) {
        Log.d(TAG, "Programmation de la notification pour: $eventTitle")
        
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        
        // Créer un Intent pour ouvrir l'activité de détail de l'événement
        val intent = Intent(this, EventDetailActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("title", eventTitle)
        }
        
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        // Construire la notification
        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("Rappel d'événement")
            .setContentText(eventTitle)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setVibrate(longArrayOf(0, 500, 200, 500))

        // Afficher la notification immédiatement pour tester
        notificationManager.notify(NOTIFICATION_ID, builder.build())
        Log.d(TAG, "Notification envoyée immédiatement")
        
        // Afficher la notification après 10 secondes
        Thread {
            try {
                Log.d(TAG, "Attente de 10 secondes avant d'afficher la notification")
                Thread.sleep(10000) // 10 secondes
                notificationManager.notify(NOTIFICATION_ID + 1, builder.build())
                Log.d(TAG, "Notification programmée affichée après 10 secondes")
            } catch (e: Exception) {
                Log.e(TAG, "Erreur lors de l'affichage de la notification programmée: ${e.message}")
            }
        }.start()
    }
}

@Composable
fun EventDetailScreen(
    eventId: String,
    modifier: Modifier = Modifier,
    title: String,
    date: String,
    description: String,
    onScheduleNotification: (String) -> Unit
) {
    var showNotificationDialog by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

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
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.primary,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(
                        onClick = { showNotificationDialog = true }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Notifications,
                            contentDescription = "Définir une notification",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
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

    if (showNotificationDialog) {
        AlertDialog(
            onDismissRequest = { showNotificationDialog = false },
            title = { Text("Définir une notification") },
            text = { Text("Une notification apparaîtra dans 10 secondes pour cet événement.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showNotificationDialog = false
                        onScheduleNotification(title)
                    }
                ) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showNotificationDialog = false }) {
                    Text("Annuler")
                }
            }
        )
    }
}
