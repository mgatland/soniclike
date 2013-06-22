package com.matthewgatland.ldengine.sonic2;

import com.matthewgatland.ldengine.engine.Sound;

/**
 * World is the game from the point of view of a game object inside the world.
 * World can play sounds and provide input.
 * World cannot, for example, tick. A game object cannot make the world tick.
 */
public interface Sonic2World {

	Sound getSound();

	Sonic2Level getLevel();
}
