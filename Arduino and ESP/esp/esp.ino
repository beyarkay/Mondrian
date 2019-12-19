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

   HOW TO ACCESS DEBUG LOG
   
   run the following command:
   nc 192.168.4.1 90

   you should see a friendly greeting from James
   note that for some reason it sometimes takes a while to connect
*/

#include <ESP8266WiFi.h>
#include <ESP8266WebServer.h>

#include <ESP8266mDNS.h>
#include <WiFiUdp.h>
#include <ArduinoOTA.h>

const char* ssid = "riversong";
const char* password = "melodypond";

const unsigned int map_updates_port = 8888;             //port to listen for UDP map updates on
const unsigned int log_server_port = 90;                //port to listen for TCP debug connections on
const unsigned int remote_control_web_server_port = 80; //web server port for remote control commands
const unsigned int OTA_updates_port = 8266;             //port for OTA updates server

const int MAX_MARKERS = 127;
const int LOG_BUFFER_SIZE = 500;

struct marker {
  double x, y, rotation;
};

char log_buffer[LOG_BUFFER_SIZE];
int log_buffer_start = 0;
int log_buffer_end = 0;

//Note "markers" and "marker_seen" are indexed by marker ID, but "incoming_markers" and "incoming_marker_ids" are not
byte incoming_markers_size = 0; //Number of markers received in the last packet (currently in the incoming_markers and incoming_marker_ids arrays)
byte incoming_marker_ids[MAX_MARKERS]; //ID of each incoming marker
marker incoming_markers[MAX_MARKERS]; //Parallel array to incoming_marker_ids

//Below are 2 arrays that aren't currently used anywhere in the code
boolean marker_seen[MAX_MARKERS]; //for future use - indexed by ID
marker markers[MAX_MARKERS];  //for future use - indexed by ID

byte packetBuffer[UDP_TX_PACKET_MAX_SIZE + 1]; //buffer to hold incoming packet for map updates

WiFiUDP Udp;
WiFiServer log_server(log_server_port);
WiFiClient log_client; // currently connected logging client
ESP8266WebServer server(remote_control_web_server_port);

String makeHtmlForm(String state) {
  String form = String("<meta name=\"viewport\" content=\"initial-scale=1.0, maximum-scale=1.0, user-scalable=no\" />") +
                "<form action='led'>" +
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
                "<a href='/serial?data=f'>Send '/serial?data=f'</a>" +
                "<br />" +
                "<a href='/serial?data=b'>Send '/serial?data=b'</a>" +
                "<br />" +
                "<a href='/serial?data=r'>Send '/serial?data=r'</a>" +
                "<br />" +
                "<a href='/serial?data=l'>Send '/serial?data=l'</a>" +
                "<br />" +
                "<a href='/serial?data=h'>Send '/serial?data=h'</a>";
  return form;
}

void handleSerial() {
  // Get the input from the client
  String data = server.arg("data"); //http ://<ip address>/serial?data=thisissomedata
  char cmd = 'h';
  if (data.length() > 0)
    cmd = data.charAt(0);

  if (cmd == 'r') {
    digitalWrite(0, LOW);
    digitalWrite(1, HIGH);
    digitalWrite(2, LOW);
    digitalWrite(3, HIGH);
  } else if (cmd == 'l') {
    digitalWrite(0, HIGH);
    digitalWrite(1, LOW);
    digitalWrite(2, HIGH);
    digitalWrite(3, LOW);
    
  } else if (cmd == 'f') {
    digitalWrite(0, HIGH);
    digitalWrite(1, LOW);
    digitalWrite(2, LOW);
    digitalWrite(3, HIGH);
  } else if (cmd == 'b') {
    digitalWrite(0, LOW);
    digitalWrite(1, HIGH);
    digitalWrite(2, HIGH);
    digitalWrite(3, LOW);
  } else if (cmd == 'h') {
    digitalWrite(0, LOW);
    digitalWrite(1, LOW);
    digitalWrite(2, LOW);
    digitalWrite(3, LOW);
  } else if (cmd == '0') {
    digitalWrite(0, HIGH);
    digitalWrite(1, LOW);
    digitalWrite(2, LOW);
    digitalWrite(3, LOW);
  } else if (cmd == '1') {
    digitalWrite(0, LOW);
    digitalWrite(1, HIGH);
    digitalWrite(2, LOW);
    digitalWrite(3, LOW);
  } else if (cmd == '2') {
    digitalWrite(0, LOW);
    digitalWrite(1, LOW);
    digitalWrite(2, HIGH);
    digitalWrite(3, LOW);
  } else if (cmd == '3') {
    digitalWrite(0, LOW);
    digitalWrite(1, LOW);
    digitalWrite(2, LOW);
    digitalWrite(3, HIGH);
  }
  // Send a confirmation response to the client
  server.send(200, "text/html", makeHtmlForm(data));
}

//void handleLed() {
//  String state = server.arg("state");
//  if (state == "on") {  //http ://<ip address>/led?state=on
//    digitalWrite(LED_BUILTIN, HIGH);
//  }
//  else if (state == "off") {  // http ://<ip address>/led?state=off
//    digitalWrite(LED_BUILTIN, LOW);
//  }
//  server.send(200, "text/html", makeHtmlForm(state));
//}\


void setup() {
  pinMode(0, OUTPUT);
  pinMode(1, OUTPUT);
  pinMode(2, OUTPUT);
  pinMode(3, OUTPUT);

  digitalWrite(0, LOW);
  digitalWrite(1, LOW);
  digitalWrite(2, LOW);
  digitalWrite(3, LOW);

  WiFi.softAP(ssid, password);             // Start the access point
  ArduinoOTA.setPort(OTA_updates_port);
  ArduinoOTA.begin();

  Udp.begin(map_updates_port);
  log_server.begin();

  // Setup the endpoints for the server:
  server.on("/", []() {
    server.send(200, "text/html", makeHtmlForm("unknown"));
  });
//  server.on("/led", handleLed);
  server.on("/serial", handleSerial);

  server.begin();
}

void loop() {
  ArduinoOTA.handle();
  server.handleClient();
  handleLogServer();
  handleMapUpdates();
}

void handleMapUpdates() {
  // if there's data available, read a packet
  int packetSize = Udp.parsePacket();
  if (packetSize) {
    // read the packet into packetBufffer
    int n = Udp.read(packetBuffer, UDP_TX_PACKET_MAX_SIZE);
    packetBuffer[n] = 0;

    byte* pointer = packetBuffer;

    int time;
    memcpy(&time, pointer, 4);                  pointer += 4;
    memcpy(&incoming_markers_size, pointer, 1); pointer += 1;

    logf("Received packet time: %d, markers: %d\n", time,  incoming_markers_size);

    for (byte i = 0; i < incoming_markers_size; i++) {
      memcpy(&incoming_marker_ids[i], pointer, 1);       pointer += 1;
      memcpy(&incoming_markers[i].x, pointer, 8);        pointer += 8;
      memcpy(&incoming_markers[i].y, pointer, 8);        pointer += 8;
      memcpy(&incoming_markers[i].rotation, pointer, 8); pointer += 8;
      
      logf("Marker id: %d, x: %f, y: %f, rotation: %f\n", incoming_marker_ids[i], incoming_markers[i].x, incoming_markers[i].y, incoming_markers[i].rotation);
    }
  }
}

void handleLogServer() {
  WiFiClient new_client = log_server.available();
  if (new_client) {
    if (log_client.connected()) {
      new_client.println("hi, you've reached James.\nSorry I can't take this connection right now, someone else is still using the debug connection.");
      new_client.stop();
    } else {
      log_client = new_client;
      log_client.println("hi, this is James.");
    }
  }

  //Send everything currently in the log buffer
  if (log_client.connected()) {
    while (log_buffer_start != log_buffer_end) {
      log_client.print(log_buffer[log_buffer_start]);
      log_buffer_start = (log_buffer_start + 1) % LOG_BUFFER_SIZE;
    }
    log_client.flush();
  }
}

/**
   Copied from the printf function at: https://github.com/esp8266/Arduino/blob/master/cores/esp8266/Print.cpp
*/
void logf(const char *format, ...) {
  va_list arg;
  va_start(arg, format);
  char temp[64];
  char* buffer = temp;
  size_t len = vsnprintf(temp, sizeof(temp), format, arg);
  va_end(arg);
  if (len > sizeof(temp) - 1) {
    buffer = new char[len + 1];
    if (!buffer) {
      return;
    }
    va_start(arg, format);
    len = vsnprintf(buffer, len + 1, format, arg);
    va_end(arg);
  }
  log(buffer);
  if (buffer != temp) {
    delete[] buffer;
  }
}

void log(char s[]) {
  for (int i = 0; s[i] != 0; i++) {
    log_buffer[log_buffer_end] = s[i];
    log_buffer_end = (log_buffer_end + 1) % LOG_BUFFER_SIZE;

    //If the buffer is full, we're going to overwrite the oldest element next
    if (log_buffer_start == log_buffer_end) {
      log_buffer_start = (log_buffer_start + 1) % LOG_BUFFER_SIZE;
    }
  }
}
