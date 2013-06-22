package com.matthewgatland.ldengine.test;

import junit.framework.Assert;

import org.junit.Test;

import com.matthewgatland.ldengine.sonic2.Scale;


public class AllTests {

	@Test
	public void testMath() {
		Assert.assertEquals(0, Scale.fromPreciseToPixel(0));
		Assert.assertEquals(0, Scale.fromPreciseToPixel(1));
		Assert.assertEquals(0, Scale.fromPreciseToPixel(499));
		Assert.assertEquals(1, Scale.fromPreciseToPixel(500));
		Assert.assertEquals(1, Scale.fromPreciseToPixel(501));
		Assert.assertEquals(1, Scale.fromPreciseToPixel(999));
		Assert.assertEquals(1, Scale.fromPreciseToPixel(1000));
		Assert.assertEquals(2, Scale.fromPreciseToPixel(1001));

	}
}
