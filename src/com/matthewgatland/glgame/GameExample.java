package com.matthewgatland.glgame;

import org.lwjgl.LWJGLException;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.DisplayMode;
import org.lwjgl.opengl.GL11;

import com.matthewgatland.ldengine.engine.Game;
import com.matthewgatland.ldengine.engine.Input;
import com.matthewgatland.ldengine.engine.Logger;
import com.matthewgatland.ldengine.engine.Sound;
import com.matthewgatland.ldengine.sonic2.Sonic2Game;

public class GameExample {
	public static int GAME_WIDTH = 800;
	public static int GAME_HEIGHT = 600;

	private static final int FRAMES_PER_SECOND = 60;
	private static final long FRAME_TIME = 1000000000 / FRAMES_PER_SECOND;
	private static final int MAX_FRAMES = 3; // Maximum game tickets per draw.

	/**
	 * @param args
	 */
	public static void main(final String[] args) {
		createDisplay();
		new GameExample().gameLoop();
	}

	private static void updateDisplay(final Game game) {
		// Clear the screen and depth buffer
		GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
		game.render();
		Display.update();
	}

	public void gameLoop() {
		final Sound sound = new Sound();
		final Game game = new Sonic2Game(sound);
		Logger.log("Game is running");
		long gameTime = System.nanoTime();
		boolean paused = false;
		boolean wasPaused = false;
		while (!Display.isCloseRequested()) {

			final long now = System.nanoTime();
			if (Keyboard.isKeyDown(Keyboard.KEY_1)) {
				if (paused) {
					paused = false;
					wasPaused = true;
				}
			}
			if (Keyboard.isKeyDown(Keyboard.KEY_3)) {
				paused = true;
			}
			boolean advancing = true;
			if (paused && !Keyboard.isKeyDown(Keyboard.KEY_2)) {
				advancing = false;
			}
			int frames = 0;
			while (advancing && now - gameTime > FRAME_TIME) {
				frames++;
				if (!paused && !wasPaused) {
					gameTime += FRAME_TIME;
					if (frames >= MAX_FRAMES) {
						gameTime = now; // no more frames.
					}
				} else {
					gameTime = now; // When paused, we only advance by one
					// frame.
					wasPaused = false;
				}
				Input.tick();
				game.tick();
			}
			updateDisplay(game);
			giveTheCpuABreak();
		}
		Display.destroy();
	}

	private void giveTheCpuABreak() {
		try {
			Thread.sleep(1);
		} catch (final InterruptedException e) {
			e.printStackTrace();
		}
	}

	private static void createDisplay() {
		try {
			Display.setDisplayMode(new DisplayMode(GAME_WIDTH, GAME_HEIGHT));
			Display.create();

			// Set up a 2D view
			GL11.glMatrixMode(GL11.GL_PROJECTION);
			GL11.glLoadIdentity();
			GL11.glOrtho(0, GAME_WIDTH, GAME_HEIGHT, 0, 1, -1);
			GL11.glMatrixMode(GL11.GL_MODELVIEW);
		} catch (final LWJGLException e) {
			e.printStackTrace();
			System.exit(0);
		}
	}

}
