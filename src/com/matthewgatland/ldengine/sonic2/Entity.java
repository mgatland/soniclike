package com.matthewgatland.ldengine.sonic2;

import com.matthewgatland.ldengine.engine.Point;

public class Entity {

	Point pos;
	private final int entityType;

	public Entity(final int entityType, final int x, final int y) {
		this.entityType = entityType;
		pos = new Point(x,y);
		System.out.println("hacks " + this.entityType);
	}

	public Point getPos() {
		return pos;
	}

}
