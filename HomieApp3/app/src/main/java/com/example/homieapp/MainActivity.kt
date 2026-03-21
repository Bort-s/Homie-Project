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
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.res.vectorResource
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

// @PreviewScreenSizes
@Composable
fun HomieAppApp() {
    //Bluetooth
    HomieMobile.temp = 27
    HomieMobile.hum = 45

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
    
    var colorTemp by remember { mutableStateOf(colorTemperature(HomieMobile.temp)) }
    var colorHum by remember { mutableStateOf(colorHumidity(HomieMobile.hum)) }
    var colorAQ by remember { mutableStateOf(Color.Green) }


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
                        PrincipalText("Vista General", 24, modifier = Modifier.padding(bottom = 8.dp))
                        Row(modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp, top = 8.dp)) {
                            Column(
                                verticalArrangement = Arrangement.SpaceEvenly,
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier
                                    .weight(1f)
                                    .height(160.dp)
                                    .clip(RoundedCornerShape(20.dp))
                                    .background(MaterialTheme.colorScheme.surface)
                                ) {
                                Box(
                                    modifier = Modifier
                                        .size(48.dp)
                                        .clip(CircleShape)
                                        .background(colorTemp.copy(alpha = 0.1f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = ImageVector.vectorResource(id = R.drawable.device_thermostat),
                                        contentDescription = "Termostato",
                                        tint = colorTemp,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                                PrincipalText("${HomieMobile.temp}°C", 32)
                                SecondaryText("Temperatura", 16)
                            }
                            Spacer(modifier = Modifier.width(16.dp))
                            Column(
                                verticalArrangement = Arrangement.SpaceEvenly,
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier
                                    .weight(1f)
                                    .height(160.dp)
                                    .clip(RoundedCornerShape(20.dp))
                                    .background(MaterialTheme.colorScheme.surface)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(48.dp)
                                        .clip(CircleShape)
                                        .background(colorHum.copy(alpha = 0.1f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = ImageVector.vectorResource(id = R.drawable.water_drop),
                                        contentDescription = "Humedad",
                                        tint = colorHum,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                                PrincipalText("${HomieMobile.hum}%", 32)
                                SecondaryText("H. Relativa", 16)
                            }
                        }
                        Row(horizontalArrangement = Arrangement.SpaceEvenly,
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(110.dp)
                                .padding(top = 8.dp)
                                .clip(RoundedCornerShape(20.dp))
                                .background(MaterialTheme.colorScheme.surface)) {
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(CircleShape)
                                    .background(colorAQ.copy(alpha = 0.1f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = ImageVector.vectorResource(id = R.drawable.air),
                                    contentDescription = "Aire",
                                    tint = colorAQ,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            Column(modifier = Modifier.fillMaxHeight(),
                                verticalArrangement = Arrangement.Center,
                                horizontalAlignment = Alignment.Start
                                ) {
                                PrincipalText("Calidad del aire", 16)
                                SecondaryText("Exellent", 14)
                                SecondaryText("AQI", 14)
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
            PrincipalText("Acerca de", 24)
            SecondaryText("**que es homie**", 16)
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
fun colorTemperature(temperature: Int): Color {
    var r: Int
    var g: Int
    if ( temperature > 21) {
        if (temperature < 25) {
            g = 255
            r = 255 - 64 * (26 - temperature)
        } else {
            r = 255
            g = 255 - 51 * (temperature - 26)
        }
    } else {
        if (temperature > 16) {
            g = 255
            r = 255 - 64 * (temperature - 17)
        } else {
            r = 255
            g = 255 - 51 * (17 - temperature)
        }
    }
    if (r < 0) r = 0
    if (g < 0) g = 0

    return Color(r, g, 0)
}

fun colorHumidity(Humidity: Int): Color {
    var r: Int
    var g: Int
    if ( Humidity > 45) {
        if (Humidity < 56) {
            g = 255
            r = 255 - 26 * (55 - Humidity)
        } else {
            r = 255
            g = 255 - 17 * (Humidity - 55)
        }
    } else {
        if (Humidity > 35) {
            g = 255
            r = 255 - 26 * (Humidity - 35)
        } else {
            r = 255
            g = 255 - 17 * (35 - Humidity)
        }
    }
    if (r < 0) r = 0
    if (g < 0) g = 0

    return Color(r, g, 0)
}

//Objects
object HomieMobile {
    var name = ""
    var id = ""
    var temp = 0
    var hum = 0
    var AQ = 0
    var connected = false
    var declared = false
}

