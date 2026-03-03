package com.example.homieapppreview

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.homieapppreview.ui.theme.HomieAppPreviewTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            HomieAppPreviewTheme {
                HomieAppPreviewUI()
            }
        }
    }
}

@Composable
fun HomieAppPreviewUI() {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        HomieAppPreviewLayout(
            true,
            32,
            60,
            300
        )
    }
}
@Composable
fun HomieAppPreviewLayout(connected: Boolean, temperature: Int, humidity: Int, airQuality: Int) {
    var temperatureData = "N/A"
    var humidityData = "N/A"
    var airQualityData = "N/A"

    if (connected) {
        temperatureData = "${temperature.toString()}°C"
        humidityData = "${humidity.toString()}%"
        airQualityData = "${airQuality.toString()}ppm"
    }
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(
            top = 48.dp,
            start = 32.dp,
            end = 32.dp,
        )
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
                .padding(bottom = 16.dp)
        ) {
            PrincipalText(
                text = "Homie Mobile",
                size = 32,
            )
            Row() {
                if (connected) {
                    Text(
                        text = "Connected",
                        color = colorResource(R.color.green_homie),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                } else {
                    Text(
                        text = "Disconnected",
                        color = colorResource(R.color.alert_color),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
        Column(
            modifier = Modifier.fillMaxWidth()
                .padding(bottom = 8.dp, top = 8.dp)
        ) {
            DataRow(
                imageVector = Icons.Filled.Home,
                type = "Temperature: ",
                data = temperatureData,
                color = R.color.blue_homie
            )
            DataRow(
                imageVector = Icons.Filled.Settings,
                type = "Humidity: ",
                data = humidityData,
                color = R.color.blue_homie
            )
            DataRow(
                imageVector = Icons.Filled.Warning,
                type = "Air Quality: ",
                data = airQualityData,
                color = R.color.blue_homie
            )
        }
    }
}

@Composable
fun PrincipalText(text: String, size: Int) {
    Text(
        text = text,
        color = MaterialTheme.colorScheme.primary,
        fontSize = size.sp,
        fontWeight = FontWeight.SemiBold
    )
}

@Composable
fun SecondaryText(text: String, size: Int) {
    Text(
        text = text,
        color = MaterialTheme.colorScheme.secondary,
        fontSize = size.sp,
        fontWeight = FontWeight.Normal
    )
}

@Composable
fun DataRow(imageVector: ImageVector, type: String, data: String, color: Int) {
    Row(
        modifier = Modifier.fillMaxWidth()
            .padding(bottom = 8.dp, top = 8.dp)
            .height(56.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.surface),
        verticalAlignment = Alignment.CenterVertically,
        ) {
        Icon(
            imageVector = imageVector,
            contentDescription = null,
            tint = Color(color),
            modifier = Modifier.size(64.dp).padding(start = 8.dp, end = 8.dp)
        )
        SecondaryText(
            text = type,
            size = 28
        )
        PrincipalText(
            text = data,
            size = 28
        )
    }
}

@Preview(showBackground = true)
@Composable
fun HomieAppPreviewPreview() {
    HomieAppPreviewTheme {
        HomieAppPreviewUI()
    }
}