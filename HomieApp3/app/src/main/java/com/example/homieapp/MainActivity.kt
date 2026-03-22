package com.example.homieapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.BorderStroke
import androidx.compose.runtime.collectAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteDefaults
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.homieapp.model.Block
import com.example.homieapp.ui.theme.HomieAppTheme
import com.example.homieapp.viewmodels.GuidesViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive

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
    // Homie Mobile
    val homieMobileData = remember { GetHomieMobileData() }
    var homieMobile by remember { mutableStateOf(HomieMobile()) }

    LaunchedEffect(Unit) {
        while(isActive) {
            delay(1000)
            homieMobile = homieMobile.copy(
                temperature = homieMobileData.getLatest(homieMobile.temperature),
                humidity = homieMobileData.getLatest(homieMobile.humidity),
                state = getState(homieMobile.temperature, homieMobile.humidity, homieMobile.AQI)
            )
        }
    }

    // Colors
    val navigationColors = NavigationSuiteDefaults.itemColors(
        navigationBarItemColors = androidx.compose.material3.NavigationBarItemDefaults.colors(
            selectedIconColor = Color(0xFF0055d4),
            selectedTextColor = Color(0xFF0055d4),
            unselectedIconColor = Color(0xFF7D7C7C),
            unselectedTextColor = Color(0xFF7D7C7C),
            indicatorColor = Color.Transparent,
        )
    )
    val suiteColors = NavigationSuiteDefaults.colors(
        navigationBarContainerColor = MaterialTheme.colorScheme.surface,
    )

    // Navigation
    var currentDestination by rememberSaveable { mutableStateOf(AppDestinations.HOME) }

    NavigationSuiteScaffold(
        navigationSuiteColors = suiteColors,
        navigationSuiteItems = {
            AppDestinations.entries.filter { it.showInBottomBar }.forEach {
                item(
                    icon = {
                        Icon(
                            painterResource(it.icon),
                            contentDescription = it.label
                        )
                    },
                    label = { Text(it.label) },
                    selected = it == currentDestination,
                    onClick = { currentDestination = it },
                    colors = navigationColors,
                )
            }
        }
    ) {
        Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
            Surface(modifier = Modifier.padding(innerPadding)) {
                when (currentDestination) {
                AppDestinations.HOME -> HomeScreen(homieMobile,
                    onNavigateToGuide = { currentDestination = AppDestinations.GUIDE })
                AppDestinations.DEVICES -> DevicesScreen()
                AppDestinations.Alerts -> AlertsScreen()
                    AppDestinations.GUIDE -> GuideScreen(onNavigateToHome = { currentDestination = AppDestinations.HOME })
            }}
        }
    }
}

enum class AppDestinations(
    val label: String,
    val icon: Int,
    val showInBottomBar: Boolean = true
) {
    HOME("Home", R.drawable.home),
    DEVICES("Devices", R.drawable.home_iot_device),
    Alerts("Alerts", R.drawable.notification_important_24dp_1f1f1f_fill0_wght400_grad0_opsz24),
    GUIDE("Guía", R.drawable.info, showInBottomBar = false)
}

// Layout Composables
@Composable
fun HomeScreen(homieMobile: HomieMobile, onNavigateToGuide: () -> Unit) {
    var expandedInfo by remember { mutableStateOf(false) }
    var showSheet by remember { mutableStateOf(false) }

    val colorTemperature = colorTemperature(homieMobile.temperature)
    val colorHumidity = colorHumidity(homieMobile.humidity)
    val colorAQ = Color.Green




    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .padding(start = 16.dp, end = 16.dp, top = 8.dp)
                .fillMaxSize()
        ) {
            // Header
            Row(modifier = Modifier
                .fillMaxWidth()
                .height(80.dp)
                .padding(bottom = 12.dp)) {
                Image(
                    painter = painterResource(id = R.drawable.happy_dommy),
                    modifier = Modifier
                        .padding(end = 16.dp)
                        .size(80.dp),
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
                item {
                    Column(modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp, top = 16.dp)) {
                        PrincipalText("Vista General", 24, modifier = Modifier.padding(bottom = 8.dp))
                        Row(modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 6.dp, top = 8.dp)) {
                            Column(
                                verticalArrangement = Arrangement.SpaceEvenly,
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier
                                    .weight(1f)
                                    .height(180.dp)
                                    .clip(RoundedCornerShape(20.dp))
                                    .background(MaterialTheme.colorScheme.surface)
                                ) {
                                Box(
                                    modifier = Modifier
                                        .size(64.dp)
                                        .clip(CircleShape)
                                        .background(colorTemperature.copy(alpha = 0.1f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = ImageVector.vectorResource(id = R.drawable.device_thermostat),
                                        contentDescription = "Termostato",
                                        tint = colorTemperature,
                                        modifier = Modifier.size(32.dp)
                                    )
                                }
                                PrincipalText("${homieMobile.temperature}°C", 32)
                                SecondaryText("Temperatura", 16)
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(
                                verticalArrangement = Arrangement.SpaceEvenly,
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier
                                    .weight(1f)
                                    .height(180.dp)
                                    .clip(RoundedCornerShape(20.dp))
                                    .background(MaterialTheme.colorScheme.surface)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(64.dp)
                                        .clip(CircleShape)
                                        .background(colorHumidity.copy(alpha = 0.1f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = ImageVector.vectorResource(id = R.drawable.water_drop),
                                        contentDescription = "Humedad",
                                        tint = colorHumidity,
                                        modifier = Modifier.size(32.dp)
                                    )
                                }
                                PrincipalText("${homieMobile.humidity}%", 32)
                                SecondaryText("H. Relativa", 16)
                            }
                        }
                        Row(horizontalArrangement = Arrangement.SpaceEvenly,
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(110.dp)
                                .padding(top = 6.dp)
                                .clip(RoundedCornerShape(20.dp))
                                .background(MaterialTheme.colorScheme.surface)) {
                            Box(
                                modifier = Modifier
                                    .size(64.dp)
                                    .clip(CircleShape)
                                    .background(colorAQ.copy(alpha = 0.1f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = ImageVector.vectorResource(id = R.drawable.air),
                                    contentDescription = "Aire",
                                    tint = colorAQ,
                                    modifier = Modifier.size(32.dp)
                                )
                            }
                            Column(modifier = Modifier.fillMaxHeight(),
                                verticalArrangement = Arrangement.Center,
                                horizontalAlignment = Alignment.Start
                                ) {
                                PrincipalText("Calidad del aire", 16)
                                SecondaryText("Exellent", 14)
                                SecondaryText("AQI: ${homieMobile.AQI}, State: ${homieMobile.state}", 14)
                            }
                        }
                    }
                }
                item {
                    Button(onClick = onNavigateToGuide,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF0055d4).copy(alpha = 0.2f)
                        ),
                        border = BorderStroke(2.dp, Color(0xFF0055d4)),
                        modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 32.dp, top = 16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(vertical = 16.dp)
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.menu_book),
                                contentDescription = "Guia",
                                modifier = Modifier.size(32.dp),
                                tint = Color(0xFF0055d4)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Consulta Nuestra Guia",
                                color = Color(0xFF0055d4),
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 24.sp,
                            )
                        }
                    }
                }
                item {
                    Column(modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .background(MaterialTheme.colorScheme.surface)
                        .padding(bottom = 16.dp, top = 16.dp)) {
                        Row(horizontalArrangement = Arrangement.Start,
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .padding(start = 16.dp, bottom = 8.dp)
                                .fillMaxWidth()
                        ){
                            Image(
                                painter = painterResource(R.drawable.dom_e_sabio),
                                contentDescription = "Dom-e",
                                modifier = Modifier
                                    .size(32.dp)
                                    .padding(end = 8.dp)
                            )
                            PrincipalText("Dato ambiental del dia", 24)
                        }
                        SecondaryText(facts[2], 16, modifier = Modifier.padding(start = 16.dp, end = 16.dp))
                    }
                    Spacer(modifier = Modifier.height(16.dp))
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
            modifier = Modifier
                .padding(start = 32.dp, end = 32.dp, top = 8.dp)
                .fillMaxSize()
        ) {

        }
    }
}

@Composable
fun AlertsScreen() {
    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {

    }
}

@Composable
fun GuideScreen(onNavigateToHome: () -> Unit) {
    val viewmodel: GuidesViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
    val allGuides by viewmodel.listGuides.collectAsState()
    var currentGuide by remember { mutableIntStateOf(0) }
    val guide = allGuides[currentGuide]


    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .padding(start = 16.dp, end = 16.dp, top = 8.dp)
                .fillMaxSize()
        ) {
            // Header
            Row(modifier = Modifier
                .fillMaxWidth()
                .padding()) {
                Row(verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)) {
                    IconButton(onClick = onNavigateToHome
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.arrow_back),
                            contentDescription = "Regresar",
                            tint = Color(0xFF0055d4),
                            modifier = Modifier.size(32.dp)
                        )
                    }
                    Text(
                        "Guia de Usuario",
                        color = Color(0xFF0055d4),
                        fontSize = 24.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }
            }
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                item {
                    Text(text = "Sección: ${guide.chapter}")
                }

                items(guide.blocks) { bloque ->
                    BlockGuide(bloque)
                }
            }
        }
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

@Composable
fun BlockGuide(block: Block) {
        Column(modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(MaterialTheme.colorScheme.surface)
            .padding(bottom = 16.dp, top = 16.dp)) {
            Row(horizontalArrangement = Arrangement.Start,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .padding(start = 16.dp, bottom = 8.dp)
                    .fillMaxWidth()
            ){
                PrincipalText(block.title, 32,)
            }
            SecondaryText(block.content, 16, modifier = Modifier.padding(start = 16.dp, end = 16.dp))
        }
        Spacer(modifier = Modifier.height(16.dp))
}




// Composable Preview
//@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
    HomieAppTheme {
        val homieMobile = HomieMobile(
            25,
            45,
            AQI = 67,
            state = 0,
            id = "000001",
            name = "Homie Mobile",
            register = true,
            connected = true
        )
        homieMobile.state = getState(homieMobile.temperature, homieMobile.humidity, homieMobile.AQI)
        HomeScreen(homieMobile, onNavigateToGuide = {})
    }
}

@Preview (showBackground = true)
@Composable
fun GuideScreenPreview() {
    HomieAppTheme {
        GuideScreen(onNavigateToHome = {})
    }
}


// Functions
fun colorTemperature(temperature: Int): Color {
    var r: Int
    var g: Int
    if ( temperature > 21) {
        if (temperature < 26) {
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

fun getState(temperature: Int, humidity: Int, AQI: Int): Int {
    return if (AQI > 150) 7
    else if (AQI > 100) 6
    else if (temperature !in 16..28) {
        if (humidity !in 30..70) 5
        else if (temperature < 16) 2
        else 1
    }
    else if (humidity < 30) 4
    else if (humidity > 60) 3
    else 0
}

// Arrays

val advice = arrayOf(
    "Todo parace estar en orden.",
    "La temperatura se encuentra arriba del rango recomendado",
    "La temperatura se encuentra abajo del rango recomendado",
    "La humedad se encuentra arriba del rango recomendado",
    "La humedad se encuentra abajo del rango recomendado",
    "La temperatura y humedad se encuentra en rangos no recomendado",
    "El nivel de calidad del aire se encuentra abajo del rango recomendado",
    "El nivel de calidad del aire es muy malo, tome medidas inmediatamente"
)

val facts = arrayOf(
    "¿Sabías que los bosques tienen su propia red social? A través de una red de hongos subterráneos llamada micorriza, los árboles se comunican, comparten nutrientes e incluso se advierten sobre plagas.",
    "A finales de los 80, el mundo se unió para prohibir los químicos que destruían la capa de ozono (Protocolo de Montreal). ¿El resultado? Se está recuperando tan bien que se espera que para el 2066 esté completamente sanada sobre la Antártida.",
    "Una ballena promedio captura la misma cantidad de CO2 que 1,000 árboles. Sus desechos fertilizan el fitoplancton, que a su vez absorbe el 40% de todo el dióxido de carbono producido en el mundo y genera más de la mitad del oxígeno que respiramos.",
    "En la última década, el costo de la energía solar ha caído un 89%. Hoy en día, en muchas partes del mundo, es más barato construir una nueva planta solar que seguir operando una de carbón ya existente.",
    "No hace falta ser vegano estricto para ayudar. Si una familia promedio en EE. UU. (o cualquier país con alto consumo de carne) redujera su consumo de carne roja a la mitad, sería el equivalente a quitar su auto de la carretera por 6 meses.",
    "Lugares como la isla de El Hierro en España o Tokelau en el Pacífico ya funcionan casi en su totalidad con energía limpia (viento, agua y sol).",
)

val domeState = intArrayOf(
    R.drawable.dom_e_esperanzado,
    R.drawable.dom_e_fuego,
    R.drawable.dom_e_congelado,
    R.drawable.afraid_dommy,
    R.drawable.afraid_dommy,
    R.drawable.afraid_dommy,
    R.drawable.dom_e_humo,
    R.drawable.dead_dommy
)

// Classes
data class HomieMobile(
    var temperature: Int = 21,
    var humidity: Int = 41,
    var AQI: Int = 67,
    var state: Int = 0,
    var id: String = "000000",
    var name: String = "Homie Mobile",
    var register: Boolean = false,
    var connected: Boolean = false
)

class GetHomieMobileData {
    fun getLatest(vall: Int): Int {
        return vall + (-1..1).random()
    }
}