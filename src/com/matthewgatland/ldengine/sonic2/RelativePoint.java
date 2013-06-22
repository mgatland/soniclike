package com.matthewgatland.ldengine.sonic2;

import com.matthewgatland.ldengine.engine.Point;

/**
 * A point with helper methods to deal with a rotated actor.
 * 'right' means "X from my point of view"
 * "down" means 'Y from my point of view"
 * 
 * Note that even when upside down, a higher value of 'down' still means down.
 * However using the relativeTranslate method will subtract 'down' instead of adding
 * when upside-down.
 *
 */
public class RelativePoint extends Point {

	private final MotionState state;

	public RelativePoint(final int x, final int y, final MotionState state) {
		super(x,y);
		this.state = state;
	}

	public RelativePoint(final Point p, final MotionState state) {
		super(p);
		this.state = state;
	}

	public int getRight() {
		if (state.isHorzontal()) {
			return getX();
		} else {
			return getY();
		}
	}

	public int getDown() {
		if (state.isHorzontal()) {
			return getY();
		} else {
			return getX();
		}
	}

	public RelativePoint translateRelative(final int right, final int down) {
		if (state.isHorzontal()) {
			return new RelativePoint(getX()+right*state.getDir(), getY()+down*state.getDir(), state);
		} else {
			return new RelativePoint(getX()+down*state.getDir(), getY()+right*state.getDir(), state);
		}
	}

	public static RelativePoint FromRelative(final int right, final int down, final MotionState state) {
		if (state.isHorzontal()) {
			return new RelativePoint(right, down, state);
		} else {
			return new RelativePoint(down, right, state);
		}
	}
}
