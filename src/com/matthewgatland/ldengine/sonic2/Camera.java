package com.matthewgatland.ldengine.sonic2;

import com.matthewgatland.glgame.GameExample;

public class Camera {

	public int width = GameExample.GAME_WIDTH;
	public int height = GameExample.GAME_HEIGHT;
	private float scale = 1.6f; // 1.6 is Sonic-like at 800x600 pixels.
	private float x;
	private float y;

	public float getScale() {
		return scale;
	}

	public void setScale(final float scale) {
		this.scale = scale;
	}

	public float getX() {
		return x;
	}

	public float getY() {
		return y;
	}

	/** Set the camera position to centre on the given world coordinates*/
	public void centerCameraOn(final int centerX, final int centerY) {
		this.x = -centerX  / Scale.PIXEL_SIZE + width/2/getScale();
		this.y = -centerY  / Scale.PIXEL_SIZE + height/2/getScale();
	}

	public void scrollInPixels(final int i, final int j) {
		this.x -= i;
		this.y -= j;
	}

	public void scrollInScaledPixels(final int i, final int j) {
		this.x -= i / scale;
		this.y -= j / scale;
	}
}
