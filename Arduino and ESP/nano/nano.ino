const int PIN_LEFT_1 = 4;
const int PIN_LEFT_2 = 5;
const int PIN_RIGHT_3 = 6;
const int PIN_RIGHT_4 = 7;


bool setupComplete = false;

void setup() {
  // put your setup code here, to run once:
  Serial.begin(115200);
  pinMode(LED_BUILTIN, OUTPUT);
  pinMode(PIN_LEFT_1, OUTPUT);
  pinMode(PIN_LEFT_2, OUTPUT);
  pinMode(PIN_RIGHT_3, OUTPUT);
  pinMode(PIN_RIGHT_4, OUTPUT);
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


void cmd_motors(char cmd) {
  if (cmd == 'l') {

  } else if (cmd == 'r') {

  } else if (cmd == 'f') {

  } else if (cmd == 'b') {

  } else if (cmd == 'h') {

  } else {
    Serial.print("Unknown Command: ");
    Serial.println(cmd, DEC);
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
