package project;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Stack;

import lejos.geom.Point;

public class World {
	public List<Location> graph = new ArrayList<Location>();
	public int graphCounter = 0;

	public Stack<Location> dijkstra(Location start, Location target) {
		Stack<Location> path = new Stack<>();
		List<Location> q = new ArrayList<Location>();

		for (Location v : graph) {
			v.parent = null;
			v.dist = Float.MAX_VALUE;
			q.add(v);
		}
		start.dist = 0;

		while (q.size() > 0) {
			// find node with smallest dist
			float dist = Float.MAX_VALUE;
			Location smallestL = null;
			for (Location l : q) {
				if (l.dist <= dist) {
					smallestL = l;
					dist = l.dist;
				}
			}
			if (smallestL == null) {
				return null;
			}
			q.remove(q.indexOf(smallestL));

			for (Location a : smallestL.connections) {
				float alt = smallestL.dist + smallestL.distanceTo(a);
				if (alt < a.dist) {
					a.dist = alt;
					a.parent = smallestL;
				}
			}
		}

		Location current = target;

		while (current.parent != null) {
			path.push(current);
			current = current.parent;
		}

		return path;
	}

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

	public Location findClosestLocationWithUnknownDirections(Point p) {
		double minDistance = 100000000.0;
		Location currentMin = null;
		for (Location l : graph) {
			double d = l.getPoint().distance(p);
			if (d < minDistance && l.getUndiscoveredDirections().size() > 0) {
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

	public Location findLocation(int index) {
		for (Location l : graph) {
			if (l.id == index) {
				return l;
			}
		}
		return null;

	}
}
