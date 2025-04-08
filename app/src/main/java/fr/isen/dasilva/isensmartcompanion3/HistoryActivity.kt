package fr.isen.dasilva.isensmartcompanion3

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.foundation.lazy.LazyColumn
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModelProvider
import fr.isen.dasilva.isensmartcompanion3.history.HistoryViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
class HistoryActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val viewModel: HistoryViewModel = viewModel(factory = ViewModelProvider.AndroidViewModelFactory(application))
            val messages by viewModel.messages.collectAsState()

            Scaffold(
                topBar = {
                    TopAppBar(
                        title = { Text("Historique des échanges") },
                        actions = {
                            IconButton(onClick = { viewModel.clearAll() }) {
                                Icon(Icons.Default.Delete, contentDescription = "Effacer tout")
                            }
                        }
                    )
                }
            ) {
                LazyColumn(modifier = Modifier.padding(it)) {
                    items(messages.size) { index ->
                        val msg = messages[index]
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp)
                                .clickable { viewModel.deleteMessage(msg) }
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text("📅 ${SimpleDateFormat("dd/MM/yyyy HH:mm").format(Date(msg.timestamp))}")
                                Text("👤 ${msg.question}")
                                Text("🤖 ${msg.response}")
                            }
                        }
                    }
                }
            }
        }
    }
}