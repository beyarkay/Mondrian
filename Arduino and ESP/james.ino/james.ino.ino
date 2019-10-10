
const String WIFI_NETWORK = "_Network_Name_";
const String WIFI_PASSWORD = "_Wifi_Password";
void setup() {
  Serial.begin(115200);
  
  // Make sure the ESP is working
  Serial.println("AT");           // output: OK
  
  //Check which mode the ESP is in right now
  Serial.println("AT+CWMODE?");   // output: [mode, depending on the current setup]

  // Get our IP address (and thereby check if ESP is connected to an AP)
  Serial.println("AT+CIFSR");     // output: IP address or error

  // Connect to the wifi network using WIFI_NETWORK and WIFI_PASSWORD
  Serial.print("AT+CWJAP= “");
  Serial.print(WIFI_NETWORK);
  Serial.print("”,“");
  Serial.print(WIFI_PASSWORD);
  Serial.println("”");    // output: OK

  // Get our IP address again (and thereby check that we connected to the AP)
  Serial.println("AT+CIFSR");     // output: IP address or error

  // Configure for multiple connections to make the ESP a Server
  // AT+CIPMUX=1 is multiple cxns, AT+CIPMUX=0 is one cxn
  Serial.println("AT+CIPMUX=1");    


  // Start a server at port 80 (default for HTTP)
  //    -in open-server mode (1) or closed-server mode (0)
  Serial.println("AT+CIPSERVER=1,80");

  // Through channel 0, send 5 characters
  // This will start a prompt that'll accept 5 characters, then send them ?to a buffer?
  Serial.println("AT+CIPSEND=0,5");

  // Close the connection on channel 0. ?Only after closing the connection will the bytes be sent?
  Serial.println("AT+CIPCLOSE=0");

  
  

  
  /*
   * Modes for operation:
   * AT+CWMODE=[mode]
   * Station = 1 
   * Access Point = 2
   * Both = 3
   */
   Serial.println("AT+CWMODE=1")  // Allow the ESP to connect to an AP-
}

void loop() {
  // put your main code here, to run repeatedly:

}
