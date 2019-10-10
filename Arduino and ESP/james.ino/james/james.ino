const int rghtIn1 = 12;
const int rghtIn2 = 11;
const int leftIn1 = 10;
const int leftIn2 = 9;
const int enablePin = A6;

const int switchPin = 7;
const int potPin = 0;
boolean reverse = true;
int speed = 1000;

char incomingByte = '_';

void setup() {
  pinMode(leftIn1, OUTPUT);
  pinMode(leftIn2, OUTPUT);
  pinMode(rghtIn1, OUTPUT);
  pinMode(rghtIn2, OUTPUT);
  pinMode(enablePin, OUTPUT);
  Serial.begin(9600);
//  delay(1000); // Make sure you don't send anything while the Nano is starting up
  digitalWrite(enablePin, HIGH);
}

void loop() {

  //  char motor = readByte();
  //  char dir = readByte();
  //  motorControl(motor, dir);

  motorControl('l', '+');
  motorControl('r', '+');
  delay(1000);


  motorControl('l', '+');
  motorControl('r', '-');
  delay(1000);


  motorControl('l', '-');
  motorControl('r', '-');
  delay(1000);  
  
  motorControl('l', '-');
  motorControl('r', '+');
  delay(1000);

}


void motorControl(char motor, char dir) {
  if (dir == '+') {
    if (motor == 'l') {
      Serial.println("Left Forwards");
      digitalWrite(leftIn1, HIGH);
      digitalWrite(leftIn2, LOW);

    } else if (motor == 'r') {
      Serial.println("Right Forwards");
      digitalWrite(rghtIn1, HIGH);
      digitalWrite(rghtIn2, LOW);
    }
  } else if (dir == '-') {
    if (motor == 'l') {
      Serial.println("Left Backwards");
      digitalWrite(leftIn1, LOW);
      digitalWrite(leftIn2, HIGH);

    } else if (motor == 'r') {
      Serial.println("Right Backwards");
      digitalWrite(rghtIn1, LOW);
      digitalWrite(rghtIn2, HIGH);
    }
  }
}

int readByte() {
  /*
     Wait until a byte is available, then read and return it
  */
  incomingByte = '\n';
  while (incomingByte == '\n') {
    while (Serial.available() == 0);
    incomingByte = (char) Serial.read();
  }
  //  Serial.println(incomingByte);
  return incomingByte;
}
