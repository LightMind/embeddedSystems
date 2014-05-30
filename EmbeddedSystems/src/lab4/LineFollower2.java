package lab4;
import lab2.Car;
import lejos.nxt.*;

public class LineFollower2 {
	public static void main(String[] aArg) throws Exception {
		final int power = 80;

		BlackWhiteGreenSensor sensor = new BlackWhiteGreenSensor(SensorPort.S3);

		sensor.calibrate();

		LCD.clear();
		LCD.drawString("Light: ", 0, 2);
		
		long timeInGreen = 0;

		while (!Button.ESCAPE.isDown()) {

			LCD.drawInt(sensor.light(), 4, 10, 2);
			LCD.refresh();

			if (sensor.black()) {
				Car.forward(power, 0);
				timeInGreen=0;
			} else if(sensor.white()){				
				Car.forward(0, power);
				timeInGreen = 0;
			} else if(sensor.green()){	
				if(timeInGreen > 150){
					Car.stop();
				}
				timeInGreen+=10;
			} else {
				Car.forward(0,power);
			}

			Thread.sleep(10);
		}

		Car.stop();
		LCD.clear();
		LCD.drawString("Program stopped", 0, 0);
		LCD.refresh();
	}
}
