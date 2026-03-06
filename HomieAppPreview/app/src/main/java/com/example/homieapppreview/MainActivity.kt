package com.example.homieapppreview

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattDescriptor
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothProfile
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.content.Context
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresPermission
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import com.example.homieapppreview.ui.theme.HomieAppPreviewTheme
import java.util.UUID

class MainActivity : ComponentActivity() {
    var temperature by mutableStateOf("N/A")
    var humidity by mutableStateOf("N/A")
    var airQuality by mutableStateOf("N/A")
    var isConnected by mutableStateOf(false)


    private val SERVICE_UUID = UUID.fromString("12345678-1234-1234-1234-1234567890ab")
    private val TX_UUID = UUID.fromString("12345678-1234-1234-1234-1234567890ac")
    private val CCCD_UUID = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb")

    private lateinit var sharedPreferences: SharedPreferences

    var bluetoothGatt: BluetoothGatt? = null
    var discoveredDevice: BluetoothDevice? = null

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    fun conectarDispositivo() {
        discoveredDevice?.let { device ->
            Log.d("BLE_DEBUG", "Conectando a: ${device.address}")
            bluetoothGatt = device.connectGatt(this, false, gattCallback)
        }
    }

    private val gattCallback = object : BluetoothGattCallback() {
        @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
        override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
            Log.d("BLE_DEBUG", "Estado de conexión: $newState")
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                isConnected = true
                Log.d("BLE_DEBUG", "Conectado. Descubriendo servicios...")
                gatt.discoverServices()
                runOnUiThread { Toast.makeText(this@MainActivity, "Conectado!", Toast.LENGTH_SHORT).show() }
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                isConnected = false
                Log.d("BLE_DEBUG", "Desconectado.")
                runOnUiThread { Toast.makeText(this@MainActivity, "Desconectado", Toast.LENGTH_SHORT).show() }
            }
        }

        @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
        override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                gatt.services.forEach { service ->
                    Log.d("BLE_DEBUG", "Servicio encontrado: ${service.uuid}")
                    service.characteristics.forEach { char ->
                        Log.d("BLE_DEBUG", "  -> Característica: ${char.uuid}")
                    }
                }

                val service = gatt.getService(SERVICE_UUID)
                val txChar = service?.getCharacteristic(TX_UUID)

                if (txChar != null) {
                    gatt.setCharacteristicNotification(txChar, true)

                    val descriptor = txChar.getDescriptor(CCCD_UUID)
                    if (descriptor != null) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            gatt.writeDescriptor(descriptor, BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE)
                        } else {
                            @Suppress("DEPRECATION")
                            descriptor.value = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
                            @Suppress("DEPRECATION")
                            gatt.writeDescriptor(descriptor)
                        }
                    } else {
                        Log.e("BLE_DEBUG", "No se encontró el descriptor 0x2902")
                    }
                } else {
                    Log.e("BLE_DEBUG", "No se encontró la característica TX con UUID: $TX_UUID")
                }
            }
        }

        override fun onCharacteristicChanged(gatt: BluetoothGatt, char: BluetoothGattCharacteristic, value: ByteArray) {
            procesarDatos(String(value))
        }

        @Deprecated("Deprecated in Java")
        override fun onCharacteristicChanged(gatt: BluetoothGatt, char: BluetoothGattCharacteristic) {
            @Suppress("DEPRECATION")
            procesarDatos(String(char.value))
        }
    }

    private fun procesarDatos(raw: String) {
        val limpia = raw.trim()

        val partes = limpia.split(":")
        if (partes.size == 2) {
            val tipo = partes[0].trim()
            val valor = partes[1].trim()
            runOnUiThread {
                when (tipo) {
                    "TEMP" -> {
                        temperature = valor
                        saveData("temperature", valor)
                    }
                    "HUM" -> {
                        humidity = valor
                        saveData("humidity", valor)
                    }
                    "PRES" -> {
                        airQuality = valor
                        saveData("airQuality", valor)
                    }
                }
            }
        }
    }

    private fun saveData(key: String, value: String) {
        sharedPreferences.edit().putString(key, value).apply()
    }

    private fun loadData() {
        temperature = sharedPreferences.getString("temperature", "N/A") ?: "N/A"
        humidity = sharedPreferences.getString("humidity", "N/A") ?: "N/A"
        airQuality = sharedPreferences.getString("airQuality", "N/A") ?: "N/A"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sharedPreferences = getSharedPreferences("HomiePrefs", Context.MODE_PRIVATE)
        loadData()

        val bluetoothManager = getSystemService(BluetoothManager::class.java)
        val bluetoothAdapter = bluetoothManager?.adapter

        enableEdgeToEdge()
        setContent {
            HomieAppPreviewTheme {
                HomieAppPreviewUI(
                    temperature = temperature,
                    humidity = humidity,
                    airQuality = airQuality,
                    connected = isConnected,
                    bluetoothAdapter = bluetoothAdapter,
                    onConnectClick = {
                        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED) {
                            conectarDispositivo()
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun HomieAppPreviewUI(
    temperature: String,
    humidity: String,
    airQuality: String,
    connected: Boolean,
    bluetoothAdapter: BluetoothAdapter?,
    onConnectClick: () -> Unit
) {
    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        HomieAppPreviewLayout(
            connected = connected,
            temperature = temperature,
            humidity = humidity,
            airQuality = airQuality,
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

    val temperatureData = if (temperature != "N/A") "${temperature}°C" else "N/A"
    val humidityData = if (humidity != "N/A") "${humidity}%" else "N/A"
    val airQualityData = if (airQuality != "N/A") {
            when (airQuality) {
                "4" -> "Ideal"
                "3" -> "Good"
                "2" -> "Fair"
                else -> "Poor"
            }
        }
        else "N/A"

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
                                Toast.makeText(context, "Searching...", Toast.LENGTH_SHORT).show()
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
                text = if (connected) "Connected to $foundDeviceName" else if (foundDeviceName.isNotEmpty()) "Found $foundDeviceName" else "Disconnected",
                color = if (connected) colorResource(R.color.green_homie) else colorResource(R.color.alert_color),
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Column(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
            DataRow(painterResource(R.drawable.device_thermostat), "Temperature: ", temperatureData, R.color.blue_homie)
            DataRow(painterResource(R.drawable.humidity), "Humidity: ", humidityData, R.color.blue_homie)
            DataRow(painterResource(R.drawable.air_quality), "Air Quality: ", airQualityData, R.color.blue_homie)
        }
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
        ) {
            var dommy by remember { mutableStateOf(R.drawable.happy_dommy) }
            var advise by remember { mutableStateOf("All seems to be good!!!") }


            if (airQuality == "1") {
                dommy = R.drawable.dead_dommy
                advise = "Your house is in extreme danger"
            } else if (airQuality == "2" || humidity > "65" || temperature > "40") {
                dommy = R.drawable.afraid_dommy
                advise = "You'd better check your home"
            } else {
                dommy = R.drawable.happy_dommy
                advise = "All seems to be good!!!"
            }


            Image(
                painter = painterResource(dommy),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(150.dp)
            )
            Box() {
                Image(
                    painter = painterResource(R.drawable.dialogue),
                    contentDescription = null,
                    modifier = Modifier.fillMaxWidth()
                )
                Text(
                    text = advise,
                    color = Color.Black,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 8.dp, start = 32.dp, end = 28.dp)
                )
            }

        }

    }
}

@Composable
fun DataRow(painterResource: Painter, type: String, data: String, color: Int) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp).height(56.dp)
            .clip(RoundedCornerShape(8.dp)).background(MaterialTheme.colorScheme.surface),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painter = painterResource, contentDescription = null,
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
        HomieAppPreviewUI(
            temperature = "25",
            humidity = "60",
            airQuality = "4",
            connected = true,
            bluetoothAdapter = null,
            onConnectClick = {}
        )
    }
}
