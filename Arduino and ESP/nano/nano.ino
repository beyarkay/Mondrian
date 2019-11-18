/*
   TOUBLESHOOTING

   Is 'Arduino Nano' selected in Tools>Board, and not the ESP?
   Is 'ATmega328P (Old Bootloader)' selected in Tools>Processor
*/


const int PIN_LEFT_1 = 4;
const int PIN_LEFT_2 = 5;
const int PIN_RIGHT_3 = 6;
const int PIN_RIGHT_4 = 7;

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
  Serial.println(command);
  cmd_motors(command);
  if (command == '+') {
    digitalWrite(LED_BUILTIN, HIGH);
  } else if (command == '-') {
    digitalWrite(LED_BUILTIN, LOW);
  }
}


void cmd_motors(char cmd) {
  if (cmd == 'l') {
    digitalWrite(PIN_LEFT_1, LOW);
    digitalWrite(PIN_LEFT_2, LOW);
    digitalWrite(PIN_RIGHT_3, LOW);
    digitalWrite(PIN_RIGHT_4, HIGH);
  } else if (cmd == 'r') {
    digitalWrite(PIN_LEFT_1, HIGH);
    digitalWrite(PIN_LEFT_2, LOW);
    digitalWrite(PIN_RIGHT_3, LOW);
    digitalWrite(PIN_RIGHT_4, LOW);
  } else if (cmd == 'f') {
    digitalWrite(PIN_LEFT_1, HIGH);
    digitalWrite(PIN_LEFT_2, LOW);
    digitalWrite(PIN_RIGHT_3, LOW);
    digitalWrite(PIN_RIGHT_4, HIGH);
  } else if (cmd == 'b') {
    digitalWrite(PIN_LEFT_1, LOW);
    digitalWrite(PIN_LEFT_2, HIGH);
    digitalWrite(PIN_RIGHT_3, HIGH);
    digitalWrite(PIN_RIGHT_4, LOW);
  } else if (cmd == 'h') {
    digitalWrite(PIN_LEFT_1, LOW);
    digitalWrite(PIN_LEFT_2, LOW);
    digitalWrite(PIN_RIGHT_3, LOW);
    digitalWrite(PIN_RIGHT_4, LOW);

  } else if (cmd == '1') {
    digitalWrite(PIN_LEFT_1, HIGH);
    digitalWrite(PIN_LEFT_2, LOW);
    digitalWrite(PIN_RIGHT_3, LOW);
    digitalWrite(PIN_RIGHT_4, LOW);
  } else if (cmd == '2') {
    digitalWrite(PIN_LEFT_1, LOW);
    digitalWrite(PIN_LEFT_2, HIGH);
    digitalWrite(PIN_RIGHT_3, LOW);
    digitalWrite(PIN_RIGHT_4, LOW);
  } else if (cmd == '3') {
    digitalWrite(PIN_LEFT_1, LOW);
    digitalWrite(PIN_LEFT_2, LOW);
    digitalWrite(PIN_RIGHT_3, HIGH);
    digitalWrite(PIN_RIGHT_4, LOW);
  } else if (cmd == '4') {
    digitalWrite(PIN_LEFT_1, LOW);
    digitalWrite(PIN_LEFT_2, LOW);
    digitalWrite(PIN_RIGHT_3, LOW);
    digitalWrite(PIN_RIGHT_4, HIGH);
  } else {
    Serial.print("Unknown Command: '");
    Serial.print(cmd);
    Serial.println("'");
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
