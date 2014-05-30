package lab6;

import lab2.Car;
import lejos.nxt.Button;
import lejos.nxt.ButtonListener;
import lejos.nxt.LCD;
import lejos.nxt.LightSensor;
import lejos.nxt.SensorPort;

public class LightFollower2 {
	private static LightSensor leftSensor = new LightSensor(SensorPort.S1);
	private static LightSensor rightSensor = new LightSensor(SensorPort.S4);
	
	private static float max = 500;
	private static float min = 150;
	
	private static float minPower = 60;
	
	private static ButtonListener t = new ButtonListener() {
		
		@Override
		public void buttonReleased(Button b) {
			// TODO Auto-generated method stub
			
		}
		
		@Override
		public void buttonPressed(Button b) {
			System.exit(1);				
		}
	};

	public static void main(String[] args) throws InterruptedException {
		LCD.drawString("left:  ", 0, 0);
		LCD.drawString("right: ", 0, 1);
		
		Button.ESCAPE.addButtonListener(t);
		
		float leftRaw;
		float rightRaw;
		float leftPower;
		float rightPower;
		float leftAverage = 0;
		float rightAverage = 0;
		float alpha = 0.01f;
		
		while(!Button.ESCAPE.isDown()){
			leftRaw = SensorPort.S1.readRawValue();
			rightRaw = SensorPort.S4.readRawValue();
			
			LCD.drawInt((int)leftRaw, 4, 9, 0);
			LCD.drawInt((int)rightRaw, 4, 9, 1);
			
			if(normalize(leftRaw) > leftAverage){
				leftPower = minPower + (100-minPower)*normalize(leftRaw);
			} else{
				leftPower = 0;
			}
			
			if(normalize(rightRaw) > rightAverage){
				rightPower = minPower + (100-minPower)*normalize(rightRaw);				
			} else{
				rightPower = 0;
			}

			leftAverage = alpha*normalize(leftRaw) + (1f-alpha)*leftAverage;
			rightAverage = alpha*normalize(rightRaw) + (1f-alpha)*rightAverage;
			
			Car.forward((int)leftPower, (int)rightPower);
			
			
			Thread.sleep(20);
		}
		

	}
	
	private static float normalize(float raw){
		float res = 1-(raw - min)/(max - min);
		if(res > 1) return 1;
		if(res < 0) return 0;
		return res;
	}
}