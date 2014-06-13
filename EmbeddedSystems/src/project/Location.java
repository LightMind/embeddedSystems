package project;

import java.util.ArrayList;
import java.util.List;

import lejos.geom.Point;

public class Location {
	public Point position;
	public final int id;
	public List<Location> connections = new ArrayList<Location>();

	public void connectTo(Location l) {
		if (!l.equals(this) && !connections.contains(l)) {
			connections.add(l);
			l.connections.add(this);
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

	public Location(int identifier, Point pos) {
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

}