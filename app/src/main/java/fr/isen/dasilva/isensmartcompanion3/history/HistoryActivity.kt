package fr.isen.dasilva.isensmartcompanion3.history

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import java.text.SimpleDateFormat
import java.util.Date
import androidx.compose.runtime.getValue

@OptIn(ExperimentalMaterial3Api::class)
class HistoryActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val viewModel: HistoryViewModel =
                viewModel(factory = ViewModelProvider.AndroidViewModelFactory(application))
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
                LazyColumn(modifier = Modifier.Companion.padding(it)) {
                    items(messages.size) { index ->
                        val msg = messages[index]
                        Card(
                            modifier = Modifier.Companion
                                .fillMaxWidth()
                                .padding(8.dp)
                                .clickable { viewModel.deleteMessage(msg) }
                        ) {
                            Column(modifier = Modifier.Companion.padding(16.dp)) {
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