package com.example.homieapppreview

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothProfile
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.util.Log
import androidx.annotation.RequiresPermission
import androidx.core.app.ActivityCompat
import android.content.pm.PackageManager
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import java.util.UUID

class MainActivity : ComponentActivity() {
    val SERVICE_UUID = UUID.fromString("12345678-1234-1234-1234-1234567890ab")

    var bluetoothGatt: BluetoothGatt? = null
    var discoveredDevice: BluetoothDevice? = null

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    fun conectarDispositivo() {
        discoveredDevice?.let { device ->
            bluetoothGatt = device.connectGatt(this, false, gattCallback)
        }
    }

    private val gattCallback = object : BluetoothGattCallback() {
        @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
        override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                Log.d("BLE", "Conectado al ESP32")
                gatt.discoverServices()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val bluetoothManager = getSystemService(BluetoothManager::class.java)
        val bluetoothAdapter = bluetoothManager.adapter

        enableEdgeToEdge()
        setContent {
            HomieAppPreviewTheme {
                HomieAppPreviewUI(bluetoothAdapter) {
                    if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED) {
                        conectarDispositivo()
                    }
                }
            }
        }
    }
}

@Composable
fun HomieAppPreviewUI(bluetoothAdapter: BluetoothAdapter?, onConnectClick: () -> Unit) {
    var temperature by remember { mutableStateOf("N/A") }
    var humidity by remember { mutableStateOf("N/A") }
    var airQuality by remember { mutableStateOf("N/A") }
    var connected by remember { mutableStateOf(false) }

    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        HomieAppPreviewLayout(
            connected, temperature, humidity, airQuality,
            bluetoothAdapter = bluetoothAdapter,
            onConnectClick = onConnectClick
        )
    }
}

@Composable
fun HomieAppPreviewLayout(
    connected: Boolean,
    temperature: String,
    humidity: String,
    airQuality: String,
    bluetoothAdapter: BluetoothAdapter?,
    onConnectClick: () -> Unit
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val activity = context as? MainActivity
    var foundDeviceName by remember { mutableStateOf("") }

    val scanCallback = remember {
        object : ScanCallback() {
            override fun onScanResult(callbackType: Int, result: ScanResult) {
                val name = try { result.device.name } catch (e: SecurityException) { null }
                if (name != null && name.startsWith("HM")) {
                    foundDeviceName = name
                    activity?.discoveredDevice = result.device
                }
            }
        }
    }

    var temperatureData = if (connected) "${temperature}°C" else "N/A"
    var humidityData = if (connected) "${humidity}%" else "N/A"
    var airQualityData = if (connected) "${airQuality}ppm" else "N/A"

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(top = 48.dp, start = 32.dp, end = 32.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                PrincipalText(text = "Homie Mobile", size = 32)
                Button(
                    onClick = {
                        if (foundDeviceName.isEmpty()) {
                            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_SCAN) == PackageManager.PERMISSION_GRANTED) {
                                bluetoothAdapter?.bluetoothLeScanner?.startScan(scanCallback)
                            }
                        } else {
                            onConnectClick()
                        }
                    },
                    modifier = Modifier.size(44.dp),
                    contentPadding = PaddingValues(0.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = colorResource(R.color.blue_homie)),
                    shape = RoundedCornerShape(24.dp)
                ) {
                    Text("+", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                }
            }

            Text(
                text = if (foundDeviceName.isNotEmpty()) "Connecter a $foundDeviceName" else "Disconnected",
                color = if (foundDeviceName.isNotEmpty()) colorResource(R.color.green_homie) else colorResource(R.color.alert_color),
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Column(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
            DataRow(Icons.Filled.Home, "Temperature: ", temperatureData, R.color.blue_homie)
            DataRow(Icons.Filled.Settings, "Humidity: ", humidityData, R.color.blue_homie)
            DataRow(Icons.Filled.Warning, "Air Quality: ", airQualityData, R.color.blue_homie)
        }
    }
}

@Composable
fun DataRow(imageVector: ImageVector, type: String, data: String, color: Int) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp).height(56.dp)
            .clip(RoundedCornerShape(8.dp)).background(MaterialTheme.colorScheme.surface),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = imageVector, contentDescription = null,
            tint = colorResource(color),
            modifier = Modifier.size(64.dp).padding(horizontal = 8.dp)
        )
        SecondaryText(text = type, size = 28)
        PrincipalText(text = data, size = 28)
    }
}

@Composable
fun PrincipalText(text: String, size: Int) {
    Text(text = text, color = MaterialTheme.colorScheme.primary, fontSize = size.sp, fontWeight = FontWeight.SemiBold)
}

@Composable
fun SecondaryText(text: String, size: Int) {
    Text(text = text, color = MaterialTheme.colorScheme.secondary, fontSize = size.sp, fontWeight = FontWeight.Normal)
}

@Preview(showBackground = true)
@Composable
fun HomieAppPreviewPreview() {
    HomieAppPreviewTheme {
        HomieAppPreviewUI(null, onConnectClick = {})
    }
}