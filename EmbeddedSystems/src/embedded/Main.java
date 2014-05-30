package embedded;

import lejos.nxt.Button;
import lejos.nxt.LCD;
import lejos.nxt.Motor;
import lejos.nxt.SensorPort;
import lejos.nxt.TouchSensor;

public class Main {
	
	public static float maxSpeed = 770f;
	
	public static void main(String[] args) throws InterruptedException {
		TouchSensor t1  =new TouchSensor(SensorPort.S1);
		TouchSensor t2  =new TouchSensor(SensorPort.S2);
		
        System.out.println("wait for button");
        float maxSpeedB = Motor.B.getMaxSpeed();
        float maxSpeedC = Motor.C.getMaxSpeed();
        System.out.println("Max speed B = " + maxSpeedB);
        System.out.println("Max speed C = " + maxSpeedC);
        Motor.B.setSpeed(maxSpeedB/2);
        Motor.C.setSpeed(maxSpeedC/2);
        Button.waitForAnyPress();
               
        while(true){
        	if(t1.isPressed()){
        		Motor.B.forward();
        	} else {
        		Motor.B.stop();
        	}
        	
        	if(t2.isPressed()){
        		Motor.C.forward();
        	} else {
        		Motor.C.stop();
        	}
        	Thread.sleep(2);
        	if((Button.readButtons() & Button.ID_ENTER) > 0){
        		break;
        	}
        }
        
	}
	
	public static void moveSome(){
		System.out.println("Speeding!");
        Motor.B.setSpeed(maxSpeed/10);
        Motor.C.setSpeed(maxSpeed/10);
        Motor.B.forward();
        Motor.C.forward();
	}
}
