#include <Arduino.h>
#include <DHT.h>

//Variables
float humidity;
float temperature;
float mq135Value;
float mq135Voltage;
int R0;

#define mq135Pin 3
#define buzzerPin 5

//DHT22
#define DHTPIN 7
#define DHTTYPE DHT22
DHT dht(DHTPIN, DHTTYPE);

void setup() {
  Serial.begin(115200);
  dht.begin();

  ledcSetup(0, 2000, 8);
  ledcAttachPin(buzzerPin, 0);
  ledcWrite(0, 0);

  pinMode(mq135Pin, INPUT);
}

void loop() {
  humidity = dht.readHumidity();
  temperature = dht.readTemperature();

  Serial.print("Humidity: ");
  Serial.print(humidity);
  Serial.print(" %\t");

  Serial.print("Temperature: ");
  Serial.print(temperature);
  Serial.println(" *C");

  mq135Value = analogRead(mq135Pin);
  mq135Voltage = mq135Value * (5.0 / 1023.0);

  float RS = ((5.0 * 10.0) / mq135Voltage) - 10.0;
  float R0 = 10000.0;
  float ratio = RS / R0;
  float ppm = pow(10, ((log10(ratio) - 0.76) / -0.52));

  Serial.print("Gas Sensor Reading: ");
  Serial.println(mq135Value);

  Serial.print("Gas Sensor PPM: ");
  Serial.println(ppm);

  if (mq135Value > 1000) {
    Serial.println("Gas level is high! Activating buzzer.");
    ledcWrite(0, 127);
  } else {
    ledcWrite(0, 0);
  }

  delay(500);
}
