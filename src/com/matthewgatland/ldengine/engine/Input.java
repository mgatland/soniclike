package com.matthewgatland.ldengine.engine;

import java.util.HashSet;
import java.util.Set;

import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import com.matthewgatland.glgame.GameExample;


//Very inefficient because every keypress involves creating an object. But hey it's concise.
public class Input  {

	private static final Set<Integer> keysHit = new HashSet<Integer>(5);
	private static final Set<Integer> keysReleased = new HashSet<Integer>(5);

	private static final Set<Integer> mouseHit = new HashSet<Integer>(3);
	private static final Set<Integer> mouseReleased = new HashSet<Integer>(3);


	private Input() {
		//static to match static Keyboard and Mouse classes.
	}

	private static void setKey(final int code, final boolean state) {
		if (state) {
			keysHit.add(code);
		} else {
			keysReleased.add(code);
		}
	}

	//was the key hit this frame.
	public static boolean getKeyHit(final int code) {
		return keysHit.contains(code);
	}

	//was the key hit this frame.
	public static boolean getKeyReleased(final int code) {
		return keysReleased.contains(code);
	}

	//was the key hit this frame.
	public static boolean getMouseHit(final int code) {
		return mouseHit.contains(code);
	}

	//was the key hit this frame.
	public static boolean getMouseReleased(final int code) {
		return mouseReleased.contains(code);
	}

	//
	/**
	 * Refreshes the key hit\key released status of all keys.
	 * Must be called whenever Display.update() is called
	 * because that updates the underlying Keyboard and Mouse classes.
	 */
	public static void tick() {
		keysHit.clear();
		keysReleased.clear();
		mouseHit.clear();
		mouseReleased.clear();
		while (Keyboard.next()) {
			setKey(Keyboard.getEventKey(), Keyboard.getEventKeyState());
		}
		while (Mouse.next()) {
			setMouse(Mouse.getEventButton(), Mouse.getEventButtonState());
		}
	}

	private static void setMouse(final int code, final boolean state) {
		if (state) {
			mouseHit.add(code);
		} else {
			mouseReleased.add(code);
		}
	}

	public static int mouseX() {
		return Mouse.getX();
	}

	public static int mouseY() {
		return GameExample.GAME_HEIGHT - Mouse.getY();
	}
}

