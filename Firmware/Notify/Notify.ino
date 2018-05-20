  
#include <SoftwareSerial.h>
#include <LedControl.h>

const int numDevices = 4;      // number of MAX7219s used
const long scrollDelay = 10;   // adjust scrolling speed

unsigned long bufferLong [14] = {0}; 
//data-clk-cs
LedControl lc=LedControl(8,13,9,numDevices);

const unsigned char scrollText[] PROGMEM = {"Chiamata in arrivo da: #neverendingpolemica "};
    
SoftwareSerial mySerial(10,11);


#define key 12
#define btLed 13

String inString = "";
char inChar = "";
void setup() {
  //LEDMatrix Init
  for (int x=0; x<numDevices; x++){
    //Disable power saving mode
    lc.shutdown(x,false);
    // Set the brightness
    lc.setIntensity(x,1);
    // Clear the display
    lc.clearDisplay(x);
  }
  pinMode(key,OUTPUT);
  pinMode(btLed,OUTPUT);
  digitalWrite(key,HIGH);
  Serial.begin(57600);
  Serial.println("Type AT commands!");
  mySerial.begin(9600);

}

void loop() {
  // put your main code here, to run repeatedly:
  if(mySerial.available()) {
    digitalWrite(btLed,HIGH);
    while(mySerial.available()) {
      //leggo i valori ricevuti dal bluetooth
      inChar = mySerial.read();
      inString += inChar;
      //Serial.print(inChar);
      //trigger fine riga
      if((int)inChar == 47){
        inString.trim();
        int l = inString.length();
        inString.remove(l-1);
        Serial.println(inString);
        Serial.println("BackSlash found!");
      }      
    }
    digitalWrite(btLed,LOW);
  }
  scrollMessage(scrollText);
}

