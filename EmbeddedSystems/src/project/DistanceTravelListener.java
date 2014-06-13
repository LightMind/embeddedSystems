package project;

import java.io.DataOutputStream;
import java.io.IOException;

import lejos.robotics.navigation.Move;
import lejos.robotics.navigation.MoveListener;
import lejos.robotics.navigation.MoveProvider;
import lejos.robotics.navigation.Move.MoveType;

public class DistanceTravelListener implements MoveListener {
	
	public float distance = 0f;
	
	public DataOutputStream out;
	
	
	public float direction;
	public float x,y;
	
	public DistanceTravelListener(DataOutputStream out){
		this.out = out;		
	}

	@Override
	public void moveStarted(Move event, MoveProvider mp) {

	}

	@Override
	public void moveStopped(Move event, MoveProvider mp) {			
		distance += event.getDistanceTraveled();
	}
	
	public void reset(){
		distance = 0;
	}

}
