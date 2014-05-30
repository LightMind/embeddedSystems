package lab4;

import lejos.nxt.Button;
import lejos.nxt.LCD;
import lejos.nxt.SensorPort;

public class Exercise1 {

	public static void main(String[] arags) {
		BlackWhiteSensor bw = new BlackWhiteSensor(SensorPort.S3);
		bw.calibrate();

		while (!Button.ESCAPE.isDown()) {
			if (bw.black()) {
				LCD.drawString("Black detected", 1, 5, false);
			} else if (bw.white()) {
				LCD.drawString("White detected", 1, 5, false);
			} else {
				LCD.drawString("Not sure      ", 1, 5, false);
			}

		}
	}
}
