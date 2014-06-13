package project;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

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
	public List<Location> graph = new ArrayList<Location>();
	public int graphCounter = 0;
	LightSensor leftSensor = new LightSensor(SensorPort.S2);
	LightSensor rightSensor = new LightSensor(SensorPort.S1);
	ColorSensor colorSensor = new ColorSensor(SensorPort.S4);

	DifferentialPilot pilot = new DifferentialPilot(56, 109, Motor.C, Motor.A);

	Random random = new Random(0);

	int currentAngle = 0;
	float currentDistance = 0;
	Point currentLocation = new Point(0, 0);

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

	public Location createNewLocation(Point pos) {
		Location l = new Location(graphCounter, pos);
		graphCounter++;
		return l;
	}

	public Location findClosesLocation(Point p) {
		double minDistance = Float.MAX_VALUE;
		Location currentMin = null;
		for (Location l : graph) {
			double d = l.position.distance(p);
			if (d < minDistance) {
				currentMin = l;
				minDistance = d;
			}
		}
		return currentMin;
	}

	public void setPossibleDirectionBits(Location l, int[] angles) {
		int b = 0;

		for (int i : angles) {
			int j = i - currentAngle;
			if (j == 0)
				b = b | 1;
			if (j == 90)
				b = b | 2;
			if (j == 180)
				b = b | 4;
			if (j == 270)
				b = b | 8;
		}
		l.possibleConnectionBits = b;
	}

	public void run() throws Exception {
		setupBluetooth();
		DistanceTravelListener dtl = new DistanceTravelListener(dos);

		// read white
		// whiteValue = calibrateWhite();
		// blackValue = calibrateBlack();
		grayValue = (whiteValue + blackValue) / 2;

		pilot.addMoveListener(dtl);

		pilot.setAcceleration(500);
		pilot.setRotateSpeed(100);
		pilot.setTravelSpeed(150);

		pilot.forward();

		while (true) {
			currentDistance = dtl.distance;
			LCD.drawString("" + currentDistance + "    ", 1, 5);

			PID(grayValue);

			if (findCrossroads()) {
				pilot.stop();
				Thread.sleep(500);
				pilot.travel(80); // drive the car to the center of the
									// crossroad

				currentDistance = dtl.distance;

				Point p = new Point((float) Math.toRadians(currentAngle));
				p.multiplyBy(currentDistance);
				currentLocation.add(p);

				Location currentGraphLocation = findClosesLocation(currentLocation);
				if (currentGraphLocation == null) {
					currentGraphLocation = createNewLocation(currentLocation);
					graph.add(currentGraphLocation);
				}

				LCD.drawString("" + currentDistance + "    ", 1, 5);
				LCD.drawString("x = " + (int) currentLocation.x, 1, 3);
				LCD.drawString("y = " + (int) currentLocation.y, 1, 4);

				Thread.sleep(10);
				dos.writeInt(1);
				dos.writeInt((int) (int) currentLocation.x);
				dos.writeInt((int) (int) currentLocation.y);
				dos.flush();

				int[] results = normalizeAngles(findOutgoingRoads(grayValue));

				setPossibleDirectionBits(currentGraphLocation, results);
				Thread.sleep(250);

				int select = random.nextInt(results.length);
				int angle = results[select];

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

				currentAngle += angle;
				currentAngle %= 360;
				currentDistance = 0;
				dtl.reset();
			}
			Thread.sleep(25);
		}

	}

	public static int normalizeAngle(int angle) {
		angle = angle % 360;

		if (angle > 46 && angle < 135)
			return 90;
		if (angle > 136 && angle < 225)
			return 180;
		if (angle > 226 && angle < 315)
			return 270;
		if (angle > 316 && angle < 45)
			return 0;
		return 0;
	}

	private int[] normalizeAngles(int[] roads) {
		int[] res = new int[roads.length];
		for (int i = 0; i < roads.length; i++) {
			res[i] = normalizeAngle(res[i]);
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

}
