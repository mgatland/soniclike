package com.matthewgatland.glgame;

import static com.matthewgatland.ldengine.sonic2.Scale.PIXEL_SIZE;
import static org.lwjgl.opengl.GL11.GL_LINES;
import static org.lwjgl.opengl.GL11.glBegin;
import static org.lwjgl.opengl.GL11.glEnd;
import static org.lwjgl.opengl.GL11.glVertex3f;

import org.lwjgl.opengl.GL11;

public class GLHelper {
	public static void drawLinePixelScale(final int x1, final int y1, final int x2, final int y2) {
		glBegin(GL_LINES);
		glVertex3f(x1, y1, 0.0f); // origin of the line
		glVertex3f(x2, y2, 0f); // ending point of the line
		glEnd( );
	}

	public static void drawLineFullScale(final int x1, final int y1, final int x2, final int y2) {
		drawLinePixelScale(x1/ PIXEL_SIZE, y1 / PIXEL_SIZE, x2 / PIXEL_SIZE, y2 / PIXEL_SIZE);
	}

	public static void drawQuadPixelScale(final int x, final int y, final int width, final int height) {
		GL11.glBegin(GL11.GL_QUADS);
		GL11.glVertex2f(x, y);
		GL11.glVertex2f(x + width, y);
		GL11.glVertex2f(x + width, y + height);
		GL11.glVertex2f(x, y + height);
		GL11.glEnd();
	}

	public static void drawEmptyQuadPixelScale(final int x, final int y, final int width, final int height) {
		GL11.glBegin(GL11.GL_LINES);
		GL11.glVertex2f(x, y);
		GL11.glVertex2f(x + width, y);
		GL11.glVertex2f(x + width, y + height);
		GL11.glVertex2f(x, y + height);
		GL11.glEnd();
	}

	public static void drawQuadFullScale(final int x, final int y, final int width, final int height) {
		drawQuadPixelScale(x/PIXEL_SIZE, y/PIXEL_SIZE, width/PIXEL_SIZE, height/PIXEL_SIZE);
	}

	public static void drawEmptyQuadFullScale(final int x, final int y, final int width, final int height) {
		drawEmptyQuadPixelScale(x/PIXEL_SIZE, y/PIXEL_SIZE, width/PIXEL_SIZE, height/PIXEL_SIZE);
	}

	public static void setColor(final int r, final int g, final int b) {
		assert(r <= 255);
		assert(g <= 255);
		assert(b <= 255);
		assert(r >= 0);
		assert(g >= 0);
		assert(b >= 0);
		GL11.glColor4b((byte)(r/2),(byte)(g/2),(byte)(b/2), (byte)127); //TODO: it's inefficient creating this every time.
	}
}
