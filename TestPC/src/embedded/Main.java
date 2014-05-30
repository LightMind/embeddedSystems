package embedded;

import lejos.nxt.Button;
import lejos.nxt.LCD;

public class Main {

	public static void main(String[] args) {
		System.out.println("test");
		LCD.drawString("Hello", 30, 40);
		Button.waitForAnyPress();
	}

}
