package lab9;

import lejos.nxt.Button;
import lejos.nxt.ButtonListener;
import lejos.nxt.MotorPort;
import lejos.nxt.NXTRegulatedMotor;
import lejos.robotics.navigation.DifferentialPilot;

public class Navigator {

	public static void main(String[] args) {
		setupEscapeButton();
		DifferentialPilot pilot = new DifferentialPilot(56,108, new NXTRegulatedMotor(MotorPort.C), new NXTRegulatedMotor(MotorPort.A));
		System.out.println("Navigator!");
		System.out.println("Press enter");
		Button.ENTER.waitForPressAndRelease();
		
		drivePenny(pilot);
		//driveLine(pilot);

	}
	
	private static void drivePenny(DifferentialPilot pilot){
		pilot.setTravelSpeed(50);
		pilot.setRotateSpeed(40);
		pilot.travel(200);
		pilot.rotate(135);
		pilot.travel(141.4);
		pilot.rotate(135);
		pilot.travel(150);
		pilot.rotate(-90-27.5);
		pilot.travel(111.8);		
	}
	

	private static void driveLine(DifferentialPilot pilot){
		pilot.setTravelSpeed(50);
		pilot.setRotateSpeed(40);
		
		while(true){
			pilot.travel(200);
			pilot.rotate(180);		
		}
	}	
	
	private static void setupEscapeButton() {
		Button.ESCAPE.addButtonListener(new ButtonListener() {
			
			@Override
			public void buttonReleased(Button b) {
				System.exit(1);				
			}
			
			@Override
			public void buttonPressed(Button b) {
			}
		});
	}

}
