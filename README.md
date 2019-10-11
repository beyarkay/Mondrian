# Mondrian

A robot to draw on a flat surface, with local positioning done 
through a moving phone camera


# Code Upload instructions

* Write code for the ESP8266
* Change board to 'Generic ESP8266 Module'
* Compile the code
* Connect the UNO to the computer
* Connect the UNO to the ESP8266: 
    * UNO Tx --> ESP Tx
    * UNO Rx --> ESP Rx
* Click 'Upload' on the Arduino IDE
* Then do this sequence to the following pins
    * ESP RESET --> LOW
    * ESP GPIO0 --> LOW
    * ESP RESET --> XXX
    * ESP GPIO0 --> XXX
* You might have to try this compile-button sequence a few times
before you see something like this:
```Connecting........_____....._____....._____.
Chip is ESP8266EX
Features: WiFi
...
```
* After this, push and release the RESET button, and all should 
be good
* Possible Errors: 
    * TODO: Add errors

* For the NANO, upload nano.ino, but make sure 
'ATMega 382P (Old Bootloader)' is selected





Last Updated: 10 October 2019
