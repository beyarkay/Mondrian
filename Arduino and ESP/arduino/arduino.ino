bool setupComplete = false;

void setup() {
  // put your setup code here, to run once:
  Serial.begin(115200);
  pinMode(LED_BUILTIN, OUTPUT);

}

void loop() {

  // put your main code here, to run repeatedly:
  char command = readByte();
  if (command == 's') {
    setupComplete = true;
  } else if (command == 'h' && setupComplete) {
    digitalWrite(LED_BUILTIN, HIGH);
  } else if (command == 'l' && setupComplete) {
    digitalWrite(LED_BUILTIN, LOW);
  }
}


int readByte() {
  /*
     Wait until a byte is available, then read and return it
  */
  byte incomingByte = '\n';
  while (incomingByte == '\n') {
    while (Serial.available() == 0);
    incomingByte = (char) Serial.read();
  }
  //  Serial.println(incomingByte);
  return incomingByte;
}
