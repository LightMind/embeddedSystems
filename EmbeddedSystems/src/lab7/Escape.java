package lab7;

import lejos.nxt.SensorPort;
import lejos.nxt.TouchSensor;
import lejos.nxt.UltrasonicSensor;
import lejos.util.Delay;

public class Escape extends Thread {

	private SharedCar car = new SharedCar();

	private int power = 70, ms = 2000;
	private TouchSensor left = new TouchSensor(SensorPort.S2);
	private TouchSensor right = new TouchSensor(SensorPort.S3);
	
	private boolean l,r;
	
	
	int frontDistance, leftDistance, rightDistance;
	int stopThreshold = 30;
	
    public Escape(SharedCar car)
    {
       this.car = car;		   
    }
	
	@Override
	public void run() {
		while(true){
			l = left.isPressed();
			r = right.isPressed();
			
			while(!l && !r){
				car.noCommand();
				Delay.msDelay(ms);
				l = left.isPressed();
				r = right.isPressed();
			}
			
			if(l && r){
				car.backward(power, power);
				Delay.msDelay(ms);
				car.forward(power, 0);	
				Delay.msDelay(2250);
			} else if(l){
				car.backward(power, power);
				Delay.msDelay(ms);
				car.forward(0, power);	
				Delay.msDelay(ms);
			} else if(r){
				car.backward(power, power);
				Delay.msDelay(ms);
				car.forward(power, 0);	
				Delay.msDelay(ms);
			}
			
			car.stop();
			Delay.msDelay(500);
		}
		
	}

}
