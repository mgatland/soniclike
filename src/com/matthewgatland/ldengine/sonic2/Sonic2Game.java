package com.matthewgatland.ldengine.sonic2;

import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;

import com.matthewgatland.glgame.GLHelper;
import com.matthewgatland.ldengine.engine.Game;
import com.matthewgatland.ldengine.engine.Input;
import com.matthewgatland.ldengine.engine.Sound;

public class Sonic2Game implements Sonic2World, Game {

	private final Sound sound;
	private final Sonic2Sonic2 sonic;
	private final Sonic2Level level;
	private final LevelEditor levelEditor;
	private final Camera cam;

	private GameMode mode;

	public enum GameMode {PLAYING, EDITING}

	public Sonic2Game(final Sound sound) {
		mode = GameMode.PLAYING;
		this.sound = sound;
		sonic = new Sonic2Sonic2(this);
		level = new Sonic2Level();
		cam = new Camera();
		this.levelEditor = new LevelEditor(level, cam);
	}


	@Override
	public void tick() {
		if (Keyboard.isKeyDown(Keyboard.KEY_EQUALS)) {
			cam.setScale(cam.getScale() + 0.01f);
		} else	if (Keyboard.isKeyDown(Keyboard.KEY_MINUS)) {
			cam.setScale(cam.getScale() - 0.01f);
		}

		if (Input.getKeyHit(Keyboard.KEY_E)) {
			if (mode == GameMode.PLAYING) {
				mode = GameMode.EDITING;
			} else {
				mode = GameMode.PLAYING;
				levelEditor.clear();
			}
		}
		level.tick();
		sonic.tick();
		if (mode == GameMode.PLAYING) {
			cam.centerCameraOn(sonic.getX(),sonic.getY());
		} else {
			levelEditor.tick();
		}
	}

	@Override
	public void render() {
		/*g.setFont(new Font("Arial", Font.PLAIN, 30));*/
		GL11.glPushMatrix(); //save the default camera position.
		renderHaxxPlayStuff();
		GL11.glScaled(cam.getScale(), cam.getScale(), cam.getScale());
		GL11.glTranslated(cam.getX(), cam.getY(), 0);
		level.render();
		sonic.render();
		if (mode == GameMode.EDITING) {
			levelEditor.render();
		}
		GL11.glPopMatrix(); //undo camera position
	}

	private void renderHaxxPlayStuff() {
		final int x = 100;
		final int y = 100;
		final int width = 100;
		final int height = 200;
		GL11.glBegin(GL11.GL_POLYGON);
		setGradientColor(0, 0);
		GL11.glVertex2f(x, y);
		setGradientColor(width, 0);
		GL11.glVertex2f(x + width, y);
		setGradientColor(width, height);
		GL11.glVertex2f(x + width, y + height);
		setGradientColor(0, height);
		GL11.glVertex2f(x, y + height);
		GL11.glEnd();
	}


	private void setGradientColor(final int x, final int y) {
		float amount = x / 130 + y / 90f;
		if (amount < 0) {
			amount = 0;
		}
		if (amount > 1) {
			amount = 1;
		}
		final int gradColor = (int)(128*(1f-amount)+255*amount);
		GLHelper.setColor(gradColor, gradColor, gradColor);
	}


	@Override
	public Sound getSound() {
		return sound;
	}


	@Override
	public Sonic2Level getLevel() {
		return level;
	}


}
