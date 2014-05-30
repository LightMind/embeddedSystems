package lab6;

import lab2.Car;
import lejos.nxt.*;

public class SoundFollower {
	private static SoundSensor soundSensor = new SoundSensor(SensorPort.S4);
	
	private static float max = 200;
	private static float min = 0;
	
	private static float minPower = 40;
	
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
		LCD.drawString("Raw: ", 0, 0);
		
		Button.ESCAPE.addButtonListener(t);
		
		float soundValueRaw;
		float power;
		
		while(!Button.ESCAPE.isDown()){
			soundValueRaw = SensorPort.S4.readRawValue();
			
			LCD.drawInt(1024-(int)soundValueRaw, 4, 0, 1);
			
			float value = widen(soundValueRaw);
			
			if(value < 0){
				power = minPower + (100-minPower)*(-value);
				Car.backward((int)power, (int)power);
			} else{
				power = minPower + (100-minPower)*value;
				Car.forward((int)power, (int)power);
			}
			
			Thread.sleep(20);
		}
		

	}
	
	private static float normalize(float raw){
		float res = 1-(raw - min)/(max - min);
		if(res > 1) return 1;
		if(res < 0) return 0;
		return res;
	}
	
	private static float widen(float raw){
		return 2*normalize(raw) - 1;
	}

}
