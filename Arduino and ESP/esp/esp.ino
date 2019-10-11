#include <ESP8266WiFi.h>
#include <ESP8266WebServer.h>

const char* ssid = "riversong";
const char* password = "melodypond";


ESP8266WebServer server(80);



String makeHtmlForm(String state) {
  String form = String("<form action='led'>") +
                "<input type='radio' name='state' value='on' checked>Turn On" +
                "</br></br>" +
                "<input type='radio' name='state' value='off'>Turn Off" +
                "</br></br>" +
                "<input type='submit' value='Submit'>" +
                "</form>" +
                "<p>Status: " +
                state +
                "</p>" +
                "</br></br>" +
                "<a href='/serial?data=somedata'>clickity clickity</a>";
  return form;
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

void handleSerial() {
  String data = server.arg("data"); //http ://<ip address>/serial?data=thisissomedata
  Serial.println(data);
  server.send(200, "text/html", makeHtmlForm("Data sent: " + data));

  
}


void setup() {
  pinMode(LED_BUILTIN, OUTPUT);

  Serial.begin(115200);
  delay(10);
  Serial.println('\n');

  WiFi.softAP(ssid, password);             // Start the access point
  Serial.print("Access Point \"");
  Serial.print(ssid);
  Serial.println("\" started");
  Serial.print("IP address:\t");
  Serial.println(WiFi.softAPIP());         // Send the IP address of the ESP8266 to the computer



  // Setup the endpoints for the server:
  server.on("/", []() {
    server.send(200, "text/html", makeHtmlForm("unknown"));
  });
  server.on("/led", handleLed);
  server.on("/serial", handleSerial);

  server.begin();
  Serial.println("HTTP server started");

}

void loop() {
  server.handleClient();

  //  Serial.println("h");
  //  digitalWrite(LED_BUILTIN, HIGH);
  //  delay(1000);
  //  Serial.println("l");
  //  digitalWrite(LED_BUILTIN, LOW);
  //  delay(2000);
}
