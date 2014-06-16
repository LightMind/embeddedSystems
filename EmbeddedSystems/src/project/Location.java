package project;

import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import lejos.geom.Point;

public class Location {
	private Point position;
	public final int id;
	public int possibleConnectionBits = 0;
	public List<Location> connections = new ArrayList<Location>();

	public void connectTo(Location l) {
		if (!l.equals(this) && !connections.contains(l)) {
			connections.add(l);
			l.connections.add(this);
		}
	}

	public Point getPoint() {
		return position.clone();
	}

	public void send(DataOutputStream out) {
		try {
			out.writeInt(3);
			out.writeInt(id);
			out.writeFloat(position.x);
			out.writeFloat(position.y);
			out.writeInt(possibleConnectionBits);
			out.flush();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public void disconnect(Location l) {
		if (connections.contains(l)) {
			connections.remove(connections.indexOf(l));
			l.connections.remove(l.connections.indexOf(this));
		}
	}

	public int connectionBits() {
		int b = 0;

		for (Location l : connections) {
			int angle = angleTo(l);
			angle = GraphBot.normalizeAngle(angle);
			if (angle == 0)
				b = b | 1;
			if (angle == 90)
				b = b | 2;
			if (angle == 180)
				b = b | 4;
			if (angle == 270)
				b = b | 8;
		}

		return b;
	}

	public int angleTo(Location l) {
		Point p = l.position.subtract(position);
		int degree = (int) Math.toDegrees(p.angle());
		return degree;

	}

	public Location(int identifier, Point pos, int directions) {
		position = pos;
		id = identifier;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + id;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Location other = (Location) obj;
		if (id != other.id)
			return false;
		return true;
	}

	public static int possibleDirectionBits(int[] angles, int currentAngle) {
		int b = 0;

		for (int i : angles) {
			int j = i - currentAngle;
			j = j + 360;
			j = j % 360;
			if (j == 0)
				b = b | 1;
			if (j == 90)
				b = b | 2;
			if (j == 180)
				b = b | 4;
			if (j == 270)
				b = b | 8;
		}
		return b;
	}

}