package project;

import java.util.ArrayList;
import java.util.List;

import lejos.geom.Point;

public class World {
	public List<Location> graph = new ArrayList<Location>();
	public int graphCounter = 0;

	public Location createNewLocation(Point pos, int[] angles, int currentAngle) {
		int bits = Location.possibleDirectionBits(angles, currentAngle);
		Location l = new Location(graphCounter, pos.clone(), bits);
		graphCounter = graphCounter + 1;
		graph.add(l);
		return l;
	}

	public void deleteLocation(Location l) {
		graph.remove(l);
	}

	public Location findClosestLocation(Point p) {
		double minDistance = 100000000.0;
		Location currentMin = null;
		for (Location l : graph) {
			double d = l.getPoint().distance(p);
			if (d < minDistance) {
				currentMin = l;
				minDistance = d;
			}
		}
		return currentMin;
	}

	public void add(Location currentGraphLocation) {
		if (!graph.contains(currentGraphLocation)) {
			graph.add(currentGraphLocation);
		}

	}
}
