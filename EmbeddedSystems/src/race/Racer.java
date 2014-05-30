package race;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;

import embedded.DataLogger;
import lejos.nxt.Button;
import lejos.nxt.ButtonListener;
import lejos.nxt.LCD;
import lejos.nxt.LightSensor;
import lejos.nxt.Motor;
import lejos.nxt.MotorPort;
import lejos.nxt.SensorPort;

public class Racer {

	LightSensor leftLight = new LightSensor(SensorPort.S2);
	LightSensor rightLight = new LightSensor(SensorPort.S1);

	MotorPort leftMotor = MotorPort.C;
	MotorPort rightMotor = MotorPort.A;

	int forward = MotorPort.FORWARD;
	int backward = MotorPort.BACKWARD;	
	
	int white = 500;
	int black = 300;
	
	int dt = 10;
	
	float targetValue = -1;
	
	int minPower = 75;
	float pGain = 0.05f;
	float iGain = 0f;
	float dGain = 0f;
	
	float integralFactor = 0.95f;
	
	int LEFT = 1;
	int RIGHT= 0;
	

	float errorLeft = 0;
	float errorRight = 0;
	float integralLeft = 0f;
	float integralRight = 0f;
	float lastErrorLeft = 0;
	float lastErrorRight = 0;		
	float derivateLeft = 0f;
	float derivateRight = 0f;
	
	
	DataLogger dl = new DataLogger("errors");
	
	public void computePID(float ku, float pu){
		pGain = 0.6f *ku;
		iGain = 2 * pGain /pu;
		dGain = pGain*pu/8f;
	}
	
	public void drive(int leftPower, int rightPower) {
		if (leftPower < 0) {
			leftMotor.controlMotor(Math.abs(leftPower), backward);
		} else {
			leftMotor.controlMotor(leftPower, forward);			
		}
		
		if (rightPower < 0){
			rightMotor.controlMotor(Math.abs(rightPower), backward);
		} else {
			rightMotor.controlMotor(rightPower, forward);
		}
	}

	public void test() throws InterruptedException, IOException {
		setupEscapeButton();		
		setupLog();		
	//	setupDerivateControl();
		//setupGainControl();
		//setupIntegralFactorControl();
		setupTurnControl();
		computePID(0.6f, 0.7f);		
		System.out.println("start");
		Button.ENTER.waitForPressAndRelease();
		
		
		// pid
		
		
		int lastBlack = 0;
		
		int state = 0;
		
		boolean bothWhite = false;
		
		//dl.start();
		
		float dtSeconds = dt/1000f;
		
		float distance = 0f;
		leftMotor.resetTachoCount();
		rightMotor.resetTachoCount();
		
		while(true){
			
			distance = (leftMotor.getTachoCount() + rightMotor.getTachoCount())/2;
			
			LCD.drawString("tacho: " + distance + "  ", 0, 0);
			
			
			
			
			
			leftMotor.resetTachoCount();
			int leftValue = leftLight.readValue();
			int rightValue = rightLight.readValue();
			
			if(Math.abs(rightValue-black) < 10){
				lastBlack = RIGHT;
			} else if(Math.abs(leftValue-black) < 10){
				lastBlack = LEFT;
			}
			
			if(Math.abs(rightValue-white) < 4 && Math.abs(leftValue-white) < 4){
				bothWhite  = true;
			} else {
				bothWhite = false;
			}
			
			// white = 55
			// black = 33
			
			errorLeft =  targetValue - leftValue  ;
			errorRight = targetValue - rightValue;
			
			integralLeft += (float) (errorLeft*dtSeconds);
			integralRight += (float) (errorRight*dtSeconds);
						
			//dl.writeSample((int)errorLeft);
			
			derivateLeft = (errorLeft - lastErrorLeft)/dtSeconds;
			derivateRight = (errorRight - lastErrorRight)/dtSeconds;
			
			int resultLeft = (int) (pGain*errorLeft + derivateLeft*dGain + integralLeft*iGain);
			int resultRight =(int) (pGain*errorRight + derivateRight*dGain + integralRight*iGain);
			
			
			if(state == 0){
				if(distance > 990){
					drive(0,0);
					state = 1;
					leftMotor.resetTachoCount();
					rightMotor.resetTachoCount();					
				} else {
					drive(minPower + resultRight/2 - resultLeft/2 , minPower + resultLeft/2 - resultRight/2);
				}
				
			} else if(state== 1){
				if(distance < 270){
					// 75 og 65
					leftSpeed = 75;
					rightSpeed = 65;
					drive(leftSpeed,rightSpeed);
				} else {
					drive(leftSpeed-5,rightSpeed-5);
					if(Math.abs(rightValue-black) < 8){ // detect black line
						//drive(0,0);
						//Thread.sleep(20);
						drive(50,65);
						Thread.sleep(50);
						state=2;

						resetPID();
						
						leftMotor.resetTachoCount();
						rightMotor.resetTachoCount();
					}
				}				
			} else if(state == 2){
				if(distance > 1050){
					drive(0,0);
					leftMotor.resetTachoCount();
					rightMotor.resetTachoCount();
					distance=0;
					state = 3;
				} else {
					drive(minPower + resultRight/2 - resultLeft/2 , minPower + resultLeft/2 - resultRight/2);
				}
			} else if(state == 3){
				if(distance < 400){
					leftSpeed = 67;
					rightSpeed = 74;
					drive(leftSpeed,rightSpeed);
				} else {
					drive(leftSpeed-4,rightSpeed-5);
					if(Math.abs(leftValue-black) < 8){ // detect black line
				//		drive(0,0);
					//	Thread.sleep(10);
					
						drive(75,50);
						Thread.sleep(75);
												
						state=4;
						drive(0,0);
						
						resetPID();
						leftMotor.resetTachoCount();
						rightMotor.resetTachoCount();
					}
				}
			} else if(state==4){
				if(distance > 1250){
					drive(0,0);
					leftMotor.resetTachoCount();
					rightMotor.resetTachoCount();
					state = 5;
				} else {
					drive(minPower + resultRight/2 - resultLeft/2 , minPower + resultLeft/2 - resultRight/2);
				}
			} else if(state == 5){
				// on top of platform				
				drive(-70,68);
				Thread.sleep(250);
				while(true){
					int left = leftLight.readValue();
					if(left-black <  5){
						break;
					}
				}			
				Thread.sleep(10);	
				drive(0,0);
				state = 6;
				minPower = 63;
				computePID(1, 0.6f);
				
			} else if (state == 6){
				drive(minPower+2 + resultRight/2 - resultLeft/2 , minPower + resultLeft/2 - resultRight/2);
			}
			
			integralLeft *= integralFactor;
			integralRight*= integralFactor;
			
			lastErrorLeft = errorLeft;
			lastErrorRight= errorRight;
			
			Thread.sleep(dt);		
		}

	}
	
	private void resetPID(){
		integralLeft = integralRight  = 0;
		derivateLeft = derivateRight  = 0;
		lastErrorRight = lastErrorLeft = 0;
		errorLeft = errorRight = 0;
	}

	private void setupIntegralFactorControl() {
		Button.LEFT.addButtonListener(new ButtonListener() {
			
			@Override
			public void buttonReleased(Button b) {
				integralFactor -= 0.005f;
			
				LCD.drawString("                  ",0,2);
				LCD.drawString("if: "+integralFactor , 0, 2);
				
			}
			
			@Override
			public void buttonPressed(Button b) {
				// TODO Auto-generated method stub
				
			}
		});
		Button.RIGHT.addButtonListener(new ButtonListener() {
			
			@Override
			public void buttonReleased(Button b) {
				integralFactor += 0.005f;				   
				LCD.drawString("                ",0,2);
				LCD.drawString("if: "+integralFactor , 0, 2);	
			}
			
			@Override
			public void buttonPressed(Button b) {
				// TODO Auto-generated method stub
				
			}
		});
	}
	
	private void setupDerivateControl() {
		Button.LEFT.addButtonListener(new ButtonListener() {
			
			@Override
			public void buttonReleased(Button b) {
				dGain -= 0.001f;
				
				LCD.drawString("                ",0,2);
				LCD.drawString("dGain: "+dGain , 0, 2);
				
			}
			
			@Override
			public void buttonPressed(Button b) {
				// TODO Auto-generated method stub
				
			}
		});
		Button.RIGHT.addButtonListener(new ButtonListener() {
			
			@Override
			public void buttonReleased(Button b) {
				dGain += 0.001f;				   
				LCD.drawString("                ",0,2);
				LCD.drawString("dGain: "+dGain , 0, 2);	
			}
			
			@Override
			public void buttonPressed(Button b) {
				// TODO Auto-generated method stub
				
			}
		});
	}

	boolean left = true;
	int leftSpeed = 50;
	int rightSpeed = 50;
	
	private void setupTurnControl() {
		Button.ENTER.addButtonListener(new ButtonListener() {
			
			@Override
			public void buttonReleased(Button b) {
				left = !left;	
				leftMotor.resetTachoCount();
				rightMotor.resetTachoCount();
				LCD.drawString("L = " + left,0,1);
			}
			
			@Override
			public void buttonPressed(Button b) {
				// TODO Auto-generated method stub
				
			}
		});
		
		Button.LEFT.addButtonListener(new ButtonListener() {
			
			@Override
			public void buttonReleased(Button b) {
				if(left){
					leftSpeed -= 5;
				} else {
					rightSpeed -= 5;
				}
				
				LCD.drawString("                ",0,2);
				LCD.drawString("left: "+leftSpeed , 0, 2);

				LCD.drawString("                ",0,3);
				LCD.drawString("right: "+rightSpeed , 0, 3);
				
			}
			
			@Override
			public void buttonPressed(Button b) {
				// TODO Auto-generated method stub
				
			}
		});
		Button.RIGHT.addButtonListener(new ButtonListener() {
			
			@Override
			public void buttonReleased(Button b) {
				if(left){
					leftSpeed += 5;
				} else {
					rightSpeed += 5;
				}
				LCD.drawString("                ",0,2);
				LCD.drawString("left: "+leftSpeed , 0, 2);

				LCD.drawString("                ",0,3);
				LCD.drawString("right: "+rightSpeed , 0, 3);
			}
			
			@Override
			public void buttonPressed(Button b) {
				// TODO Auto-generated method stub
				
			}
		});
	}

	
	private void setupGainControl() {
		Button.LEFT.addButtonListener(new ButtonListener() {
			
			@Override
			public void buttonReleased(Button b) {
				pGain -= 0.001f;
				if(pGain <0){
					pGain = 0;
				}
				LCD.drawString("                ",0,2);
				LCD.drawString("pGain: "+pGain , 0, 2);
				
			}
			
			@Override
			public void buttonPressed(Button b) {
				// TODO Auto-generated method stub
				
			}
		});
		Button.RIGHT.addButtonListener(new ButtonListener() {
			
			@Override
			public void buttonReleased(Button b) {
				pGain += 0.001f;				   
				LCD.drawString("                ",0,2);
				LCD.drawString("pGain: "+pGain , 0, 2);	
			}
			
			@Override
			public void buttonPressed(Button b) {
				// TODO Auto-generated method stub
				
			}
		});
	}

	private void setupEscapeButton() {
		Button.ESCAPE.addButtonListener(new ButtonListener() {
			
			@Override
			public void buttonReleased(Button b) {
				dl.close();
				System.exit(1);				
			}
			
			@Override
			public void buttonPressed(Button b) {
			}
		});
	}

	private void setupLog() throws FileNotFoundException, IOException, InterruptedException {
		File f = new File("config");
		
		if(f.exists()){
			FileInputStream fin = new FileInputStream(f);
			int b = fin.read();
			int w = fin.read();
			
			white = w;
			black = b;			

			targetValue = (white+black)/2f;		
			
			fin.close();
			LCD.clear();
			LCD.drawString("white: " + w, 1, 5);
			LCD.drawString("Black: "+black, 1, 6);
		}else{
 			f.createNewFile();
			System.out.println("read black");
			Button.waitForAnyPress();
			int b = leftLight.readValue();
			Thread.sleep(1000);			
			System.out.println("read white");
			Button.waitForAnyPress();
			int w = rightLight.readValue();
		
			white = w;
			black = b;
			
			targetValue = (white+black)/2f;			
			FileOutputStream fout = new FileOutputStream(f);
			fout.write(b);
			fout.write(w);
			fout.flush();
			fout.close();			
		}
	}
	
}
