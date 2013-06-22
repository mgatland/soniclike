package com.matthewgatland.ldengine.engine;

import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.IOException;

import javax.imageio.ImageIO;


public class Art {
	public static BufferedImage[] sonics = split(load("../stolenart/sprites1.png"), 100, 90);

	//again copied from Notch, you don't return the original BufferedImage
	//but create a copy. I don't know why.
	public static BufferedImage load(final String name) {
		Logger.debug("Loading image " + name);
		BufferedImage original;
		try {
			original = ImageIO.read(Art.class.getResource(name));
			final BufferedImage copy = new BufferedImage(original.getWidth(), original.getHeight(), BufferedImage.TYPE_INT_ARGB);
			final Graphics g = copy.getGraphics();
			g.drawImage(original, 0, 0, null, null);
			g.dispose();
			return copy;
		} catch (final IOException e) {
			throw new RuntimeException(e);
		}
	}

	//Copied from Notch
	private static BufferedImage[] split(final BufferedImage src, final int xs, final int ys) {
		final int xSlices = src.getWidth() / xs;
		final int ySlices = src.getHeight() / ys;
		final BufferedImage[] res = new BufferedImage[xSlices*ySlices];
		for (int y = 0; y < ySlices; y++) {
			for (int x = 0; x < xSlices; x++) {
				final int position = x+(y*xSlices);
				res[position] = new BufferedImage(xs, ys, BufferedImage.TYPE_INT_ARGB);
				final Graphics g = res[position].getGraphics();
				g.drawImage(src, -x * xs, -y * ys, null);
				g.dispose();
			}
		}
		return res;
	}
}
