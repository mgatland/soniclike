package com.matthewgatland.ldengine.engine;

public class MyMath {
	private MyMath() {
		//no instantiation
	}

	public static int signum(final int a) {
		if (a == 0) {
			return 0;
		}
		if (a < 0) {
			return -1;
		}
		return 1;
	}

	public static double angleDifference(final double oldAngle, final double angle) {
		final double difference = (oldAngle + Math.PI - angle) % (2.0*Math.PI) - Math.PI;
		return difference;
	}

	public static double radFromDegrees(final int degrees) {
		return (degrees * Math.PI * 2.0 / 360.0);
	}


	public static int degreesFromRads(final double anAngle) {
		return (int)(anAngle*360.0/2.0/Math.PI);
	}

	public static double fixAngle(final double d) {
		return (d + Math.PI)  % (2.0*Math.PI) - Math.PI;
	}
}
