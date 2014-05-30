package lab3;

import lejos.nxt.Button;
import lejos.nxt.LCD;
import lejos.nxt.SensorPort;
import lejos.nxt.SoundSensor;
import lejos.nxt.UltrasonicSensor;

public class SonicSensorTest {
	public static void main(String[] args) throws Exception {

		SoundSensor us = new SoundSensor(SensorPort.S1);

		LCD.drawString("Volume (??) ", 0, 0);

		while (!Button.ESCAPE.isDown()) {
			LCD.drawInt(us.readValue(), 3, 13, 0);

			Thread.sleep(5);
		}
		LCD.clear();
		LCD.drawString("Program stopped", 0, 0);
		Thread.sleep(2000);
	}
}
