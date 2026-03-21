package com.example.homieapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewScreenSizes
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import com.example.homieapp.ui.theme.HomieAppTheme
import org.intellij.lang.annotations.JdkConstants

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            HomieAppTheme {
                HomieAppApp()
            }
        }
    }
}

@PreviewScreenSizes
@Composable
fun HomieAppApp() {
    //Bluetooth

    var currentDestination by rememberSaveable { mutableStateOf(AppDestinations.HOME) }

    NavigationSuiteScaffold(
        navigationSuiteItems = {
            AppDestinations.entries.forEach {
                item(
                    icon = {
                        Icon(
                            painterResource(it.icon),
                            contentDescription = it.label
                        )
                    },
                    label = { Text(it.label) },
                    selected = it == currentDestination,
                    onClick = { currentDestination = it }
                )
            }
        }
    ) {
        Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
            Surface(modifier = Modifier.padding(innerPadding)) {
                when (currentDestination) {
                AppDestinations.HOME -> HomeScreen()
                AppDestinations.DEVICES -> DevicesScreen()
                AppDestinations.Alerts -> AlertsScreen()
            }}
        }
    }
}

enum class AppDestinations(
    val label: String,
    val icon: Int,
) {
    HOME("Home", R.drawable.home),
    DEVICES("Devices", R.drawable.home_iot_device),
    Alerts("Alerts", R.drawable.notification_important_24dp_1f1f1f_fill0_wght400_grad0_opsz24),
}

// Layout Composables
@Composable
fun HomeScreen() {
    var expandedInfo by remember { mutableStateOf(false) }
    var showSheet by remember { mutableStateOf(false) }

    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(start = 32.dp, end = 32.dp, top = 8.dp).fillMaxSize()
        ) {
            // Header
            Row(modifier = Modifier.fillMaxWidth().height(80.dp).padding(bottom = 12.dp)) {
                Image(
                    painter = painterResource(id = R.drawable.happy_dommy),
                    modifier = Modifier.padding(end = 16.dp).size(80.dp),
                    contentDescription = "Dom-e Feliz")

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Column(Modifier.fillMaxHeight(), verticalArrangement = Arrangement.SpaceEvenly) {
                        SecondaryText("Bienvenido", 16)
                        PrincipalText("Homie App", 32)
                        SecondaryText("", 16)
                    }

                    Box(modifier = Modifier.wrapContentSize(Alignment.TopEnd)) {
                        IconButton(onClick = { expandedInfo = true }) {
                            Icon(
                                painter = painterResource(id = R.drawable.info),
                                contentDescription = "Info"
                            )
                        }
                        DropdownMenu(
                            expanded = expandedInfo,
                            onDismissRequest = { expandedInfo = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Información") },
                                onClick = {
                                    expandedInfo = false
                                    showSheet = true
                                }
                            )
                        }
                        if (showSheet) {
                            InfoModalBottomSheet(onDismiss = { showSheet = false })
                        }
                    }
                }
            }
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                item() {
                    Column(modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp, top = 12.dp)) {
                        PrincipalText("Vista general", 28, modifier = Modifier.padding(bottom = 8.dp))
                        Row(modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp, top = 8.dp)) {
                            Column(
                                verticalArrangement = Arrangement.SpaceEvenly,
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier
                                    .weight(1f)
                                    .background(MaterialTheme.colorScheme.surface)
                                    .clip(RoundedCornerShape(8.dp))
                                ) {
                                Text("AA")
                                Text("AB")
                            }
                            Spacer(modifier = Modifier.width(16.dp))
                            Column(
                                verticalArrangement = Arrangement.SpaceEvenly,
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier
                                    .weight(1f)
                                    .background(MaterialTheme.colorScheme.surface)
                                    .clip(RoundedCornerShape(8.dp))
                            ) {
                                Text("BA")
                                Text("BB")
                            }
                        }
                    }
                }
            }
        }
    }
}
@Composable
fun DevicesScreen()  {
    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(start = 32.dp, end = 32.dp, top = 8.dp).fillMaxSize()
        ) {



        }
    }
}
@Composable
fun AlertsScreen() {
    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {

    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InfoModalBottomSheet(onDismiss: () -> Unit) {
    val sheetState = rememberModalBottomSheetState()

    ModalBottomSheet(
        onDismissRequest = { onDismiss() },
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface
    ) {
        // Content
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            PrincipalText("Información", 24)
            SecondaryText("Esta es la app de Homie para controlar tus dispositivos.", 16)
        }
    }
}



// Resource Composables
@Composable
fun PrincipalText(text: String, size: Int, modifier: Modifier = Modifier) {
    Text(text = text, color = MaterialTheme.colorScheme.primary, fontSize = size.sp, fontWeight = FontWeight.SemiBold, modifier = modifier)
}

@Composable
fun SecondaryText(text: String, size: Int, modifier: Modifier = Modifier) {
    Text(text = text, color = MaterialTheme.colorScheme.secondary, fontSize = size.sp, fontWeight = FontWeight.Normal, modifier = modifier)
}






// Composable Preview
@Preview(showBackground = true)
@Composable
fun AppPreview() {
    HomieAppTheme {
        HomeScreen()
    }
}

// Functions


//Objects
object HomieMobile {
    var name = ""
    var id = ""
    var temp = 0
    var hum = 0
    var connected = false
    var declared = false
}

