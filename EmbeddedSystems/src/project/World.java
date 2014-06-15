package project;

import java.util.ArrayList;
import java.util.List;

import lejos.geom.Point;

public class World {
	public List<Location> graph = new ArrayList<Location>();
	public int graphCounter = 0;

	public Location createNewLocation(Point pos) {
		Location l = new Location(graphCounter, pos.clone());
		graphCounter = graphCounter + 1;
		graph.add(l);
		return l;
	}
	
	public void deleteLocation(Location l){
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

	public void setPossibleDirectionBits(Location l, int[] angles,
			int currentAngle) {
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
		l.possibleConnectionBits = b;
	}

	public void add(Location currentGraphLocation) {
		if (!graph.contains(currentGraphLocation)) {
			graph.add(currentGraphLocation);
		}

	}
}
