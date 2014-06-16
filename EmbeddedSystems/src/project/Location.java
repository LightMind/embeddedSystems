package project;

import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import lejos.geom.Point;

public class Location {
	private Point position;
	public final int id;
	public int possibleConnectionBits = 0;
	public List<Location> connections = new ArrayList<Location>();

	public Location parent = null;
	public float dist = Float.MAX_VALUE;

	public void connectTo(Location l, DataOutputStream dos) throws IOException {
		if (!l.equals(this) && !connections.contains(l)) {
			connections.add(l);
			l.connections.add(this);

			dos.writeInt(7);
			dos.writeFloat(position.x);
			dos.writeFloat(position.y);
			dos.writeFloat(l.position.x);
			dos.writeFloat(l.position.y);
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
		while (degree < 0) {
			degree += 360;
		}
		while (degree >= 360) {
			degree -= 360;
		}
		return degree;
	}

	public float distanceTo(Location l) {
		return (float) l.position.distance(position);
	}

	public List<Integer> getUndiscoveredDirections() {
		List<Integer> dirs = new ArrayList<Integer>();
		int dirbits = connectionBits();
		if ((dirbits & 1) == 0 && (possibleConnectionBits & 1) == 1) {
			dirs.add(0);
		}
		if ((dirbits & 2) == 0 && (possibleConnectionBits & 2) == 2) {
			dirs.add(90);
		}
		if ((dirbits & 4) == 0 && (possibleConnectionBits & 4) == 4) {
			dirs.add(180);
		}
		if ((dirbits & 8) == 0 && (possibleConnectionBits & 8) == 8) {
			dirs.add(270);
		}

		return dirs;
	}

	public Location(int identifier, Point pos, int directions) {
		position = pos;
		id = identifier;
		possibleConnectionBits = directions;
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
			int j = i + currentAngle;
			if (j < 0) {
				j = j + 360;
			}
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