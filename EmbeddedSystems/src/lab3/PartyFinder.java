package lab3;

import lab2.Car;
import lejos.nxt.Button;
import lejos.nxt.ButtonListener;
import lejos.nxt.LCD;
import lejos.nxt.SensorPort;
import lejos.nxt.SoundSensor;

public class PartyFinder {
	private static SoundSensor soundLeft = new SoundSensor(SensorPort.S4);
	private static SoundSensor soundRight = new SoundSensor(SensorPort.S1);
	
	public static void main(String[] args) throws Exception {
		LCD.drawString("left:  ", 0, 0);
		LCD.drawString("right: ", 0, 1);
		LCD.refresh();
		
		ButtonListener t = new ButtonListener() {
			
			@Override
			public void buttonReleased(Button b) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void buttonPressed(Button b) {
				System.exit(1);				
			}
		};
		
		Button.ESCAPE.addButtonListener(t);

		int leftSoundValue;
		int rightSoundValue;
		
		float ratio;
		int minPower = 10;
		int val = 100 - minPower;
		
		float leftPower;
		float rightPower;
		
		while (!Button.ESCAPE.isDown()) {
			leftSoundValue = soundLeft.readValue()+5;
			rightSoundValue = soundRight.readValue();
			LCD.drawInt(leftSoundValue, 4, 8, 0);
			LCD.drawInt(rightSoundValue, 4, 8, 1);
			
			ratio = (float)leftSoundValue/(float)(rightSoundValue+leftSoundValue);
			
			leftPower = 25f+(float)Math.pow(minPower, 2.2f-ratio);
			rightPower = 25f+(float)Math.pow(minPower, 1.2f+ratio);
						
			Car.forward((int)leftPower, (int)rightPower);
			
			Thread.sleep(20);
		}
		Car.stop();
		LCD.clear();
		LCD.drawString("Program stopped", 0, 0);
		Thread.sleep(2000);
	}
}
