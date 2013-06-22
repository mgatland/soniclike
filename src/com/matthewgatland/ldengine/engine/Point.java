package com.matthewgatland.ldengine.engine;


/**
 * A point in the game world.
 * This is immutable or was at the time I wrote this comment...
 */
public class Point  {
	private final int x;
	private final int y;

	public Point() {
		this(0, 0);
	}

	public Point(final Point p) {
		this(p.x, p.y);
	}

	public Point(final int x, final int y) {
		this.x = x;
		this.y = y;
	}

	public int getX() {
		return x;
	}

	public int getY() {
		return y;
	}

	public Point translate(final int dx, final int dy) {
		return new Point(x + dx, y + dy);
	}

	@Override
	public boolean equals(final Object obj) {
		if (obj instanceof Point) {
			final Point pt = (Point)obj;
			return (x == pt.x) && (y == pt.y);
		}
		return super.equals(obj);
	}

	@Override
	public String toString() {
		return getClass().getName() + "[x=" + x + ",y=" + y + "]";
	}

	@Override
	public int hashCode() {
		return x * 38 + y*7;
	}

}

