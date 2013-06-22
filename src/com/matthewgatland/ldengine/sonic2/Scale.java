package com.matthewgatland.ldengine.sonic2;

public class Scale {
	public static final int PIXEL_SIZE = 1000; //the size of the smallest world unit.

	private Scale() {
		//cannot instantiate utility class
	}

	public static int fromPreciseToPixel(final int val) {
		return val/ PIXEL_SIZE;
	}

	public static int fromPixelToPrecise(final int val) {
		return val * PIXEL_SIZE;
	}
}
