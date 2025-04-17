package fr.isen.dasilva.isensmartcompanion3.agenda

import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import fr.isen.dasilva.isensmartcompanion3.event.EventEntity
import fr.isen.dasilva.isensmartcompanion3.event.EventViewModel
import fr.isen.dasilva.isensmartcompanion3.event.EventViewModelFactory
import fr.isen.dasilva.isensmartcompanion3.event.EventDetailActivity
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.TextStyle
import java.time.format.DateTimeFormatter
import java.util.*

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AgendaScreen() {
    val TAG = "AgendaScreen"
    val pagerState = rememberPagerState(
        initialPage = 120,
        pageCount = { 240 }
    )
    val context = LocalContext.current
    val viewModel: EventViewModel = viewModel(factory = EventViewModelFactory(context.applicationContext as android.app.Application))
    val events by viewModel.events.collectAsState()
    val currentPage = pagerState.currentPage
    val yearMonth = remember(currentPage) {
        YearMonth.now().plusMonths((currentPage - 120).toLong())
    }
    var showAddEventDialog by remember { mutableStateOf(false) }
    var selectedDateForEvent by remember { mutableStateOf<LocalDate?>(null) }

    // Afficher les événements chargés dans les logs
    LaunchedEffect(events) {
        Log.d(TAG, "Nombre total d'événements chargés: ${events.size}")
        events.forEach { event ->
            Log.d(TAG, "Événement: ${event.title}, Date: ${event.date}")
        }
    }

    LaunchedEffect(Unit) {
        Log.d(TAG, "Chargement des événements depuis l'API")
        viewModel.fetchAndSaveEvents()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // En-tête avec l'année
        Text(
            text = yearMonth.year.toString(),
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )

        // Calendrier dans une box colorée
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            HorizontalPager(
                state = pagerState
            ) { pageOffset ->
                val offsetMonth = YearMonth.now().plusMonths((pageOffset - 120).toLong())
                CalendarMonthView(
                    yearMonth = offsetMonth, 
                    context = context, 
                    events = events,
                    onDateSelected = { date ->
                        selectedDateForEvent = date
                        showAddEventDialog = true
                    }
                )
            }
        }
    }

    if (showAddEventDialog) {
        AddEventDialog(
            onDismiss = { showAddEventDialog = false },
            onEventAdded = { title, description, date ->
                val newEvent = EventEntity(
                    title = title,
                    date = date,
                    description = description
                )
                viewModel.addEvent(newEvent)
                showAddEventDialog = false
                Toast.makeText(context, "Événement ajouté avec succès", Toast.LENGTH_SHORT).show()
            },
            initialDate = selectedDateForEvent ?: LocalDate.now()
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEventDialog(
    onDismiss: () -> Unit,
    onEventAdded: (String, String, String) -> Unit,
    initialDate: LocalDate
) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var selectedDate by remember { mutableStateOf(initialDate) }
    var showDatePicker by remember { mutableStateOf(false) }
    
    // Formatter pour afficher la date dans le format "24 septembre 2024"
    val displayFormatter = DateTimeFormatter.ofPattern("d MMMM yyyy", Locale.FRENCH)
    
    // Créer un état pour le DatePicker
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = selectedDate.atStartOfDay(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli()
    )

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { millis ->
                            // Convertir les millisecondes en LocalDate
                            val instant = java.time.Instant.ofEpochMilli(millis)
                            val zonedDateTime = java.time.ZonedDateTime.ofInstant(instant, java.time.ZoneId.systemDefault())
                            selectedDate = zonedDateTime.toLocalDate()
                        }
                        showDatePicker = false
                    }
                ) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Annuler")
                }
            }
        ) {
            DatePicker(
                state = datePickerState,
                showModeToggle = false
            )
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Ajouter un événement") },
        text = {
            Column {
                TextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Titre") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                TextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedButton(
                    onClick = { showDatePicker = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Date: ${selectedDate.format(displayFormatter)}")
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (title.isNotBlank()) {
                        // Utiliser le format "24 septembre 2024" pour la date
                        onEventAdded(title, description, selectedDate.format(displayFormatter))
                    }
                }
            ) {
                Text("Ajouter")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Annuler")
            }
        }
    )
}

@Composable
fun CalendarMonthView(
    yearMonth: YearMonth, 
    context: Context, 
    events: List<EventEntity>,
    onDateSelected: (LocalDate) -> Unit
) {
    val TAG = "CalendarMonthView"
    val daysInMonth = yearMonth.lengthOfMonth()
    val firstDayOfWeek = yearMonth.atDay(1).dayOfWeek.value % 7
    var selectedDate by remember { mutableStateOf<LocalDate?>(null) }
    
    // Formatter pour afficher la date dans le format "24 septembre 2024"
    val displayFormatter = DateTimeFormatter.ofPattern("d MMMM yyyy", Locale.FRENCH)
    
    // Filtrer les événements pour la date sélectionnée
    val filteredEvents = remember(selectedDate, events) {
        if (selectedDate != null) {
            val formattedSelectedDate = selectedDate!!.format(displayFormatter)
            Log.d(TAG, "Recherche d'événements pour la date: $formattedSelectedDate")
            val filtered = events.filter { 
                val eventDate = it.date
                Log.d(TAG, "Comparaison: $eventDate == $formattedSelectedDate")
                eventDate == formattedSelectedDate 
            }
            Log.d(TAG, "Événements trouvés: ${filtered.size}")
            filtered
        } else {
            emptyList()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Text(
            text = yearMonth.month.getDisplayName(TextStyle.FULL, Locale.FRENCH),
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            listOf("Dim", "Lun", "Mar", "Mer", "Jeu", "Ven", "Sam").forEach { day ->
                Text(
                    text = day,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.weight(1f),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        LazyVerticalGrid(
            columns = GridCells.Fixed(7),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(firstDayOfWeek) {
                Box(modifier = Modifier.aspectRatio(1f))
            }

            items(daysInMonth) { day ->
                // Correction: day commence à 0, donc on ajoute 1 pour commencer à 1
                val dayNumber = day + 1
                val date = yearMonth.atDay(dayNumber)
                val isSelected = date == selectedDate
                val formattedDate = date.format(displayFormatter)
                
                // Vérifier si des événements existent pour cette date
                val hasEvents = events.any { 
                    val eventDate = it.date
                    Log.d(TAG, "Vérification événement: $eventDate pour date: $formattedDate")
                    eventDate == formattedDate 
                }
                
                Box(
                    modifier = Modifier
                        .aspectRatio(1f)
                        .padding(2.dp)
                        .clickable { 
                            selectedDate = date
                        }
                        .background(
                            when {
                                isSelected -> MaterialTheme.colorScheme.primary
                                else -> Color.Transparent
                            }
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Box {
                        Text(
                            text = dayNumber.toString(),
                            color = when {
                                isSelected -> MaterialTheme.colorScheme.onPrimary
                                else -> MaterialTheme.colorScheme.onSurface
                            }
                        )
                        if (hasEvents) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .background(
                                        color = MaterialTheme.colorScheme.primary,
                                        shape = MaterialTheme.shapes.small
                                    )
                                    .align(Alignment.BottomEnd)
                            )
                        }
                    }
                }
            }
        }

        if (selectedDate != null) {
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Événements pour le ${selectedDate!!.dayOfMonth}",
                    style = MaterialTheme.typography.titleMedium
                )
                IconButton(
                    onClick = { onDateSelected(selectedDate!!) }
                ) {
                    Icon(
                        Icons.Default.Add,
                        contentDescription = "Ajouter un événement",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
            if (filteredEvents.isNotEmpty()) {
                filteredEvents.forEach { event ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clickable {
                                val intent = Intent(context, EventDetailActivity::class.java).apply {
                                    putExtra("eventId", event.id.toString())
                                    putExtra("title", event.title)
                                    putExtra("date", event.date)
                                    putExtra("description", event.description)
                                }
                                context.startActivity(intent)
                            }
                    ) {
                        Column(modifier = Modifier.padding(8.dp)) {
                            Text(text = event.title, style = MaterialTheme.typography.bodyLarge)
                            Text(text = event.description, style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
            } else {
                Text(
                    text = "Aucun événement pour cette date",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }
        }
    }
}
