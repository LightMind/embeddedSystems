package project;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Random;
import java.util.Stack;

import lab4.PIDLineFollower;
import lejos.geom.Point;
import lejos.nxt.Button;
import lejos.nxt.ColorSensor;
import lejos.nxt.ColorSensor.Color;
import lejos.nxt.comm.BTConnection;
import lejos.nxt.comm.Bluetooth;
import lejos.nxt.LCD;
import lejos.nxt.LightSensor;
import lejos.nxt.Motor;
import lejos.nxt.MotorPort;
import lejos.nxt.SensorPort;
import lejos.nxt.Sound;
import lejos.robotics.localization.OdometryPoseProvider;
import lejos.robotics.navigation.DifferentialPilot;
import lejos.robotics.navigation.Move;
import lejos.robotics.navigation.MoveListener;
import lejos.robotics.navigation.MoveProvider;
import lejos.robotics.navigation.Move.MoveType;
import lejos.robotics.navigation.Pose;
import lejos.util.PIDController;

public class GraphBot {
	public int errorRadius = 70;

	World world = new World();
	Location lastLocation = null;

	LightSensor leftSensor = new LightSensor(SensorPort.S2);
	LightSensor rightSensor = new LightSensor(SensorPort.S1);
	ColorSensor colorSensor = new ColorSensor(SensorPort.S4);

	DifferentialPilot pilot = new DifferentialPilot(56, 109, Motor.C, Motor.A);

	Stack<Location> path = new Stack<>();

	Random random = new Random(0);

	int currentAngle = 0;
	float currentDistance = 0;
	Point currentPoint = new Point(0, 0);

	int whiteValue = 624;
	int blackValue = 365;
	int grayValue = 0;

	BTConnection btc;
	private DataInputStream dis;
	private DataOutputStream dos;

	private void setupBluetooth() {
		LCD.drawString("waiting", 1, 2);
		btc = Bluetooth.waitForConnection();
		LCD.clear();
		dis = btc.openDataInputStream();
		dos = btc.openDataOutputStream();
	}

	public void run() throws Exception {
		setupBluetooth();
		DistanceTravelListener dtl = new DistanceTravelListener(dos);

		// read white
		// whiteValue = calibrateWhite();
		// blackValue = calibrateBlack();
		grayValue = (whiteValue + blackValue) / 2;

		initializePilot(dtl);
		pilot.forward();

		int sendCounter = 0;

		while (true) {
			currentDistance = dtl.distance;
			LCD.drawString("" + currentDistance + "    ", 1, 5);

			PID(grayValue);
			sendCounter = updatePosition(dtl, sendCounter);

			if (findCrossroads()) {
				pilot.stop();
				Thread.sleep(500);
				pilot.travel(80); // drive to center of intersection

				updateCurrentPosition(dtl);

				Location currentGraphLocation = world
						.findClosestLocation(currentPoint);

				currentGraphLocation = updateGraph(currentGraphLocation);

				sendPosition(currentPoint.x, currentPoint.y);
				currentGraphLocation.send(dos);

				Thread.sleep(250);

				int angle = 0;
				if (path.empty()) {
					List<Integer> directions = currentGraphLocation
							.getUndiscoveredDirections();
					dos.writeInt(110);
					dos.writeInt(currentGraphLocation.possibleConnectionBits);
					dos.writeInt(currentGraphLocation.connectionBits());
					if (directions.size() > 0) {
						int select = random.nextInt(directions.size());
						angle = directions.get(select) - currentAngle;
						angle = limitAngle(angle);
						dos.writeInt(101);
						dos.writeInt(10);
						dos.writeInt(directions.size());
						for (Integer direction : directions) {
							dos.writeInt(direction);
						}

					} else {
						dos.writeInt(102);
						Location target = world
								.findClosestLocationWithUnknownDirections(currentPoint);
						if (target != null) {
							path = world.dijkstra(currentGraphLocation, target);

							Location next = path.pop();
							int directionToNext = currentGraphLocation
									.angleTo(next);
							angle = directionToNext - currentAngle;
							angle = limitAngle(angle);
						} else {
							Location newTarget = null;
							while (newTarget == null) {
								int index = dis.readInt();
								newTarget = world.findLocation(index);
							}

							path = world.dijkstra(currentGraphLocation,
									newTarget);
							Location next = path.pop();
							int directionToNext = currentGraphLocation
									.angleTo(next);
							angle = directionToNext - currentAngle;
							angle = limitAngle(angle);
						}
					}
				} else {
					dos.writeInt(103);
					Location next = path.pop();
					int directionToNext = currentGraphLocation.angleTo(next);
					angle = directionToNext - currentAngle;
					angle = limitAngle(angle);
				}

				rotateToDirection(angle);

				currentAngle += angle;
				currentAngle %= 360;
				currentDistance = 0;
				dtl.reset();
				lastLocation = currentGraphLocation;
			}
			Thread.sleep(25);
		}

	}

	private int limitAngle(int angle) {
		while (angle < 0) {
			angle += 360;
		}
		while (angle >= 360) {
			angle -= 360;
		}
		return angle;
	}

	private void updateCurrentPosition(DistanceTravelListener dtl) {
		currentDistance = dtl.distance;

		float x = (float) Math.cos(Math.toRadians(currentAngle))
				* currentDistance;
		float y = (float) Math.sin(Math.toRadians(currentAngle))
				* currentDistance;

		currentPoint.x += x;
		currentPoint.y += y;
	}

	private Location updateGraph(Location currentGraphLocation)
			throws IOException {
		if (currentGraphLocation == null) {
			int[] results = normalizeAngles(findOutgoingRoads(grayValue));
			currentGraphLocation = world.createNewLocation(currentPoint,
					results, currentAngle);
			Sound.beepSequenceUp();
			dos.writeInt(4);
		} else {
			if (currentGraphLocation.getPoint().distance(currentPoint) > errorRadius) {
				int[] results = normalizeAngles(findOutgoingRoads(grayValue));
				currentGraphLocation = world.createNewLocation(currentPoint,
						results, currentAngle);
				Sound.beepSequenceUp();
				currentGraphLocation.connectTo(lastLocation, dos);
				dos.writeInt(5);
			} else {
				// we assume that we hit a node , that is known to us.
				currentGraphLocation.connectTo(lastLocation, dos);
				currentPoint = currentGraphLocation.getPoint();
				dos.writeInt(6);
			}
		}
		return currentGraphLocation;
	}

	private void initializePilot(DistanceTravelListener dtl) {
		pilot.addMoveListener(dtl);
		pilot.setAcceleration(500);
		pilot.setRotateSpeed(100);
		pilot.setTravelSpeed(150);
	}

	private int updatePosition(DistanceTravelListener dtl, int sendCounter)
			throws IOException, InterruptedException {
		sendCounter++;
		sendCounter = sendCounter % 5;
		if (sendCounter == 0) {
			positionUpdate(dtl);
		}
		return sendCounter;
	}

	private void sendDebugDirections(int[] results) throws IOException {
		dos.writeInt(10);
		dos.writeInt(results.length);
		for (int i = 0; i < results.length; i++) {
			dos.writeInt(results[i]);
		}
	}

	private void rotateToDirection(int angle) throws InterruptedException {
		if (angle != 0) {
			pilot.rotate(angle - 20);
			Sound.beep();
			Thread.sleep(100);
			pilot.rotate(45, true);

			while (pilot.isMoving()) {
				if (leftSensor.readNormalizedValue() < grayValue) {
					pilot.stop();
					pilot.rotate(7);
				}
			}
		}
	}

	private void positionUpdate(DistanceTravelListener dtl) throws IOException,
			InterruptedException {
		currentDistance = dtl.distance;

		float testX = currentPoint.x
				+ (float) Math.cos(Math.toRadians(currentAngle))
				* currentDistance;
		float testY = currentPoint.y
				+ (float) Math.sin(Math.toRadians(currentAngle))
				* currentDistance;

		sendPosition(testX, testY);
	}

	public static int normalizeAngle(int angle) {
		if (angle > 46 && angle < 135)
			return 90;
		if (angle > 136 && angle < 225)
			return 180;
		if (angle > 226 && angle < 315)
			return 270;

		return 0;
	}

	private int[] normalizeAngles(int[] roads) {
		int[] res = new int[roads.length];
		for (int i = 0; i < roads.length; i++) {
			res[i] = normalizeAngle(roads[i]);
		}

		return res;
	}

	private int[] findOutgoingRoads(int grayValue) {
		boolean[] testForBlack = new boolean[400];

		pilot.rotate(360, true);

		while (pilot.isMoving()) {
			double angle = pilot.getAngleIncrement();
			int rounding = (int) (angle);

			int lightValue = leftSensor.readNormalizedValue();
			testForBlack[rounding] = lightValue < grayValue;
		}

		for (int i = 0; i < 360; i++) {
			int color = 0;
			if (testForBlack[i]) {
				color = 1;
			}
			for (int j = 0; j < 10; j++) {
				LCD.setPixel(i, j, color);
			}
		}

		int[] angles = new int[16];
		int numberOfAngles = 0;

		for (int i = 0; i < 360; i++) {
			if (testForBlack[i] == false) {
				continue;
			} else {
				int angle = i;
				int counter = 1;
				i++;
				while (testForBlack[i]) {
					angle += i;
					counter++;
					i++;
				}

				angle = angle / counter;
				angles[numberOfAngles] = angle % 360;
				numberOfAngles++;
			}
		}

		int[] result = new int[numberOfAngles];
		for (int i = 0; i < numberOfAngles; i++) {
			result[i] = angles[i];
		}
		return result;
	}

	public boolean findCrossroads() {
		int colorID = colorSensor.getColorID();
		return colorID == 3;
	}

	private void testColorSensor() {
		int color = colorSensor.getColorID();

		LCD.clear();
		LCD.drawString("color: " + color, 0, 0);
		/*
		 * LCD.drawString("red: " + color.getRed(), 0, 0);
		 * LCD.drawString("green: " + color.getGreen(), 0, 1);
		 * LCD.drawString("blue: " + color.getBlue(), 0, 2);
		 */
		/*
		 * LCD.drawString("Green: " + ColorSensor.GREEN, 0, 1);
		 * LCD.drawString("Yellow: " + ColorSensor.YELLOW, 0, 2);
		 * LCD.drawString("White: " + ColorSensor.WHITE, 0, 3);
		 * LCD.drawString("Red: " + ColorSensor.RED, 0, 4);
		 * LCD.drawString("Blue: " + ColorSensor.BLUE, 0, 5);
		 * LCD.drawString("Black: " + ColorSensor.BLACK, 0, 6);
		 */
	}

	private void PID(int target) {
		float pGain = 0.7f;

		int errorLeft = target - leftSensor.readNormalizedValue();
		int errorRight = target - rightSensor.readNormalizedValue();

		int resultLeft = (int) (pGain * errorLeft);
		int resultRight = (int) (pGain * errorRight);

		pilot.steer((resultLeft - resultRight) / 2);
	}

	private int calibrateBlack() throws InterruptedException {
		int blackValue = 0;
		while (true) {
			LCD.clear();
			blackValue = leftSensor.readNormalizedValue();
			LCD.drawString("Black: " + blackValue, 0, 1);
			Thread.sleep(50);
			if (Button.ENTER.isDown()) {
				break;
			}
		}
		Thread.sleep(500);

		return blackValue;
	}

	private int calibrateWhite() throws InterruptedException {
		int whiteValue = 0;
		while (true) {
			LCD.clear();
			whiteValue = leftSensor.readNormalizedValue();
			LCD.drawString("White: " + whiteValue, 1, 1);
			Thread.sleep(50);
			if (Button.ENTER.isDown()) {
				break;
			}
		}

		Thread.sleep(500);
		return whiteValue;
	}

	private void sendPosition(float x, float y) throws IOException,
			InterruptedException {
		dos.writeInt(1);
		dos.writeInt((int) x);
		dos.writeInt((int) y);
		dos.flush();

	}

}
