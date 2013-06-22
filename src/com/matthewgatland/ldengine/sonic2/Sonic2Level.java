package com.matthewgatland.ldengine.sonic2;

import static com.matthewgatland.ldengine.sonic2.Scale.PIXEL_SIZE;

import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;

import com.matthewgatland.glgame.GLHelper;
import com.matthewgatland.ldengine.engine.Point;
import com.matthewgatland.ldengine.sonic2.Line.LineType;

public class Sonic2Level {
	private final List<Line> lines;
	private final List<Entity> ents;
	private final List<Line2D> debugLines;

	public enum CollisionType {ALL, SOLID}

	public Sonic2Level() {
		super();
		lines = new ArrayList<Line>();
		ents = new ArrayList<Entity>();
		debugLines = new ArrayList<Line2D>();
		ridiculousLevelLoading();
	}

	private void ridiculousLevelLoading() {
		Scanner s;
		try {
			s = new Scanner(new File("level.txt"));
			while (s.hasNext()) {
				final String line = s.nextLine();
				final String[] parts = line.split(" "); //four numbers and a string
				final int[] nums = new int[4];
				for (int i = 0; i < 4; i++) {
					nums[i] = Integer.parseInt(parts[i]);
				}
				addLineInPixelScale(nums[0], nums[1], nums[2], nums[3], LineType.valueOf(parts[4]));
			}
		} catch (final FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void saveLevel(final String string) {
		try {
			final FileWriter f = new FileWriter(string);
			for (final Line l: lines) {
				f.append(l.x1/PIXEL_SIZE + " " + l.y1/PIXEL_SIZE + " " + l.x2/PIXEL_SIZE + " " + l.y2/PIXEL_SIZE + " " + l.getType() + "\n");
			}
			f.close();
		} catch (final IOException e) {
			System.out.println("Error writing file");
			e.printStackTrace();
		}
	}

	public void addLineInPixelScale(final int x1, final int y1, final int x2, final int y2, final LineType type) {
		lines.add(new Line(x1 * PIXEL_SIZE, y1 * PIXEL_SIZE, x2 * PIXEL_SIZE, y2 * PIXEL_SIZE, type));
	}

	public void tick() {
		//nothing
		debugLines.clear();
	}

	public void render() {
		for (final Line line : lines) {
			if (line.getType() == LineType.SOLID) {
				GLHelper.setColor(255,255,255);
			} else {
				GLHelper.setColor(192,192,192);
			}
			GLHelper.drawLineFullScale(line.x1, line.y1, line.x2, line.y2);
		}

		GLHelper.setColor(0,255,0);
		for (final Line2D line: debugLines) {
			GLHelper.drawLineFullScale((int)line.getX1(), (int)line.getY1(), (int)line.getX2(), (int)line.getY2());
		}

		GLHelper.setColor(255,127,0);
		for (final Entity ent: ents) {
			GLHelper.drawLineFullScale(ent.getPos().getX(), ent.getPos().getY(), ent.getPos().getX() + 32000, ent.getPos().getY() + 32000);
		}
	}

	/**
	 * Return the collision point closest to the start of the trace line.
	 */
	public Point traceForCollisions(final Line2D traceLine, final CollisionType type) {
		debugLines.add(traceLine);
		final List<Point2D> intersectionPoints = checkForCollisions(type, Collections.singletonList(traceLine));
		Point2D closest = null;
		double closestDistance = Double.MAX_VALUE;
		for (final Point2D point : intersectionPoints) {
			final double newDistance = point.distance(traceLine.getP1());
			if (newDistance < closestDistance) {
				closestDistance = newDistance;
				closest = point;
			}
		}
		if (closest == null) {
			return null;
		}
		return new Point((int) closest.getX(), (int) closest.getY());
	}

	/**
	 * Set max distance to zero for infinite distance.
	 * @param x
	 * @param y
	 * @param maxDistance
	 * @return
	 */
	public Line findLineClosestToInPixelScale(final int x, final int y, final int maxDistance) {
		double closestDist = maxDistance != 0? maxDistance * PIXEL_SIZE : Integer.MAX_VALUE;
		Line closestLine = null;
		for (final Line l: lines) {
			final double distance = Line2D.ptSegDist(l.x1, l.y1, l.x2, l.y2, x*PIXEL_SIZE, y*PIXEL_SIZE);
			if (distance < closestDist) {
				closestLine = l;
				closestDist = distance;
			}
		}
		return closestLine;
	}

	private List<Point2D> checkForCollisions(final CollisionType type, final List<Line2D> actorLines) {
		final List<Point2D> intersectionPoints = new ArrayList<Point2D>();
		for (final Line2D actorLine : actorLines) {
			for (final Line levelLine : lines) {
				if (type.equals(CollisionType.ALL) || levelLine.getType() == LineType.SOLID) {
					final Line2D levelLine2D = levelLine.getLine2D();
					final Point2D intersectionPoint = getIntersectionPoint(levelLine2D, actorLine);
					if (intersectionPoint != null) {
						intersectionPoints.add(intersectionPoint);
					}
				}
			}
		}
		return intersectionPoints;
	}

	// from http://www.thatsjava.com/java-tech/54693/
	// thanks tom7.
	private Point2D.Float getIntersectionPoint(final Line2D line, final Line2D line2) {
		if (!line.intersectsLine(line2)) { // replace with
			// Line2D.lineIntersects(x,y,x,y)
			// and stop using Line2D objects?
			return null;
		}
		final double px = line.getX1(), py = line.getY1(), rx = line.getX2() - px, ry = line.getY2() - py;
		final double qx = line2.getX1(), qy = line2.getY1(), sx = line2.getX2() - qx, sy = line2.getY2() - qy;
		final double det = sx * ry - sy * rx;
		if (det == 0) {
			return null;
		} else {
			final double z = (sx * (qy - py) + sy * (px - qx)) / det;
			if (z == 0 || z == 1) {
				return null; // intersection at end point!
			}
			return new Point2D.Float((float) (px + z * rx), (float) (py + z * ry));
		}
	}

	public void removeLine(final Line lineToRemove) {
		lines.remove(lineToRemove);
	}

	public void addEntityInPixelScale(final int entityType, final int x, final int y) {
		ents.add(new Entity(entityType, x*PIXEL_SIZE, y * PIXEL_SIZE));
	}

}
