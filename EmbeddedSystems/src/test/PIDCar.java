package test;

import lab2.Car;
import lejos.nxt.Button;
import lejos.nxt.ButtonListener;
import lejos.nxt.LCD;
import lejos.nxt.LightSensor;
import lejos.nxt.SensorPort;
import lejos.util.Datalogger;

public class PIDCar {
	static int Kc = 130;
	static float dT = 0.0015f;  //0,0015 sek per loop
	static int Pc = 1; 			//1 sekund
	
	static float Kp = 0.6f*Kc; // REMEMBER we are using Kp*100 so this is really 10 //
	static float Ki = (2*Kp*dT)/Pc; // REMEMBER we are using Ki*100 so this is really 1 //

	static float Kd = (Kp*Pc)/(8*dT); // REMEMBER we are using Kd*100 so this is really 100//
	static int offset = 45; // Initialize the variables
	static int Tp = 70;
	static int integral = 0; // the place where we will store our integral
	static int lastError = 0; // the place where we will store the last error value
	static int derivative = 0; // the place where we will store the derivative

	
	
	
	
	
	public static void main (String[] args) {
	
		//final DataLogger dl = new DataLogger("Sample.txt");
		
		Button.ESCAPE.addButtonListener(new ButtonListener() {
			@Override
			public void buttonReleased(Button b) {
				//dl.close();
				System.exit(1);
			}
			
			@Override
			public void buttonPressed(Button b) {
			}
		});
		
		LightSensor ls = new LightSensor(SensorPort.S2);
		
		while (true) {
			
		   int lightValue = ls.readValue();     // what is the current light reading?
		   int error = lightValue - offset;     // calculate the error by subtracting the offset
		   integral = integral + error;         // calculate the integral 
		   derivative = error - lastError;  	// calculate the derivative
		   float turn = Kp*error + Ki*integral + Kd*derivative;  // the "P term" the "I term" and the "D term"
		   turn = turn/100f;                // REMEMBER to undo the affect of the factor of 100 in Kp, Ki and Kd//
		   int powerA = Tp + (int)turn;                 // the power level for the A motor
		   int powerC = Tp - (int)turn;                // the power level for the C motor
		   
		   LCD.drawString("Power A: " + powerA, 0, 0);
		   LCD.drawString("Power C: " + powerC, 0, 1);
		   
		   Car.forward(powerA, powerC);
		  // dl.writeSample(error);
		   lastError = error ;                 // save the current error so it can be the lastError next time around
		  
		}
		
	}
}
