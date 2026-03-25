#include <Wire.h>
#include <SPI.h>
#include <Adafruit_Sensor.h>
#include "Adafruit_BME680.h"
#include <Arduino.h>
#include <BLEDevice.h>
#include <BLEUtils.h>
#include <BLEServer.h>
#include <BLE2902.h>
#include <stdio.h>
#include <iostream>


//Variables
int temperature;
int humidity;
int gas_resistance;
int alertData;
int AQI;
float hum_score;
float gas_score;

const int buzzer = 5;

//Definitions
#define SERVICE_UUID        "12345678-1234-1234-1234-1234567890ab"
#define CHARACTERISTIC_TX   "12345678-1234-1234-1234-1234567890ac"
#define CHARACTERISTIC_RX   "dcba4321-1234-1234-1234-abcdef654321"


Adafruit_BME680 MainSensor;

BLECharacteristic *pTxCharacteristic;
bool deviceConnected = false;
bool sendData = true;

class MyServerCallbacks: public BLEServerCallbacks {
    void onConnect(BLEServer* pServer) {
        deviceConnected = true;
        Serial.println("Cliente conectado");
    }

    void onDisconnect(BLEServer* pServer) {
      deviceConnected = false;
      Serial.println("Cliente desconectado");

      BLEDevice::startAdvertising();
      Serial.println("Reiniciando advertising...");
}
};

class MyCallbacks: public BLECharacteristicCallbacks {
    void onWrite(BLECharacteristic *pCharacteristic) {
        String value = pCharacteristic->getValue().c_str();

        if (value.length() > 0) {
            Serial.print("Comando recibido: ");
            Serial.println(value.c_str());

            if (value == "CMD:START") {
                sendData = true;
            }
            if (value == "CMD:STOP") {
                sendData = false;
            }
        }
    }
};

void setup() {
    delay(2000);
    Serial.begin(115200);
    Wire.begin(6, 7);
    if (!MainSensor.begin()) {
        Serial.println("No se encontró un sensor BME680, revise la conexión.");
        delay(1000);
        while (1);
    } else Serial.println("Sensor BME680 inicializado correctamente.");

    MainSensor.setTemperatureOversampling(BME680_OS_16X);
    MainSensor.setHumidityOversampling(BME680_OS_16X);
    MainSensor.setIIRFilterSize(BME680_FILTER_SIZE_3);
    MainSensor.setGasHeater(320, 150);

    pinMode(buzzer, OUTPUT);
    noTone(buzzer);

    BLEDevice::init("HMMB000001");

    BLEServer *pServer = BLEDevice::createServer();
    pServer->setCallbacks(new MyServerCallbacks());

    BLEService *pService = pServer->createService(SERVICE_UUID);

    pTxCharacteristic = pService->createCharacteristic(
                        CHARACTERISTIC_TX,
                        BLECharacteristic::PROPERTY_NOTIFY
                      );

    BLECharacteristic *pRxCharacteristic = pService->createCharacteristic(
                        CHARACTERISTIC_RX,
                        BLECharacteristic::PROPERTY_WRITE
                      );

                      pTxCharacteristic->addDescriptor(new BLE2902());

    pRxCharacteristic->setCallbacks(new MyCallbacks());

    pService->start();

    BLEAdvertising *pAdvertising = BLEDevice::getAdvertising();
    pAdvertising->addServiceUUID(SERVICE_UUID);
    pAdvertising->start();

    Serial.println("Esperando conexión...");
}

void loop() {
    if (!MainSensor.performReading()) {
    Serial.println("Failed to perform reading :(");
    delay(1000);
    while (1) {}
    }

    temperature = MainSensor.temperature;
    humidity = MainSensor.humidity;
    gas_resistance = MainSensor.gas_resistance;

    AQI = map(gas_resistance, 0, 50000, 500, 0);

    Serial.print("Temperatura: ");
    Serial.println(temperature);

    Serial.print("Humedad: ");
    Serial.println(humidity);

    Serial.print("Resistencia de gas: ");
    Serial.println(gas_resistance);

    Serial.print("AQI: ");
    Serial.println(AQI);

    if (deviceConnected && sendData) {

        String temperatureData = "TEMP:" + String(temperature);
        String humidityData = "HUM:" + String(humidity);
        String AQIData = "AQI:" + String(AQI);

        pTxCharacteristic->setValue(temperatureData.c_str());
        pTxCharacteristic->notify();
        Serial.println(temperatureData);

        pTxCharacteristic->setValue(humidityData.c_str());
        pTxCharacteristic->notify();
        Serial.println(humidityData);

        pTxCharacteristic->setValue(AQIData.c_str());
        pTxCharacteristic->notify();
        Serial.println(AQIData);

    }
    if (AQI > 300) {
        tone(buzzer, 2700);
        delay(500);
        noTone(buzzer);
        delay(500);
    } else {
        noTone(buzzer);
        delay(1000);
    }
}
