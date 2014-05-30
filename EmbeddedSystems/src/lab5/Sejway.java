package lab5;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import lejos.nxt.*;
import lejos.nxt.comm.BTConnection;
import lejos.nxt.comm.Bluetooth;

/**
 * A controller for a self-balancing Lego robot with a light sensor
 * on port 2. The two motors should be connected to port B and C.
 *
 * Building instructions in Brian Bagnall: Maximum Lego NXTBuilding 
 * Robots with Java Brains</a>, Chapter 11, 243 - 284
 * 
 * @author Brian Bagnall
 * @version 26-2-13 by Ole Caprani for leJOS version 0.9.1
 */

public class Sejway 
{

    // PID constants
    float KP = 28;
    float KI = 4;
    float KD = 33;
    float SCALE = 18;
    
    BTConnection btc;
    private DataInputStream dis;
    private DataOutputStream dos;

    // Global vars:
    float offset;
    float prev_error;
    float int_error;
	
    LightSensor ls;
	
    public Sejway() 
    {
        ls = new LightSensor(SensorPort.S2, true);
    }
	
    public void getBalancePos() throws IOException 
    {

        // Wait for pc program to send signal, while bot is balanced
    	LCD.drawString("Waiting for signal", 1, 5);
    	dis.read();
    	LCD.drawString("Collecting        ", 1, 5);
        
        // NXTway must be balanced.
        offset = 0;
        int samples = 500;
        for(int i = 0; i < samples; i++){
        	offset = offset + ls.readNormalizedValue();
        	try {
				Thread.sleep(5);
			} catch (InterruptedException e) {	e.printStackTrace();}
        }
        offset= offset/samples;
        LCD.clear();
        LCD.drawInt((int)offset, 2, 4);
        LCD.refresh();
        
    }
	
    public void pidControl() throws IOException 
    {
        while (!Button.ESCAPE.isDown()) 
        {
            float normVal = ls.readNormalizedValue();
            
            if(dis.available() >= 3){
            	KP = dis.readByte();
            	KI = dis.readByte();
            	KD = dis.readByte();
            	offset += dis.readByte();
            }

            LCD.drawInt(dis.available(),5 , 1, 1);
            LCD.drawString("P "+ KP, 2, 3);
            LCD.drawString("I "+ KI, 2, 4);
            LCD.drawString("D "+ KD, 2, 5);
            LCD.refresh();
            	
            // Proportional Error:
            float error = normVal - offset;
            // Adjust far and near light readings:
            if (error < 0) error = (int)(error * 1.8F);
			
            // Integral Error:
            int_error = ((int_error + error) * 2)/3;
			
            // Derivative Error:
            float deriv_error = error - prev_error;
            prev_error = error;
			
            int pid_val = (int)((KP * error + KI * int_error + KD * deriv_error) / SCALE);
			
            if (pid_val > 100)
                pid_val = 100;
            if (pid_val < -100)
                pid_val = -100;

            // Power derived from PID value:
            int power = Math.abs(pid_val);
            power = 55 + (power * 45) / 100; // NORMALIZE POWER


            if (pid_val > 0) {
                MotorPort.B.controlMotor(power, BasicMotorPort.FORWARD);
                MotorPort.C.controlMotor(power, BasicMotorPort.FORWARD);
            } else {
                MotorPort.B.controlMotor(power, BasicMotorPort.BACKWARD);
                MotorPort.C.controlMotor(power, BasicMotorPort.BACKWARD);
            }
        }
    }
	
    public void shutDown()
    {
        // Shut down light sensor, motors
        Motor.B.flt();
        Motor.C.flt();
        ls.setFloodlight(false);
    }
	
    public static void main(String[] args) throws IOException 
    {
    	setupExit();
    	
        Sejway sej = new Sejway();
        sej.setupBluetooth();
        sej.getBalancePos();
        sej.pidControl();
        sej.shutDown();
    }

    private static void setupExit() {
		ButtonListener t = new ButtonListener() {
			
			@Override
			public void buttonReleased(Button b) {
				System.exit(1);
				
			}
			
			@Override
			public void buttonPressed(Button b) {
				// TODO Auto-generated method stub
				
			}
		};
		Button.ESCAPE.addButtonListener(t);
		
	}

	private final String waiting = "Waiting ...";
    
	private void setupBluetooth() {
		LCD.drawString(waiting,0,0);
        btc = Bluetooth.waitForConnection();
        LCD.clear();
        dis = btc.openDataInputStream();
        dos = btc.openDataOutputStream();		
	}
}