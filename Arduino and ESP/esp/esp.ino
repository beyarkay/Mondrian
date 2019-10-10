void setup() {
  // put your setup code here, to run once:
  Serial.begin(115200);
  Serial.println("s");
}

void loop() {
  // put your main code here, to run repeatedly:
  Serial.println("h");
  delay(1000);
  Serial.println("l");
  delay(500);
}
