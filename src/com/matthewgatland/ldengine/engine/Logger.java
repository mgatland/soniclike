package com.matthewgatland.ldengine.engine;

//you did _not_ just roll your own logger class!
public class Logger {

	public static void log(final Object obj) {
		System.out.println(obj.toString());
	}

	public static void debug(final Object obj) {
		System.out.println(obj.toString());
	}

}
