#include <Adafruit_NeoPixel.h>
#ifdef __AVR__
  #include <avr/power.h>
#endif


#include "I2Cdev.h"

#include "MPU6050_6Axis_MotionApps20.h"

#if I2CDEV_IMPLEMENTATION == I2CDEV_ARDUINO_WIRE
    #include "Wire.h"
#endif

MPU6050 mpu;

#define OUTPUT_READABLE_YAWPITCHROLL
#define LED_PIN 13 // (Arduino is 13, Teensy is 11, Teensy++ is 6)
bool blinkState = false;

float initialZiro = 0;
float ziroValue=0;
// MPU control/status vars
bool dmpReady = false;  // set true if DMP init was successful
uint8_t mpuIntStatus;   // holds actual interrupt status byte from MPU
uint8_t devStatus;      // return status after each device operation (0 = success, !0 = error)
uint16_t packetSize;    // expected DMP packet size (default is 42 bytes)
uint16_t fifoCount;     // count of all bytes currently in FIFO
uint8_t fifoBuffer[64]; // FIFO storage buffer

// orientation/motion vars
Quaternion q;           // [w, x, y, z]         quaternion container
VectorInt16 aa;         // [x, y, z]            accel sensor measurements
VectorInt16 aaReal;     // [x, y, z]            gravity-free accel sensor measurements
VectorInt16 aaWorld;    // [x, y, z]            world-frame accel sensor measurements
VectorFloat gravity;    // [x, y, z]            gravity vector
float euler[3];         // [psi, theta, phi]    Euler angle container
float ypr[3];           // [yaw, pitch, roll]   yaw/pitch/roll container and gravity vector

// packet structure for InvenSense teapot demo
uint8_t teapotPacket[14] = { '$', 0x02, 0,0, 0,0, 0,0, 0,0, 0x00, 0x00, '\r', '\n' };


volatile bool mpuInterrupt = false;     // indicates whether MPU interrupt pin has gone high
void dmpDataReady() {
    mpuInterrupt = true;
}

void setupMPU6050() {
    // join I2C bus (I2Cdev library doesn't do this automatically)
    #if I2CDEV_IMPLEMENTATION == I2CDEV_ARDUINO_WIRE
        Wire.begin();
        TWBR = 24; // 400kHz I2C clock (200kHz if CPU is 8MHz)
    #elif I2CDEV_IMPLEMENTATION == I2CDEV_BUILTIN_FASTWIRE
        Fastwire::setup(400, true);
    #endif

    Serial.begin(9600);
 //   while (!Serial); // wait for Leonardo enumeration, others continue immediately

    // initialize device
    Serial.println(F("Initializing I2C devices..."));
    mpu.initialize();

    // verify connection
    Serial.println(F("Testing device connections..."));
    Serial.println(mpu.testConnection() ? F("MPU6050 connection successful") : F("MPU6050 connection failed"));

    // wait for ready
    Serial.println(F("\nSend any character to begin DMP programming and demo: "));

    // load and configure the DMP
    Serial.println(F("Initializing DMP..."));
    devStatus = mpu.dmpInitialize();

    // supply your own gyro offsets here, scaled for min sensitivity
    mpu.setXGyroOffset(220);
    mpu.setYGyroOffset(76);
    mpu.setZGyroOffset(-85);
    mpu.setZAccelOffset(1788); // 1688 factory default for my test chip

    // make sure it worked (returns 0 if so)
    if (devStatus == 0) {
        // turn on the DMP, now that it's ready
        Serial.println(F("Enabling DMP..."));
        mpu.setDMPEnabled(true);

        // enable Arduino interrupt detection
        Serial.println(F("Enabling interrupt detection (Arduino external interrupt 0)..."));
        attachInterrupt(0, dmpDataReady, RISING);
        mpuIntStatus = mpu.getIntStatus();

        // set our DMP Ready flag so the main loop() function knows it's okay to use it
        Serial.println(F("DMP ready! Waiting for first interrupt..."));
        dmpReady = true;

        // get expected DMP packet size for later comparison
        packetSize = mpu.dmpGetFIFOPacketSize();
    } else {
        // ERROR!
        // 1 = initial memory load failed
        // 2 = DMP configuration updates failed
        // (if it's going to break, usually the code will be 1)
        Serial.print(F("DMP Initialization failed (code "));
        Serial.print(devStatus);
        Serial.println(F(")"));
    }

    // configure LED for output
    pinMode(LED_PIN, OUTPUT);
}

#define PIN 6
#define NUMPIXELS 80

// Parameter 1 = number of pixels in strip
// Parameter 2 = Arduino pin number (most are valid)
// Parameter 3 = pixel type flags, add together as needed:
//   NEO_KHZ800  800 KHz bitstream (most NeoPixel products w/WS2812 LEDs)
//   NEO_KHZ400  400 KHz (classic 'v1' (not v2) FLORA pixels, WS2811 drivers)
//   NEO_GRB     Pixels are wired for GRB bitstream (most NeoPixel products)
//   NEO_RGB     Pixels are wired for RGB bitstream (v1 FLORA pixels, not v2)
Adafruit_NeoPixel strip = Adafruit_NeoPixel(NUMPIXELS, PIN, NEO_GRB + NEO_KHZ800);

// IMPORTANT: To reduce NeoPixel burnout risk, add 1000 uF capacitor across
// pixel power leads, add 300 - 500 Ohm resistor on first pixel's data input
// and minimize distance between Arduino and first pixel.  Avoid connecting
// on a live circuit...if you must, connect GND first.

void setup() {
  // This is for Trinket 5V 16MHz, you can remove these three lines if you are not using a Trinket
  #if defined (__AVR_ATtiny85__)
    if (F_CPU == 16000000) clock_prescale_set(clock_div_1);
  #endif
  // End of trinket special code

  strip.begin();
  strip.show(); // Initialize all pixels to 'off'

  setupMPU6050();
}

//KJK
int nBaseDegree = 0;
int nReCalcGyroDegree = 0;
//   ledOn(ledwhere);

int nData = 0;
int inputVal=0;
int i,k, flag=1, flag2;

int ledwhere=0;
void theaterChase_rand() ;
bool bFinish = false;

void loop() {
  // put your main code here, to run repeatedly:
  loopMPU6050();

  // Serial.println(nData);

  // Some example procedures showing how to display to the pixels:
  if (IsExit() == false) {
    if(flag)
      initialize();
    else{
      //Serial -> LED ON
      inputVal = Serial.read();
    
      switch(inputVal)
      {
        case 'L' : 
        
          nBaseDegree += 45;
          if(nBaseDegree >= 360) nBaseDegree = nBaseDegree - 360;
            /*
            ledwhere=ledwhere-1;
            if(ledwhere<0)
              ledwhere=7;
            ledOn(ledwhere);
            changeDirection(2); 
            if(ledwhere>7)
              ledwhere=0;
            ledOn(ledwhere);
            */
          break; //L
        case 'S' : 
          bFinish = false;
        
          nBaseDegree = 0;
          flag=1;
          //initialZiro=ziroValue;
         // nBaseDegree = nReCalcGyroDegree;
          //ledwhere=ledwhere;
         // ledOn(ledwhere);
          break; //S
        case 'R' : 
          nBaseDegree -= 45;
          if(nBaseDegree < 0) nBaseDegree = 360 + nBaseDegree;
          
            //ledwhere=ledwhere+1;
            //if(ledwhere>7)
            //  ledwhere=0;
            //ledOn(ledwhere);
            //changeDirection(0); 
            //if(ledwhere<0)
            //  ledwhere=7;
            //ledOn(ledwhere);
          break; //R
         case 'A' :
         bFinish = true;          
          break;
      }
    }
  }

  if (IsExit() == true) {
    ClearLED();
    flag=1;
  }  
}

bool IsExit()
{
  nData = analogRead(A0);
  if (nData <= 100)
  {
    return true;
  }
  return false;
}

void initialize(){
  
  if(IsExit() == false) theaterChase(strip.Color(127, 127, 127), 50); // White
  if(IsExit() == false) theaterChase(strip.Color(127, 60, 0), 50); // Red
  if(IsExit() == false) theaterChase(strip.Color(0, 60, 127), 50); // Blue

  //initialZiro=ziroValue;
  flag=0;
  flag2=1;
}

void theaterChase(uint32_t c, uint8_t wait) {
  for (int j=0; j<10; j++) {  //do 10 cycles of chasing
    for (int q=0; q < 3; q++) {
      for (int i=0; i < strip.numPixels(); i=i+3) {
        strip.setPixelColor(i+q, c);    //turn every third pixel on
        if(IsExit() == true) return; 
      }
      strip.show();
    
    if (IsExit() == true) return;

      delay(wait);

      for (int i=0; i < strip.numPixels(); i=i+3) {
        strip.setPixelColor(i+q, 0);        //turn every third pixel off
      }
    }
  }
}
void ledOn(int val){
  for(i=0;i<val*10;i++){
    strip.setPixelColor(i, strip.Color(0,150,150));
    strip.show();
  }
  for(i;i<val*10+10;i++){
    strip.setPixelColor(i, strip.Color(150,0,150));
    strip.show();
  }
  for(i;i<NUMPIXELS;i++){
    strip.setPixelColor(i, strip.Color(0,150,150));
    strip.show();
  }
}

// Fill the dots one after the other with a color
void ClearLED() {
  for (uint16_t i = 0; i<strip.numPixels(); i++) {
    strip.setPixelColor(i, 0);
    strip.show();
  }
}

void loopMPU6050() {
    // if programming failed, don't try to do anything
    if (!dmpReady) return;

    // wait for MPU interrupt or extra packet(s) available
    while (!mpuInterrupt && fifoCount < packetSize) {
        // other program behavior stuff here
        // .
        // .
        // .
        // if you are really paranoid you can frequently test in between other
        // stuff to see if mpuInterrupt is true, and if so, "break;" from the
        // while() loop to immediately process the MPU data
        // .
        // .
        // .
    }

    // reset interrupt flag and get INT_STATUS byte
    mpuInterrupt = false;
    mpuIntStatus = mpu.getIntStatus();

    // get current FIFO count
    fifoCount = mpu.getFIFOCount();

    // check for overflow (this should never happen unless our code is too inefficient)
    if ((mpuIntStatus & 0x10) || fifoCount == 1024) {
        // reset so we can continue cleanly
        mpu.resetFIFO();
        Serial.println(F("FIFO overflow!"));

    // otherwise, check for DMP data ready interrupt (this should happen frequently)
    } else if (mpuIntStatus & 0x02) {
        // wait for correct available data length, should be a VERY short wait
        while (fifoCount < packetSize) fifoCount = mpu.getFIFOCount();

        // read a packet from FIFO
        mpu.getFIFOBytes(fifoBuffer, packetSize);
        
        // track FIFO count here in case there is > 1 packet available
        // (this lets us immediately read more without waiting for an interrupt)
        fifoCount -= packetSize;

        #ifdef OUTPUT_READABLE_QUATERNION
            // display quaternion values in easy matrix form: w x y z
            mpu.dmpGetQuaternion(&q, fifoBuffer);
            Serial.print("quat\t");
            Serial.print(q.w);
            Serial.print("\t");
            Serial.print(q.x);
            Serial.print("\t");
            Serial.print(q.y);
            Serial.print("\t");
            Serial.println(q.z);
        #endif

        #ifdef OUTPUT_READABLE_EULER
            // display Euler angles in degrees
            mpu.dmpGetQuaternion(&q, fifoBuffer);
            mpu.dmpGetEuler(euler, &q);
            Serial.print("euler\t");
            Serial.print(euler[0] * 180/M_PI);
            Serial.print("\t");
            Serial.print(euler[1] * 180/M_PI);
            Serial.print("\t");
            Serial.println(euler[2] * 180/M_PI);
        #endif


         if( bFinish == true) 
         {
            theaterChase_rand();
         }
         else
         {
        #ifdef OUTPUT_READABLE_YAWPITCHROLL
            // display Euler angles in degrees
            mpu.dmpGetQuaternion(&q, fifoBuffer);
            mpu.dmpGetGravity(&gravity, &q);
            mpu.dmpGetYawPitchRoll(ypr, &q, &gravity);
      
//            Serial.print("ypr\t");
//            Serial.print(ypr[0] * 180/M_PI);
//            Serial.print("\t    ");
//            Serial.print(ypr[1] * 180/M_PI);
//            Serial.print("\t");
//            Serial.println(ypr[2] * 180/M_PI);
//
//            delay(200);

              ziroValue = ypr[0] * 180 / M_PI;

              nReCalcGyroDegree = ziroValue;
              if(nReCalcGyroDegree < 0) nReCalcGyroDegree = 360 + nReCalcGyroDegree;
              int nTempDegree = 0;     
              int nCell = 0;
              if( nReCalcGyroDegree > nBaseDegree)
              {
                nTempDegree = nReCalcGyroDegree - nBaseDegree;
              }
              else
              {
                nTempDegree = nReCalcGyroDegree + 360 - nBaseDegree;
              }
              
              nCell = nTempDegree * 8 / 360;

                if(IsExit() == false) 
                {
                  ledOn(nCell);
                }

              
              Serial.print(" \n------------");
             Serial.print(nCell);
              Serial.print(" / ");
             Serial.println(nTempDegree);
              /*
              if(flag2)
              {
                if(k>10 && k<50)
                {
                  initialZiro += ziroValue;
                }else if(i>=50)
                {
                  initialZiro /= 10.;
                  flag2=0;
                }
                k++;
              }
              */
         //     Serial.println(" \n ziroValue    ");
        //      Serial.println(ziroValue);
        //      Serial.println(" \n nBaseDegree    ");
         //     Serial.println(nBaseDegree);
              
//              Serial.print("ypr\t");
//              Serial.println(ypr[0] * 180 / M_PI);


        #endif
         }

        #ifdef OUTPUT_READABLE_REALACCEL
            // display real acceleration, adjusted to remove gravity
            mpu.dmpGetQuaternion(&q, fifoBuffer);
            mpu.dmpGetAccel(&aa, fifoBuffer);
            mpu.dmpGetGravity(&gravity, &q);
            mpu.dmpGetLinearAccel(&aaReal, &aa, &gravity);
            Serial.print("areal\t");
            Serial.print(aaReal.x);
            Serial.print("\t");
            Serial.print(aaReal.y);
            Serial.print("\t");
            Serial.println(aaReal.z);
        #endif

        #ifdef OUTPUT_READABLE_WORLDACCEL
            // display initial world-frame acceleration, adjusted to remove gravity
            // and rotated based on known orientation from quaternion
            mpu.dmpGetQuaternion(&q, fifoBuffer);
            mpu.dmpGetAccel(&aa, fifoBuffer);
            mpu.dmpGetGravity(&gravity, &q);
            mpu.dmpGetLinearAccel(&aaReal, &aa, &gravity);
            mpu.dmpGetLinearAccelInWorld(&aaWorld, &aaReal, &q);
            Serial.print("aworld\t");
            Serial.print(aaWorld.x);
            Serial.print("\t");
            Serial.print(aaWorld.y);
            Serial.print("\t");
            Serial.println(aaWorld.z);
        #endif
    
        #ifdef OUTPUT_TEAPOT
            // display quaternion values in InvenSense Teapot demo format:
            teapotPacket[2] = fifoBuffer[0];
            teapotPacket[3] = fifoBuffer[1];
            teapotPacket[4] = fifoBuffer[4];
            teapotPacket[5] = fifoBuffer[5];
            teapotPacket[6] = fifoBuffer[8];
            teapotPacket[7] = fifoBuffer[9];
            teapotPacket[8] = fifoBuffer[12];
            teapotPacket[9] = fifoBuffer[13];
            Serial.write(teapotPacket, 14);
            teapotPacket[11]++; // packetCount, loops at 0xFF on purpose
        #endif

        // blink LED to indicate activity
        blinkState = !blinkState;
        digitalWrite(LED_PIN, blinkState);
    }
}

void changeDirection(int dirValue)
{
  while(1)
  {
    loopMPU6050();
    
    if(dirValue==0) //Right
    {
      Serial.print("Come in Right");
      float tempZiro;
      float leftTempZiro, rightTempZiro;
      Serial.print("\n init  ");
      Serial.print(initialZiro);
      Serial.print("\n ziroValue  ");
      Serial.print(ziroValue);
      
      if(initialZiro>=135){
        Serial.print("\n initialZiro-ziroValue  ");
        Serial.print(ziroValue-initialZiro);
        if(ziroValue-initialZiro>45-360){
          ledwhere=ledwhere-1;
          initialZiro=ziroValue;
          break;
        }
      }
      else{
        Serial.print("\n ziroValue-initialZiro  ");
        Serial.print(ziroValue-initialZiro);
        if(ziroValue-initialZiro>45){
            ledwhere=ledwhere-1;
            initialZiro=ziroValue;
          break;
        }
      }
    }
    else //Left
    {
      Serial.print("Come in Left");
      float tempZiro;
      float leftTempZiro, rightTempZiro;
      Serial.print("\n init  ");
      Serial.print(initialZiro);
      Serial.print("\n ziroValue  ");
      Serial.print(ziroValue);

      if(initialZiro<=-135){
        Serial.print("\n initialZiro-ziroValue  ");
        Serial.print(ziroValue-initialZiro);
        if(ziroValue-initialZiro<-45+360){
          ledwhere=ledwhere+1;
          initialZiro=ziroValue;
          break;
        }
      }
      else{
        Serial.print("\n ziroValue-initialZiro  ");
        Serial.print(ziroValue-initialZiro);
        if(ziroValue-initialZiro<-45){
            ledwhere=ledwhere+1;
            initialZiro=ziroValue;
          break;
        }
      }
      
    }
  }
}

void theaterChase_rand() 
{  
  for(int j=0;j<5;j++)
  {
     for (int i=0; i < strip.numPixels(); i++) {
      int nR = random(0,255);
      int nG = random(0,255);
      int nB = random(0,255);
        strip.setPixelColor(i, strip.Color(nR, nG, nB));    //turn every third pixel on
        
        if(IsExit() == true) return;
      }
      strip.show();
      delay(500);
  }   
    if (IsExit() == true) return;
}
