package com.matthewgatland.ldengine.sonic2;

import java.awt.geom.Line2D;

import com.matthewgatland.ldengine.engine.MyMath;

public class Line {
	private final Line2D line2d;
	private final double angle;
	private final LineType type;

	public enum LineType {JUMPTHROUGH, SOLID}

	public Line(final int x1, final int y1, final int x2, final int y2) {
		this(x1, y1, x2, y2, LineType.SOLID);
	}

	public Line(final int x1, final int y1, final int x2, final int y2, final LineType type) {
		super();
		this.x1 = x1;
		this.y1 = y1;
		this.x2 = x2;
		this.y2 = y2;
		this.type = type;
		line2d = new Line2D.Float(x1, y1, x2, y2);
		final int rise =  y1- y2;
		final int width = x2 - x1;
		double tempAngle = (Math.atan(1.0*rise/width));
		if (width < 0) {
			tempAngle = MyMath.radFromDegrees(180) + tempAngle;
		}
		while (tempAngle < -Math.PI){
			tempAngle += 2.0*Math.PI;
		}
		while (tempAngle > Math.PI){
			tempAngle -= 2.0*Math.PI;
		}
		angle = tempAngle;
		System.out.println(MyMath.degreesFromRads(angle));
	}
	public int x1;
	public int y1;
	public int x2;
	public int y2;

	public Line2D getLine2D() {
		return line2d;
	}

	public LineType getType() {
		return type;
	}

	public int getTypeAsInt() {
		return type == LineType.SOLID ? 0 : 1;
	}
}
