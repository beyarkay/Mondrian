#define PIN_DEBUG 2


void setup() {
  Serial.begin(115200);
  Serial.println("s");
  pinMode(PIN_DEBUG, OUTPUT);
}

void loop() {
  Serial.println("h");
  digitalWrite(PIN_DEBUG, HIGH);
  delay(1000);
  Serial.println("l");
  digitalWrite(PIN_DEBUG, LOW);
  delay(2000);
}
