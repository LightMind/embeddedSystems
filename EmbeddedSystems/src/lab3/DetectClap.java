package lab3;


import lab2.Car;
import lejos.nxt.Button;
import lejos.nxt.ButtonListener;
import lejos.nxt.LCD;
import lejos.nxt.SensorPort;
import lejos.nxt.SoundSensor;

public class DetectClap {
	private static SoundSensor sound = new SoundSensor(SensorPort.S1);
	
	

	private static void waitForClap() throws Exception {
		int soundLevel;
		long msSinceBelow50 = 0;
		long msSinceClap = 0;
		boolean isClapping = false;
		
		Thread.sleep(500);
		do {
			Thread.sleep(5);
			soundLevel = sound.readValue();
			if(soundLevel < 50 && !isClapping){
				msSinceBelow50 = 0;
			}
			else if(soundLevel > 85 && msSinceBelow50 <= 30 && !isClapping){
				isClapping = true;
				msSinceClap = 0;
			}
			else if(isClapping){
				msSinceClap += 5;
				if(soundLevel < 50 && msSinceClap <= 250){
					break;
				}
				else if(msSinceClap > 250){
					isClapping = false;
					msSinceBelow50 = 0;
					msSinceClap = 0;
				}
			}
			
			LCD.drawInt(soundLevel, 4, 10, 0);
			msSinceBelow50 += 5;
		} while (true);
	}

	public static void main(String[] args) throws Exception {
		LCD.drawString("dB level: ", 0, 0);
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

		while (!Button.ESCAPE.isDown()) {
			waitForClap();
			LCD.drawString("Forward ", 0, 1);
			Car.forward(100, 100);

			waitForClap();
			LCD.drawString("Right   ", 0, 1);
			Car.forward(100, 0);

			waitForClap();
			LCD.drawString("Left    ", 0, 1);
			Car.forward(0, 100);

			waitForClap();
			LCD.drawString("Stop    ", 0, 1);
			Car.stop();
		}
		Car.stop();
		LCD.clear();
		LCD.drawString("Program stopped", 0, 0);
		Thread.sleep(2000);
	}
}
