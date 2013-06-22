package com.matthewgatland.ldengine.sonic2;

import java.awt.geom.Point2D;

import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import com.matthewgatland.glgame.GLHelper;
import com.matthewgatland.ldengine.engine.Input;
import com.matthewgatland.ldengine.engine.Point;
import com.matthewgatland.ldengine.sonic2.Line.LineType;


public class LevelEditor {

	private static final int ENTITY_TYPES = 1;
	private Point dragStartPoint;
	private final Sonic2Level level;
	private final int  minLineLength = 10; //in Pixel scale
	private Point dragTempPoint;
	private Line lineWeMightRemove;
	private Point scrollPoint;
	private int lineType = 0; //type of line to draw
	private final Camera cam;
	private int entType;

	public LevelEditor(final Sonic2Level level, final Camera cam) {
		this.level = level;
		this.cam = cam;
	}

	public void tick() {
		hackyLevelEditTick();
	}

	private void hackyLevelEditTick() {
		final int clickX = (int) (Input.mouseX() / cam.getScale() - cam.getX());
		final int clickY = (int) (Input.mouseY() / cam.getScale() - cam.getY());

		if (Input.getKeyHit(Keyboard.KEY_Y)) {
			entType++;
			if (entType >= ENTITY_TYPES) {
				entType = 0;
			}
		}

		//insert entity
		if (Input.getKeyHit(Keyboard.KEY_I)) {
			level.addEntityInPixelScale(entType, clickX, clickY);
		}

		if (Input.getKeyHit(Keyboard.KEY_T)) {
			lineType++;
			if (lineType > 1) {
				lineType = 0;
			}
		}

		if (Input.getKeyHit(Keyboard.KEY_S)) {
			System.out.println("saving level");
			level.saveLevel("level.txt");
		}

		//Scrolling
		if (Mouse.isButtonDown(2)) {
			final int newX = Input.mouseX();
			final int newY = Input.mouseY();
			if (scrollPoint != null) {
				cam.scrollInScaledPixels(scrollPoint.getX() - newX, scrollPoint.getY() - newY);
			}
			scrollPoint = new Point(newX, newY);
		} else {
			scrollPoint = null;
		}

		//Drawing lines
		if (Input.getMouseHit(0)) {
			dragStartPoint = new Point(clickX, clickY);
		} else if (Input.getMouseReleased(0)) {
			dragTempPoint = null;
			if (dragStartPoint != null) {
				if ((new Point2D.Float(clickX, clickY).distance(dragStartPoint.getX(), dragStartPoint.getY())) > minLineLength) {
					level.addLineInPixelScale(dragStartPoint.getX(), dragStartPoint.getY(), clickX, clickY, lineType == 0 ? LineType.SOLID : LineType.JUMPTHROUGH);
					dragStartPoint = null;
					level.saveLevel("temp.txt");
				}
			}
		}
		if (Mouse.isButtonDown(0)) {
			dragTempPoint = new Point(clickX, clickY);
		}

		//Deleting
		lineWeMightRemove = null;
		if (Mouse.isButtonDown(1)) {
			lineWeMightRemove = level.findLineClosestToInPixelScale(clickX, clickY, 40);
		}
		if (Input.getMouseReleased(1)) {
			lineWeMightRemove = level.findLineClosestToInPixelScale(clickX, clickY, 40);
			if (lineWeMightRemove == null) {
				System.out.println("No line close enough to delete");
			} else {
				level.removeLine(lineWeMightRemove);
				level.saveLevel("temp.txt");
			}
		}
	}

	public void clear() {
		dragStartPoint = null;
	}

	public void render() {
		if (dragStartPoint != null && dragTempPoint != null) {
			GLHelper.setColor(0, 255, 255);
			GLHelper.drawLinePixelScale(dragStartPoint.getX(), dragStartPoint.getY(), dragTempPoint.getX(), dragTempPoint.getY());
		}

		if (lineWeMightRemove != null) {
			GLHelper.setColor(255, 0, 0);
			GLHelper.drawLineFullScale(lineWeMightRemove.x1, lineWeMightRemove.y1, lineWeMightRemove.x2, lineWeMightRemove.y2);
		}
		/*
		g.setColor(Color.white);
		g.setFont(new Font("Helvetica", Font.PLAIN, 15));
		g.drawString("EDITING", 40 - (int)cam.getX(), 40 - (int)cam.getY());
		g.drawString("LINE TYPE " + lineType, 40 - (int)cam.getX(), 60 - (int)cam.getY());
		 */}
}
