/*
   Steps for uploading to ESP8266

   1a. Change Tools>Board to 'Generic ESP8266 Module'
   1b. Change Tools>Port to be the correct port
   1c. Compile changes
   2a. Connect USB to Arduino Nano
   2b. Connect Nano RST pin to GND
   2c. Connect ESP Rx --> Nano Rx
   2d. Connect ESP Tx --> Nano Tx
   2e. Disconnect IO2 from everything.
   3a. Hold ESP RST to GND and ESP IO0 to GND
   3b. Click upload on Arduino IDE
   3c. Wait for Arduino IDE output to read "Compiling"
   3d. Release ESP RST
   3e. Release ESP IO0
   4a. On a successful connection, you'll see something like:
  ```
  Chip is ESP8266EX
  Features: WiFi
  MAC: 2c:f4:32:0f:95:e2
  Uploading stub...
  Running stub...
  Stub running...
  Configuring flash size...
  Auto-detected Flash size: 1MB
  Flash params set to 0x0320
  Compressed 295648 bytes to 211834...

  Writing at 0x00000000... (7 %)
  Writing at 0x00004000... (15 %)
  Writing at 0x00008000... (23 %)
  Writing at 0x0000c000... (30 %)
  Writing at 0x00010000... (38 %)
  Writing at 0x00014000... (46 %)
  Writing at 0x00018000... (53 %)
  Writing at 0x0001c000... (61 %)
  Writing at 0x00020000... (69 %)
  Writing at 0x00024000... (76 %)
  Writing at 0x00028000... (84 %)
  Writing at 0x0002c000... (92 %)
  Writing at 0x00030000... (100 %)
  Wrote 295648 bytes (211834 compressed) at 0x00000000 in 18.8 seconds (effective 125.6 kbit/s)...
  Hash of data verified.

  Leaving...
  Hard resetting via RTS pin...
  ```
   4b. It is extremely finicky, so you'll have to repeat steps 3a --> 3e a couple times, usually 10 or 15
   5a. after you see these two lines:
  ```
  Leaving...
  Hard resetting via RTS pin...
  ```
   5b. Make sure ESP IO0 isn't connected to ground
   5c. Connect ESP RST to ground, then release it
   6.  The ESP will restart and run the code that you've just uploaded

   TROUBLESHOOTING

   If the ESP crashes the Exception Cause will be shown and the current stack will be dumped.
      This website has a list of exceptions and their hex number:
          https://arduino-esp8266.readthedocs.io/en/latest/exception_causes.html
      The tool EspExceptionDecoder (https://github.com/me-no-dev/EspExceptionDecoder) works to
          decode the ESP8266 Stack dump into readable error messages
*/

#include <ESP8266WiFi.h>
#include <ESP8266WebServer.h>

const boolean DEBUG = false;
const char* ssid = "riversong";
const char* password = "melodypond";
int numStations = 0;
boolean shouldPrint = true;


ESP8266WebServer server(80);

String makeHtmlForm(String state) {
  String form = String("<form action='led'>") +
                "<input type='radio' name='state' value='on' checked>Turn On" +
                "<br /><br />" +
                "<input type='radio' name='state' value='off'>Turn Off" +
                "<br /><br />" +
                "<input type='submit' value='Submit'>" +
                "</form>" +
                "<p>Status: " +
                state +
                "</p>" +
                "<br />" +
                "<a href='/serial?data=somedata'>Send '/serial?data=somedata'</a>";
  return form;
}

void handleSerial() {
  // Get the input from the client
  String data = server.arg("data"); //http ://<ip address>/serial?data=thisissomedata
  // Send that data off to the nano, over the Serial
  Serial.println(data);
  // Send a confirmation response to the client
  server.send(200, "text/html", makeHtmlForm(data));
}

void handleLed() {
  String state = server.arg("state");
  if (state == "on") {  //http ://<ip address>/led?state=on
    digitalWrite(LED_BUILTIN, HIGH);
  }
  else if (state == "off") {  // http ://<ip address>/led?state=off
    digitalWrite(LED_BUILTIN, LOW);
  }
  server.send(200, "text/html", makeHtmlForm(state));
}


void setup() {
  pinMode(LED_BUILTIN, OUTPUT);

  Serial.begin(115200);
  delay(10);
  WiFi.softAP(ssid, password);             // Start the access point

  if (DEBUG) {
    Serial.println('\n');
    Serial.print("Access Point '");
    Serial.print(ssid);
    Serial.println("' started");
  }
  // Setup the endpoints for the server:
  server.on("/", []() {
    server.send(200, "text/html", makeHtmlForm("unknown"));
  });
  server.on("/led", handleLed);
  server.on("/serial", handleSerial);

  server.begin();

  if (DEBUG) {
    Serial.print("HTTP server started at: http://");
    Serial.println(WiFi.softAPIP());
  }
}

void loop() {
  server.handleClient();

  if (numStations != WiFi.softAPgetStationNum()) {
    if (DEBUG) {
      Serial.print("\nNumber of connected stations: ");
      Serial.println(WiFi.softAPgetStationNum());
    }
    numStations = WiFi.softAPgetStationNum();
  }
  if (millis() % 2000 == 0 && shouldPrint && DEBUG) {
    Serial.print(".");
    shouldPrint = false;
  } else if (millis() % 2000 > 0) {
    shouldPrint = true;
  }
}
