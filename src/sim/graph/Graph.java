package sim.graph;

import java.awt.Color;
import java.util.List;

public class Graph {

	public static class Point {
		public final int distance;
		public final double dmgPerTurn;
		public final double hitChance;
		public Point(int distance, double hitChance, double dmgPerTurn) {
			this.distance = distance;
			this.dmgPerTurn = dmgPerTurn;
			this.hitChance = hitChance;
		}
	}
	
	private final List<Point> points;
	private final Color color;
	
	public Graph(List<Point> points, Color color) {
		this.points = points;
		this.color = color;
	}


	public List<Point> getPoints() {
		return points;
	}


	public Color getColor() {
		return color;
	}
	
	
}
