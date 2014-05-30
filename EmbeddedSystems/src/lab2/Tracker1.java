package lab2;

import lejos.nxt.*;
/**
 * A LEGO 9797 car with a sonar sensor. The sonar is used
 * to maintain the car at a constant distance 
 * to objects in front of the car.
 * 
 * The sonar sensor should be connected to port 1. The
 * left motor should be connected to port C and the right 
 * motor to port B.
 * 
 * @author  Ole Caprani
 * @version 24.08.08
 */
public class Tracker1
{
  public static void main (String[] aArg)
  throws Exception
  {
     UltrasonicSensor us = new UltrasonicSensor(SensorPort.S1);
     final int  noObject = 255;
     int distance,
         desiredDistance = 35, // cm
         lastDistance = 0,
         power, 
         minPower = 60;
     float error = 0, previousError;
     float integral = 0, integralGain = 0.0005f;
     float derivative, derivativeGain = 0.05f;
     float gain = 0.05f;
     float dt = 330f;
	  
     LCD.drawString("Distance: ", 0, 1);
     LCD.drawString("Power:    ", 0, 2);
     LCD.drawString("integral: ", 0, 3);
     LCD.drawString("Derivate: ", 0, 4);
     LCD.drawString("error:    ", 0, 5);
     
     lastDistance = us.getDistance();
     previousError = lastDistance - desiredDistance;
	   
     while (! Button.ESCAPE.isDown())
     {		   
         distance = us.getDistance();
		 
         if ( distance != noObject ) 
         {
             error = distance - desiredDistance;
             integral = integral + error*dt;
             //integral = Math.min(integral, 250);
             //integral = Math.max(integral, -250);
             
             derivative = (error - previousError)/dt;
             power = (int)(gain * error + integralGain*integral + derivativeGain*derivative);
             if ( power > 0 )
             { 
                 power = Math.min(minPower + power,100);
                 Car.forward(power,power);
                 LCD.drawString("Forward ", 0, 3);
             }
             else 
             {
                 power = Math.min(minPower + Math.abs(power),100);
                 Car.backward(power, power);
		         LCD.drawString("Backward", 0, 3);
		    	 
             }
             LCD.drawInt(distance,4,10,1);
             LCD.drawInt(power, 4,10,2);
             LCD.drawInt((int)integral,4,10,3);
             LCD.drawInt((int)derivative,4,10,4);
             LCD.drawInt((int)error,4,10,5);
             
             
		 }
         else
             Car.forward(100, 100);
		 previousError = error;
         Thread.sleep(300);
     }
	 
     Car.stop();
     LCD.clear();
     LCD.drawString("Program stopped", 0, 0);
     Thread.sleep(2000);
   }
}
