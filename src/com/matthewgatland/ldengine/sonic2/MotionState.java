package com.matthewgatland.ldengine.sonic2;

public enum MotionState {
	GROUND(true, true, 1, 0),
	UPSIDE_DOWN(true, true, -1, Math.PI),
	LEFT_WALL(true, false, -1, -Math.PI / 2.0),
	RIGHT_WALL(true, false, 1, Math.PI / 2.0),
	AIRBORN(false, true, 1, 0);

	private boolean grounded;
	private boolean horizontal;
	private int dir;
	private double defaultAngle;

	public boolean isGrounded() {
		return grounded;
	}

	public boolean isHorzontal() {
		return horizontal;
	}

	private MotionState(final boolean grounded, final boolean horizontal, final int dir, final double defaultAngle) {
		this.grounded = grounded;
		this.horizontal = horizontal;
		this.dir = dir;
		this.defaultAngle = defaultAngle;
	}

	public int getDir() {
		return dir;
	}

	public double getDefaultAngle() {
		return defaultAngle;
	}
}
