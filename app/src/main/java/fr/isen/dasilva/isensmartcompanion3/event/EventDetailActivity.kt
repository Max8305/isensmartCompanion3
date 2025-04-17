package fr.isen.dasilva.isensmartcompanion3.event

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
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

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            Log.d(TAG, "Permission de notification accordée")
            Toast.makeText(this, "Permission de notification accordée", Toast.LENGTH_SHORT).show()
        } else {
            Log.d(TAG, "Permission de notification refusée")
            Toast.makeText(this, "Les notifications ne fonctionneront pas sans permission", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "onCreate: Démarrage de l'activité")
        createNotificationChannel()
        checkNotificationPermission()
        enableEdgeToEdge()

        val eventId = intent.getStringExtra("eventId") ?: "ID non disponible"
        val title = intent.getStringExtra("title") ?: "Titre non disponible"
        val date = intent.getStringExtra("date") ?: "Date non disponible"
        val description = intent.getStringExtra("description") ?: "Description non disponible"

        Log.d(TAG, "onCreate: Titre de l'événement: $title")

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
                            Log.d(TAG, "onScheduleNotification appelé avec: $eventTitle")
                            showNotification(eventTitle)
                        }
                    )
                }
            }
        }
    }

    private fun checkNotificationPermission() {
        Log.d(TAG, "checkNotificationPermission: Vérification des permissions")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            when {
                ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED -> {
                    Log.d(TAG, "Permission de notification déjà accordée")
                }
                else -> {
                    Log.d(TAG, "Demande de permission de notification")
                    requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            }
        } else {
            Log.d(TAG, "Version Android < 13, pas besoin de permission explicite")
        }
    }

    private fun createNotificationChannel() {
        Log.d(TAG, "createNotificationChannel: Création du canal de notification")
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
        } else {
            Log.d(TAG, "Version Android < 8.0, pas besoin de canal de notification")
        }
    }

    private fun showNotification(eventTitle: String) {
        Log.d(TAG, "showNotification: Programmation de la notification pour: $eventTitle")
        
        try {
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

            Toast.makeText(this, "Notification programmée pour dans 10 secondes", Toast.LENGTH_SHORT).show()
            
            // Afficher la notification après 10 secondes
            Thread {
                try {
                    Log.d(TAG, "Attente de 10 secondes avant d'afficher la notification")
                    Thread.sleep(10000) // 10 secondes
                    notificationManager.notify(NOTIFICATION_ID, builder.build())
                    Log.d(TAG, "Notification affichée après 10 secondes")
                } catch (e: Exception) {
                    Log.e(TAG, "Erreur lors de l'affichage de la notification: ${e.message}")
                    e.printStackTrace()
                }
            }.start()
        } catch (e: Exception) {
            Log.e(TAG, "Erreur lors de la programmation de la notification: ${e.message}")
            e.printStackTrace()
            Toast.makeText(this, "Erreur lors de la programmation de la notification: ${e.message}", Toast.LENGTH_SHORT).show()
        }
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
    var notificationScheduled by remember { mutableStateOf(false) }
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
                        onClick = { 
                            Log.d("EventDetailScreen", "Clic sur l'icône de notification")
                            showNotificationDialog = true 
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Notifications,
                            contentDescription = "Définir une notification",
                            tint = if (notificationScheduled) Color.Green else MaterialTheme.colorScheme.primary
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
                        Log.d("EventDetailScreen", "Confirmation de la notification")
                        showNotificationDialog = false
                        onScheduleNotification(title)
                        notificationScheduled = true
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
