package sim.graph;

import java.awt.Canvas;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import sim.graph.Graph.Point;

public class GraphCanvas extends Canvas {

	public static enum GraphMode {
		DMG_PER_TURN,
		HIT_RATIO;
	}
	
	private int maxY;
	private List<Graph> graphs = new ArrayList<>();
	
	
	private static int SIZE_X = 500;
	private static int SIZE_Y = 500;
	
	private static int OFFSET_X = 50;
	private static int OFFSET_Y = 50;
	
	private GraphMode mode = GraphMode.DMG_PER_TURN;
	
	public GraphCanvas(int maxY) {
		setBackground(Color.WHITE);
		setSize(SIZE_X + OFFSET_X, SIZE_Y + OFFSET_Y);
		setPreferredSize(new Dimension(SIZE_X + OFFSET_X, SIZE_Y + OFFSET_Y));
		this.maxY = maxY;
	}
	
	public void setGraphs(List<Graph> graphs) {
		this.graphs = graphs;
		repaint();
	}
	
	
	public void setSettings(int y, GraphMode mode) {
		this.mode = mode;
		if(mode==GraphMode.HIT_RATIO) {
			maxY = 100;
		} else {
			this.maxY = y;
		}
		repaint();
	}
	
//	public void setMaxYValue(int y) {
//		this.maxY = y;
//		repaint();
//	}
//	
//	public void setYValue(GraphMode mode) {
//		this.mode = mode;
//		repaint();
//	}
	
	@Override
	public void paint(Graphics g) {
		paintGrid(g);
		paintScale(g);
		g.translate(OFFSET_X, 0);
		Function<Point, Double> fun = p->p.dmgPerTurn;
		if(mode==GraphMode.HIT_RATIO) {
			fun = p->p.hitChance;
		}
		for(Graph graph : graphs) {
			g.setColor(graph.getColor());
			for(int i = 0; i<graph.getPoints().size(); i++) {
				Point current = graph.getPoints().get(i);
				if(i>0) {
					Point last = graph.getPoints().get(i-1);
					drawLine(last, current, fun, g);
				}
			}
		}
		g.translate(-OFFSET_X, 0);
	}

	private void paintScale(Graphics g) {
		for(int i = 0; i<SIZE_X; i+=5) {
			String s = "" + i;
			g.drawString(s, OFFSET_X - s.length()*5 + i*10, SIZE_Y+13);
		}
		g.drawString("Distance (Tiles)", SIZE_X/2, SIZE_Y + 35);
		
		if(mode==GraphMode.DMG_PER_TURN) {
			for(int i = 1; i<=10; i++) {
				String s = "" + (maxY / 10)*i;
				g.drawString(s, 48-(s.length()*7), SIZE_Y - (SIZE_Y/10) *i + 5);
			}
			g.drawString("Dmg/Turn", 10, SIZE_Y + 35);
		} else {
			for(int i = 1; i<=10; i++) {
				String s = 10*i + "%";
				g.drawString(s, 48-(s.length()*7), SIZE_Y - (SIZE_Y/10) *i + 5);
			}
			g.drawString("%", 20, SIZE_Y + 35);
		}
	}

	private void paintGrid(Graphics g) {
		g.translate(OFFSET_X, 0);
		g.setColor(Color.LIGHT_GRAY);
		g.drawLine(0, 0, SIZE_X, 0);
		g.drawLine(0, 0, 0, SIZE_Y);
		for(int i = 0; i<=SIZE_X; i+=10) {
			g.drawLine(i, 0, i, SIZE_Y);
		}
		g.setColor(Color.DARK_GRAY);
		for(int i = 0; i<=SIZE_X; i+=50) {
			g.drawLine(i, 0, i, SIZE_Y);
		}
		
		g.setColor(Color.LIGHT_GRAY);

		for(int i = 0; i<=SIZE_Y; i+=10) {
			g.drawLine(0, i, SIZE_X, i);
		}
		g.setColor(Color.DARK_GRAY);
		for(int i = 0; i<=SIZE_Y; i+=50) {
			g.drawLine(0, i, SIZE_X, i);
		}
		g.translate(-OFFSET_X, 0);
	}

	
	private void drawLine(Point last, Point current, Function<Point, Double> fun, Graphics g) {
		g.drawLine(last.distance*10, cY(fun.apply(last)), current.distance*10, cY(fun.apply(current)));
	}
	
	private int cY(double i) {
		double y = (double)SIZE_Y/((double)maxY/i);
		return SIZE_Y-(int)y;
	}
}
