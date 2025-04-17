package fr.isen.dasilva.isensmartcompanion3.agenda

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import fr.isen.dasilva.isensmartcompanion3.event.Event
import fr.isen.dasilva.isensmartcompanion3.event.RetrofitClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.TextStyle
import java.util.*


@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AgendaScreen() {
    val pagerState = rememberPagerState(
        initialPage = 120,
        pageCount = {240})
    val context = LocalContext.current
    val currentPage = pagerState.currentPage
    val yearMonth = remember(currentPage) {
        YearMonth.now().plusMonths((currentPage - 120).toLong())
    }
    HorizontalPager(
        state = pagerState
    ) { pageOffset ->
        val offsetMonth = YearMonth.now().plusMonths((pageOffset - 120).toLong())
        CalendarMonthView(yearMonth = offsetMonth, context = context)
    }
}

@Composable
fun CalendarMonthView(yearMonth: YearMonth, context: Context) {
    val daysInMonth = yearMonth.lengthOfMonth()
    val firstDayOfWeek = yearMonth.atDay(1).dayOfWeek.value % 7 // 0 (dimanche) à 6 (samedi)

    val daysList = remember(yearMonth) {
        buildList {
            repeat(firstDayOfWeek) { add("") }
            for (day in 1..daysInMonth) add(day.toString())
        }
    }

    var selectedDate by remember { mutableStateOf<LocalDate?>(null) }
    var events by remember { mutableStateOf<List<Event>>(emptyList()) }

    val daysWithEvents = remember(events) {
        events.mapNotNull { event ->
            try {
                val eventDate = LocalDate.parse(event.date)
                if (eventDate.month == yearMonth.month && eventDate.year == yearMonth.year) {
                    eventDate.dayOfMonth
                } else null
            } catch (e: Exception) {
                null
            }
        }.toSet()
    }
    // Récupération des événements
    LaunchedEffect(selectedDate) {
        selectedDate?.let {
            fetchEvents(context, it) { fetchedEvents ->
                events = fetchedEvents
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Column {
            // Titre du mois
            Text(
                text = "${yearMonth.month.getDisplayName(TextStyle.FULL, Locale.FRENCH)} ${yearMonth.year}",
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Noms des jours
            Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                listOf("L", "M", "M", "J", "V", "S", "D").forEach { day ->
                    Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                        Text(text = day)
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Grille des jours
            LazyVerticalGrid(
                columns = GridCells.Fixed(7),
                modifier = Modifier
                    .fillMaxHeight()
                    .weight(1f),
                content = {
                    items(daysList.size) { index ->
                        val day = daysList[index]
                        Box(
                            modifier = Modifier
                                .aspectRatio(1f)
                                .padding(4.dp)
                                .clickable(enabled = day.isNotEmpty()) {
                                    day.toIntOrNull()?.let {
                                        selectedDate = LocalDate.of(yearMonth.year, yearMonth.month, it)
                                    }
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            val isSelected = day.toIntOrNull()?.let {
                                selectedDate == LocalDate.of(yearMonth.year, yearMonth.month, it)
                            } ?: false

                            Text(
                                text = day,
                                color = when {
                                    isSelected -> Color.Blue
                                    day.toIntOrNull() in daysWithEvents -> Color(0xFF2E7D32) // Vert foncé pour les jours avec événement
                                    else -> Color.Black
                                }
                            )
                        }
                    }
                }
            )

            // Affichage des événements
            Spacer(modifier = Modifier.height(16.dp))
            if (selectedDate != null && events.isNotEmpty()) {
                Text(
                    text = "Événements pour le ${selectedDate!!.dayOfMonth}",
                    style = MaterialTheme.typography.titleMedium
                )
                events.forEach { event ->
                    Column(modifier = Modifier.padding(8.dp)) {
                        Text(text = event.title, style = MaterialTheme.typography.bodyLarge)
                        Text(text = event.description, style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
        }
    }
}

// Fonction de récupération des événements (non composable)
/*fun fetchEvents(context: Context, date: LocalDate, onEventsFetched: (List<Event>) -> Unit) {
    RetrofitClient.instance.getEventsForDate(date.toString()).enqueue(object : Callback<List<Event>> {
        override fun onResponse(call: Call<List<Event>>, response: Response<List<Event>>) {
            if (response.isSuccessful) {
                onEventsFetched(response.body() ?: emptyList())
            } else {
                Toast.makeText(context, "Erreur lors de la récupération des événements", Toast.LENGTH_SHORT).show()
            }
        }

        override fun onFailure(call: Call<List<Event>>, t: Throwable) {
            Toast.makeText(context, "Erreur de connexion", Toast.LENGTH_SHORT).show()
        }
    })
}*/
fun fetchEvents(context: Context, date: LocalDate, onEventsFetched: (List<Event>) -> Unit) {
    RetrofitClient.instance.getEvents().enqueue(object : Callback<List<Event>> {
        override fun onResponse(call: Call<List<Event>>, response: Response<List<Event>>) {
            if (response.isSuccessful) {
                val allEvents = response.body() ?: emptyList()
                val filteredEvents = allEvents.filter {
                    // Vérifie si le champ date correspond à la date sélectionnée
                    it.date == date.toString() // date doit être au format "yyyy-MM-dd"
                }
                onEventsFetched(filteredEvents)
            } else {
                Toast.makeText(context, "Erreur lors de la récupération des événements", Toast.LENGTH_SHORT).show()
            }
        }

        override fun onFailure(call: Call<List<Event>>, t: Throwable) {
            Toast.makeText(context, "Erreur de connexion", Toast.LENGTH_SHORT).show()
        }
    })
}
