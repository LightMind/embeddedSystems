package lab4;

import lab2.Car;
import lejos.nxt.Button;
import lejos.nxt.LCD;
import lejos.nxt.SensorPort;

public class PIDLineFollower {

	public static void main(String[] args) {
		BlackWhiteGreenSensor2 bw = new BlackWhiteGreenSensor2(SensorPort.S3);
		bw.calibrate();
		
		long timeGreen = 0;
		
		float proportionalGain = 0.7f;
		float integralGain = 0.001f;
		float derivativeGain = 1.2f;
		
		float integral = 0;
		float lastError = 0;
		float derivative = 0;
		float error = 0;
		float result = 0;
		
		int minPower = 60;
		
		int dt = 10;
		

		while(!Button.ESCAPE.isDown()){
			
			LCD.drawInt(bw.error(), 3, 1, 6);
			
			error = bw.error();			
			integral += error*dt;
			derivative = (error-lastError)/(float)dt;	
			
			result = error*proportionalGain + integral*integralGain + derivativeGain*derivative;
			
			
			if(bw.black() || bw.white()){
				timeGreen = 0;
			}
			
			if(bw.green()){
				if(timeGreen > 500){
					Car.stop();
				}
				timeGreen+=dt;
			}	else {
				
				Car.forward(minPower - (int)result, minPower + (int)result);
				
			}
			
			lastError = error;

			try {
				Thread.sleep((int)dt);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

	}

}
