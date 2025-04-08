package fr.isen.dasilva.isensmartcompanion3

import android.app.Application
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import fr.isen.dasilva.isensmartcompanion3.ui.theme.Isensmartcompanion3Theme
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import fr.isen.dasilva.isensmartcompanion3.GeminiApiService
import fr.isen.dasilva.isensmartcompanion3.R
import fr.isen.dasilva.isensmartcompanion3.event.EventScreen
import androidx.compose.foundation.layout.Column
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import fr.isen.dasilva.isensmartcompanion3.history.HistoryViewModel
import fr.isen.dasilva.isensmartcompanion3.history.HistoryViewModelFactory
import fr.isen.dasilva.isensmartcompanion3.history.MessageDatabase

data class TabBarItem(
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val badgeAmount: Int? = null
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val homeTab = TabBarItem("Home", Icons.Filled.Home, Icons.Filled.Home)
            val eventsTab = TabBarItem("Events", Icons.Filled.DateRange, Icons.Filled.DateRange)
            val agendaTab = TabBarItem("Agenda", Icons.Filled.Person, Icons.Filled.Person)
            val moreTab = TabBarItem("History", Icons.Filled.Search, Icons.Filled.Search)
            val tabBarItems = listOf(homeTab, eventsTab, agendaTab, moreTab)
            val navController = rememberNavController()

            val db = MessageDatabase.getDatabase(applicationContext)
            val historyDao = db.messageDao()

            val viewModel: HistoryViewModel = viewModel(
                factory = HistoryViewModelFactory(application) // Passe l'application ici
            )

            Isensmartcompanion3Theme {
                Scaffold(
                    topBar = { TopSection() },
                    bottomBar = {TabView(tabBarItems, navController)}
                ) { innerPadding ->

                    NavHost(
                        navController = navController,
                        startDestination = homeTab.title,
                        modifier = Modifier.padding(innerPadding)
                    ){
                        composable(homeTab.title){ HomeScreen() }
                        composable(eventsTab.title){ EventScreen() }
                        composable(agendaTab.title){ Text(agendaTab.title) }
                        composable(moreTab.title){ MoreView() }
                    }
                }
            }
        }
    }
}

@Composable
fun TopSection() {
    Column(
        modifier = Modifier.fillMaxWidth().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ){
        Image(
            painter = painterResource(id = R.drawable.logo),
            modifier = Modifier.size(100.dp),
            contentDescription = "Logo"
        )
        Spacer(modifier = Modifier.height(16.dp))
        SearchBar()
    }
}


@Composable
fun SearchBar() {
    var searchText by remember { mutableStateOf(TextFieldValue("")) }
    var responseText by remember { mutableStateOf("Posez-moi une question !") }
    var isLoading by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val dao = MessageDatabase.getDatabase(context).messageDao()

    val viewModel: HistoryViewModel = viewModel(factory = HistoryViewModelFactory(context.applicationContext as Application))

    Column(modifier = Modifier.fillMaxWidth()) { // ✅ Correction ici
        OutlinedTextField(
            value = searchText,
            onValueChange = { searchText = it },
            leadingIcon = { Icon(Icons.Filled.Search, contentDescription = "Search") },
            label = { Text("Rechercher") },
            modifier = Modifier.fillMaxWidth(),
            trailingIcon = {
                IconButton(onClick = {
                    if (searchText.text.isNotEmpty()) {
                        isLoading = true
                        GeminiApiService.getResponse (searchText.text) { response ->
                            responseText = response
                            viewModel.addHistory(searchText.text, response)
                            isLoading = false
                        }
                    }
                }) {
                    Icon(Icons.Filled.Send, contentDescription = "Send")
                }
            }
        )

        Spacer(modifier = Modifier.height(8.dp))

        if (isLoading) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
        } else {
            Text(
                text = responseText,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(8.dp)
            )
        }
    }
}
@Composable
fun HomeScreen() {

}
@Composable
fun TabView(tabBarItems: List<TabBarItem>, navController: NavController) {
    var selectedTabIndex by rememberSaveable { mutableStateOf(0) } // ✅ Correction ici

    NavigationBar {
        tabBarItems.forEachIndexed { index, tabBarItem ->
            NavigationBarItem(
                selected = selectedTabIndex == index,
                onClick = {
                    selectedTabIndex = index
                    navController.navigate(tabBarItem.title)
                },
                icon = {
                    TabBarIconView(
                        isSelected = selectedTabIndex == index,
                        selectedIcon = tabBarItem.selectedIcon,
                        unselectedIcon = tabBarItem.unselectedIcon,
                        title = tabBarItem.title,
                        badgeAmount = tabBarItem.badgeAmount
                    )
                },
                label = { Text(tabBarItem.title) }
            )
        }
    }
}

@Composable
fun TabBarIconView(
    isSelected: Boolean,
    selectedIcon: ImageVector,
    unselectedIcon: ImageVector,
    title: String,
    badgeAmount: Int? = null
) {
    BadgedBox(badge = { TabBarBadgeView(badgeAmount) }) {
        Icon(
            imageVector = if (isSelected) selectedIcon else unselectedIcon,
            contentDescription = title
        )
    }
}

@Composable
fun TabBarBadgeView(count: Int? = null) {
    if (count != null) {
        Badge { Text(count.toString()) }
    }
}

@Composable
fun MoreView() {
    val context = LocalContext.current

    Column(modifier = Modifier
        .fillMaxSize()
        .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Options supplémentaires")

        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = {
            context.startActivity(Intent(context, HistoryActivity::class.java))
        }) {
            Text("Voir l'historique")
        }
    }
}
