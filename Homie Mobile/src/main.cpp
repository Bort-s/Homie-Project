#include <Arduino.h>
#include <Wire.h>
#include <SPI.h>
#include <Adafruit_Sensor.h>
#include "Adafruit_BME680.h"


//Variables
double temperature;
double humidity;
double pressure;
double gas_resistance;
double altitude;

#define SEALEVELPRESSURE_HPA (1012.5)

Adafruit_BME680 MainSensor;

void setup() {
  Serial.begin(115200);

  if (!MainSensor.begin()) {
	Serial.println("Could not find a valid BME680 sensor");
	while (1);
  } else {
    Serial.println("BME680 sensor Inicialized!");
  }

  MainSensor.setTemperatureOversampling(BME680_OS_16X);
  MainSensor.setHumidityOversampling(BME680_OS_16X);
  MainSensor.setPressureOversampling(BME680_OS_16X);
  MainSensor.setIIRFilterSize(BME680_FILTER_SIZE_15);
  MainSensor.setGasHeater(320, 100);
}

void loop() {
  if (!MainSensor.performReading()) {
    Serial.println("Failed to perform reading :(");
    return;
  }

  temperature = MainSensor.temperature;
  humidity = MainSensor.humidity;
  pressure = MainSensor.pressure;
  gas_resistance = MainSensor.gas_resistance;
  altitude = MainSensor.readAltitude(SEALEVELPRESSURE_HPA);


  Serial.print("Temperature = ");
  Serial.print(temperature);
  Serial.println(" *C");

  Serial.print("Pressure = ");
  Serial.print(pressure / 100.0);
  Serial.println(" hPa");

  Serial.print("Humidity = ");
  Serial.print(humidity);
  Serial.println(" %");

  Serial.print("Gas = ");
  Serial.print(gas_resistance / 1000.0);
  Serial.println(" KOhms");

  Serial.print("Approx. Altitude = ");
  Serial.print(altitude);
  Serial.println(" m");

  delay(2000);
}
