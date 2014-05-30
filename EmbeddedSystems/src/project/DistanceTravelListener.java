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
		// TODO Auto-generated method stub

	}

	@Override
	public void moveStopped(Move event, MoveProvider mp) {			
		distance += event.getDistanceTraveled();
		
		float directionAsRadian =(float) Math.toRadians(direction);

		int type = event.getMoveType().ordinal();
		float arcRadius = event.getArcRadius();
		float turned = event.getAngleTurned();
		float distance = event.getDistanceTraveled();
		
		if( !Float.isInfinite(arcRadius)){
			double dx,dy;
			double asRadian = Math.toRadians(turned);
			
			if(arcRadius >= 0.0){
				dx = arcRadius * ( Math.cos(asRadian) -1.0);
				dy = arcRadius * ( Math.sin(asRadian));
				
			} else {
				dx = arcRadius * ( -Math.cos(asRadian) - 1.0 );
				dy = arcRadius * ( -Math.sin(asRadian));
			}
			
			// update x,y
			float cosd = (float) Math.cos(directionAsRadian);
			float sind = (float) Math.sin(directionAsRadian);
			
			x += dy*cosd - dx*sind;
			y += dy*sind + dx*cosd;
		} else {
			x += Math.cos(directionAsRadian) * distance;
			y += Math.sin(directionAsRadian) * distance;
			
		}
			
		
		direction = direction + turned  % 360; // degrees
		
		try {
			out.writeInt(2); // this is an event message
			out.writeInt(type);
			out.writeFloat(arcRadius);
			out.writeFloat(turned);
			out.writeFloat(distance);
			out.flush();
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		
		
		
	}
	
	public void reset(){
		distance = 0;
	}

}
