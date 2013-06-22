package com.matthewgatland.ldengine.engine;

import java.io.IOException;

import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;


public class Sound {

	//TODO: don't expose Clips, hide them inside MyClip with a .play method.
	public Clip startSound;

	public Sound() {
		this.startSound = load("../resource/sound/startSound.wav");
		//play(startSound);
	}

	public void play(final Clip clip) {
		//copied from notch: Make sure the same sound doesn't play twice at the same time?
		clip.stop();
		clip.setFramePosition(0);
		clip.start();
		//not sure if this makes sense without the 'multiple variations' system that he used.
	}

	private Clip load(final String name) {
		Logger.debug("Loading sound " + name);
		Clip clip;
		try {
			clip = AudioSystem.getClip();
			clip.open(AudioSystem.getAudioInputStream(Sound.class.getResourceAsStream(name)));
			return clip;
		} catch (final LineUnavailableException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (final IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (final UnsupportedAudioFileException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
}
