package lab10;

import lejos.nxt.Button;
import lejos.nxt.ButtonListener;
import lejos.nxt.LCD;
import lejos.nxt.Motor;
import lejos.nxt.SensorPort;
import lejos.nxt.TouchSensor;
import lejos.nxt.UltrasonicSensor;
import lejos.robotics.PressureDetector;
//import lejos.robotics.MirrorMotor;
import lejos.robotics.RegulatedMotor;
import lejos.robotics.subsumption.Arbitrator;
import lejos.robotics.subsumption.Behavior;

/**
 * Demonstration of the Behavior subsumption classes.
 * 
 * Requires a wheeled vehicle with two independently controlled motors connected
 * to motor ports A and C, and a touch sensor connected to sensor port 1 and an
 * ultrasonic sensor connected to port 3;
 * 
 * @author Brian Bagnall and Lawrie Griffiths, modified by Roger Glassey
 * 
 */
public class BumperCar {
	static RegulatedMotor leftMotor = Motor.A;
	static RegulatedMotor rightMotor = Motor.C;

	// Use these definitions instead if your motors are inverted
	// static RegulatedMotor leftMotor = MirrorMotor.invertMotor(Motor.A);
	// static RegulatedMotor rightMotor = MirrorMotor.invertMotor(Motor.C);

	public static void main(String[] args) {
		leftMotor.setSpeed(400);
		rightMotor.setSpeed(400);
		Behavior b1 = new DriveForward();
		Behavior b2 = new DetectWall();
		Behavior b3 = new Exit();
		Behavior[] behaviorList = {  b1, b2, b3 };
		Arbitrator arbitrator = new Arbitrator(behaviorList);
		LCD.drawString("Bumper Car", 0, 1);
		Button.waitForAnyPress();
		arbitrator.start();
	}
	
}

class DriveForward implements Behavior {

	private boolean _suppressed = false;

	public boolean takeControl() {
		return true; // this behavior always wants control.
	}

	public void suppress() {
		_suppressed = true;// standard practice for suppress methods
	}

	public void action() {
		LCD.clear();
		LCD.drawString("Driving", 0, 3);
		_suppressed = false;
		BumperCar.leftMotor.forward();
		BumperCar.rightMotor.forward();
		while (!_suppressed) {
			Thread.yield(); // don't exit till suppressed
		}
		BumperCar.leftMotor.stop();
		BumperCar.leftMotor.stop();
	}
}

class Exit implements Behavior {

	@Override
	public boolean takeControl() {
		return Button.ESCAPE.isDown();
	}

	@Override
	public void action() {
		LCD.clear();
		LCD.drawString("Exiting", 0, 3);
		System.exit(0);
	}

	@Override
	public void suppress() {
	
	}

}

class UltrasonicRunner implements Runnable {

	private int cachedDistance;
	private UltrasonicSensor sonar;
	
	public UltrasonicRunner(UltrasonicSensor sonar0){
		sonar=sonar0;
	}
	
	@Override
	public void run() {
		while(true){
			sonar.ping();
			cachedDistance = sonar.getDistance();
			try {
				Thread.sleep(20);
			} catch (InterruptedException e) {
				break;
			}
		}
	}
	
	public int getDistance(){
		return cachedDistance;
	}
	
}

class DetectWall implements Behavior {
	
	private TouchSensor touch;
	private UltrasonicSensor sonar;
	private UltrasonicRunner runner;
	
	public boolean supressed = false;
	
	public DetectWall() {
		touch = new TouchSensor(SensorPort.S1);
		sonar = new UltrasonicSensor(SensorPort.S3);	
		runner = new UltrasonicRunner(sonar);
		
		Thread t = new Thread(runner);
		t.start();
		t.setDaemon(true);
	}

	public boolean takeControl() {
		return touch.isPressed() || runner.getDistance() < 25;
	}

	public void suppress() {
		supressed = true;
	}

	public void action() {
		LCD.clear();
		LCD.drawString("Detected Wall", 0, 3);
		
		supressed = false;
		
		long time = System.currentTimeMillis();
		
		while(!supressed && System.currentTimeMillis()-time < 1000){
			BumperCar.leftMotor.backward();
			BumperCar.rightMotor.backward();
			
		}
		
		if(supressed){
			BumperCar.leftMotor.stop();
			BumperCar.rightMotor.stop();
			return;
		}
		
		BumperCar.leftMotor.rotate(-180, true);  // start Motor.A rotating backward
		BumperCar.rightMotor.rotate(-360, true); // rotate C farther to make the turn
		
		while(BumperCar.rightMotor.isMoving() && !supressed){
			//Thread.yield();
			if(touch.isPressed()){
				BumperCar.leftMotor.stop();
				BumperCar.rightMotor.stop();
				action();
				break;
			}
		}
		
		BumperCar.leftMotor.stop();
		BumperCar.rightMotor.stop();
	}

}
