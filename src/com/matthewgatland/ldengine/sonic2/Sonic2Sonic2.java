package com.matthewgatland.ldengine.sonic2;

import static com.matthewgatland.ldengine.sonic2.Scale.PIXEL_SIZE;

import java.awt.geom.Line2D;

import org.lwjgl.input.Keyboard;

import com.matthewgatland.glgame.GLHelper;
import com.matthewgatland.ldengine.engine.Input;
import com.matthewgatland.ldengine.engine.Logger;
import com.matthewgatland.ldengine.engine.MyMath;
import com.matthewgatland.ldengine.engine.Point;
import com.matthewgatland.ldengine.sonic2.Sonic2Level.CollisionType;

public class Sonic2Sonic2 {

	private static final double ANGLE_90_DEGREES = MyMath.radFromDegrees(90);
	//TODO: limit absolute maximum x and y speed to 16000 before moving.
	//This stops Sonic from falling through entire blocks.

	Point debugPoint;
	private static boolean moveDebug = false;

	private static final int RIGHT = 1;
	private static final int LEFT = -1;

	//TODO: replace with an UpOrDown enum.
	private static final int SCAN_UP = -1;
	private static final int SCAN_DOWN = 1;

	private boolean isRolling = false;

	private final Sonic2World world;

	private int x;
	private int y;

	private final int midHeight = 20000;

	private int groundSpeed;
	private double angle; //TODO: change to integer in degrees.
	private final int groundAcceleration = 47*4/3; //WAS 47 IN SONIC THE HEDGEHOG acceleration by pressing forwards.
	/** deceleration due to not pressing any button	 */
	private final int groundIdleDeceleration = 47; //Called 'Friction' by the Sonic Retro Physics Guide
	private final int groundDeceleration = 500; //deceleration by pressing backwards
	private final int groundMaxSpeedNormal = 6000; //max speed by normal acceleration.
	private final int trudgeSpeed = 2000; //max speed sonic can 'trudge' (walk uphill without slipping) at
	private final int widthWhenPushing = 20000;
	private final int groundSensorWidth = 18000; //with between ground sensors, we can fall down holes this wide
	private final int groundSensorDepth = midHeight + 16000;
	private final int  slopeFactor = moveDebug ? 1 : 125; //effect of slopes on running speed.
	private final int  slopeTrudgeFactor = 0; //lesser effect of slopes while trudging uphil.
	private final int fallOffSpeed = 2500;
	private final int maxGroundControlLockTimer = 30; //in frames, at 60 FPS
	private int groundControlLockTimer; // Makes left and right keys have no effect while on the ground.

	private final int rollingIdleDeceleration = groundIdleDeceleration / 2; //Called 'Friction' by the Sonic Retro Physics Guide
	private final int rollingDeceleration = 125;
	private final int minSpeedToStartRolling = 1000; //Sonic 3 style
	private final int minSpeedToAllowRolling = 500; //Sonic 3 style - you stop rolling if speed drops below this.

	private final int airAcceleration = groundAcceleration * 2;
	private final int airMaxSpeedNormal = groundMaxSpeedNormal; //max speed by air control.
	private final int airDragMultiplier = 969; //air x speed is multiplied by this/1000;
	private final int airDragMinSpeed = 125; //no drag when moving slower than this.

	private final int gravity = 219;
	private final int jumpSpeed = 6500;
	private final int jumpReleaseSpeed = 4000;
	private boolean isJumping;

	private MotionState state;
	private int xSpeed;
	private int ySpeed;



	public Sonic2Sonic2(final Sonic2World world) {
		this.world = world;
		x = 100*1000;
		y = 100*1000;
		state = MotionState.GROUND;
	}



	public void tick() {
		final boolean left = Keyboard.isKeyDown(Keyboard.KEY_LEFT);
		final boolean right = Keyboard.isKeyDown(Keyboard.KEY_RIGHT);
		final boolean jump = Keyboard.isKeyDown(Keyboard.KEY_Z);
		final boolean key_boost = Input.getKeyHit(Keyboard.KEY_X);
		final boolean up = Keyboard.isKeyDown(Keyboard.KEY_UP);
		final boolean down = Keyboard.isKeyDown(Keyboard.KEY_DOWN);

		//Movement arrow keys only work when the opposite arrow is not held.
		final boolean moveLeft = left && !right;
		final boolean moveRight = right && !left;

		if (state.isGrounded()) {
			if (groundControlLockTimer > 0) {
				groundControlLockTimer--;
			} else {
				if (down && !isRolling && Math.abs(groundSpeed) > minSpeedToStartRolling) {
					isRolling = true;
				} //TODO: consider automatically rolling if speed gets too high - more like sliding
			}

			if (Math.abs(groundSpeed) < minSpeedToAllowRolling) {
				isRolling = false;
			}

			if (isRolling) {
				adjustSpeedGroundedAndRolling(moveLeft, moveRight);
			} else {
				adjustSpeedGroundedAndRunning(moveLeft, moveRight);
			}

			displaySpeedHack();
			tryDoSpeedBoostHack(left, right, key_boost);

			//FIXME: while walking slowly uphill, upright,
			//Sonic should 'trudge' with reduced slipping.
			//This allow Sonic to walk slowly up steep slopes.
			if (state == MotionState.GROUND
					&& Math.abs(groundSpeed) < trudgeSpeed
					&& Math.signum(Math.sin(angle)) == Math.signum(groundSpeed)
					&& (moveLeft || moveRight)
					&& groundControlLockTimer == 0) {
				groundSpeed -= (int)(slopeTrudgeFactor *Math.sin(angle));
			} else {
				groundSpeed -= (int)(slopeFactor *Math.sin(angle));
			}
			xSpeed = (int)(groundSpeed*Math.cos(angle));
			ySpeed = (int)(groundSpeed*-Math.sin(angle));
			x += xSpeed;
			y += ySpeed;

			checkForWalls(RIGHT);
			checkForWalls(LEFT);
			checkForCeiling();
			checkForGround();

			if (jump) {
				state = MotionState.AIRBORN;

				ySpeed /= 2; //Hacks: half y component because otherwise the vertical speed boost gets silly.
				xSpeed -= (int)(jumpSpeed*Math.sin(angle));
				ySpeed -= (int)(jumpSpeed*Math.cos(angle));

				// And an angled component
				final int jumpDir = moveLeft ? 1 : moveRight ? -1 : 0;
				if (jumpDir != 0) {
					final double jumpAngle = MyMath.fixAngle(angle + jumpDir * ANGLE_90_DEGREES);
					xSpeed -= (int) (jumpSpeed / 5 * Math.sin(jumpAngle));
					ySpeed -= (int) (jumpSpeed / 5 * Math.cos(jumpAngle));
				}

				groundSpeed = 0;
				isJumping = true;
			} else if ((angle > radFromDegrees(60) || angle < radFromDegrees(-60)) && Math.abs(groundSpeed) < fallOffSpeed && groundControlLockTimer== 0) {
				//slipping - or falling right off if we're upside down
				if (angle > radFromDegrees(100) || angle < radFromDegrees(-100)) {
					Logger.debug("Fell off");
					state = MotionState.AIRBORN;
					groundControlLockTimer = maxGroundControlLockTimer;
				} else if (ySpeed < 0) { //only slip if we're actually moving upwards.
					Logger.debug("slipping");
					groundSpeed = 0;
					groundControlLockTimer = maxGroundControlLockTimer;
				}
			}

		} else if (state == MotionState.AIRBORN) {
			if (isJumping && !jump) {
				//jump key was released - slow upwards speed.
				isJumping = false;
				if (ySpeed < -jumpReleaseSpeed) {
					ySpeed = -jumpReleaseSpeed;
				}
			}

			if (moveRight) {
				flyInThisDirection(RIGHT);
			} else if (moveLeft) {
				flyInThisDirection(LEFT);
			}

			//drag
			if (Math.abs(xSpeed) > airDragMinSpeed) {
				xSpeed = xSpeed * airDragMultiplier / 1000;
			}

			ySpeed += gravity;
			x += xSpeed;
			y += ySpeed;

			checkForWalls(RIGHT);
			checkForWalls(LEFT);
			checkForCeiling();
			checkForGround();

			if (state.isGrounded()) {
				//we have landed. Always stand up after landing.
				isRolling = false;
			}
		}

		if (up) {
			world.getSound().play(world.getSound().startSound);
			x = 100 * 1000;
			y = 100*1000;
		}

		animate();

	}



	private void animate() {
		animDelay+= (Math.abs(groundSpeed));
		if (animDelay >14000) {
			animDelay = 0;
			animFrame++;
			if (animFrame > 7) {
				animFrame = 0;
			}
		}
	}

	/** just for a debug mode speed tracking display */
	int speedoTempHack  = 0;
	private void displaySpeedHack() {
		final int tmpHackSpeed = groundSpeed / 1000;
		if (tmpHackSpeed != speedoTempHack) {
			speedoTempHack = tmpHackSpeed;
			Logger.debug("Ground speed: " + tmpHackSpeed);
		}
	}

	private void tryDoSpeedBoostHack(final boolean left, final boolean right, final boolean key_boost) {
		// The boost key instantly accelerates to the maximum (normal)
		// speed.
		if (key_boost) {
			int dir;
			if (left && !right) {
				dir = LEFT;
			} else if (right && !left) {
				dir = RIGHT;
			} else {
				dir = MyMath.signum(groundSpeed);
			}
			if (groundSpeed * dir < groundMaxSpeedNormal) {
				groundSpeed = dir * groundMaxSpeedNormal;
			}
		}
	}

	private void adjustSpeedGroundedAndRunning(final boolean left, final boolean right) {
		// intentional acceleration or deceleration
		if (right && groundControlLockTimer == 0) {
			runInThisDirection(RIGHT);
		} else if (left && groundControlLockTimer == 0) {
			runInThisDirection(LEFT);
		} else {
			// slow down gradually due to 'friction'.
			final int absDecelerationAmount = Math.min(Math.abs(groundSpeed), groundIdleDeceleration);
			groundSpeed = groundSpeed - absDecelerationAmount * MyMath.signum(groundSpeed);
		}
	}

	private void adjustSpeedGroundedAndRolling(final boolean left, final boolean right) {
		// intentional acceleration or deceleration
		if (right && groundControlLockTimer == 0) {
			rollDecelerateInThisDirection(RIGHT);
		} else if (left && groundControlLockTimer == 0) {
			rollDecelerateInThisDirection(LEFT);
		}
		// unlike while running, rolling is always affected by 'friction'
		// (even when an arrow key is held down)
		final int absDecelerationAmount = Math.min(Math.abs(groundSpeed), rollingIdleDeceleration);
		groundSpeed = groundSpeed - absDecelerationAmount * MyMath.signum(groundSpeed);
	}


	private void checkForCeiling() {
		final RelativePoint p1 = groundOrCeilingScan(LEFT * groundSensorWidth / 2, SCAN_UP);
		final RelativePoint p2 = groundOrCeilingScan(RIGHT * groundSensorWidth / 2, SCAN_UP);
		if (p1 == null && p2 == null) {
			return; //no ceiling
		}

		final int ceilingHeight = Math.max(p1 != null? p1.getDown() : Integer.MIN_VALUE, p2 != null ? p2.getDown() : Integer.MIN_VALUE);
		final int myMaxHeight = ceilingHeight + state.getDir() * (midHeight + PIXEL_SIZE*2);

		//TODO: clean up rotation code.
		if (state.isHorzontal()) { //we're upright or upside-down
			if (y * state.getDir() < myMaxHeight * state.getDir()) {
				y = myMaxHeight;
				if (ySpeed * state.getDir() < 0) {
					ySpeed = 0;
				}
				isJumping = false; // cancel jump
			}
		} else { //we're sideways
			if (x * state.getDir() < myMaxHeight * state.getDir()) {
				x = myMaxHeight;
				if (xSpeed * state.getDir() < 0) {
					xSpeed = 0;
				}
				isJumping = false; // cancel jump
			}
		}
	}

	private void checkForGround() {
		final double oldAngle = angle;
		final Point p1 = groundOrCeilingScan(LEFT * groundSensorWidth / 2, SCAN_DOWN);
		final Point p2 = groundOrCeilingScan(RIGHT * groundSensorWidth / 2, SCAN_DOWN);
		final RelativePoint rel = new RelativePoint(x, y, state);

		if (p1 == null && p2 == null) {
			if (state.isGrounded()) {
				state = MotionState.AIRBORN;
			}
			//TODO: wobble on edges when one sensor returns null AND
			//my center point is over the edge. Only when upright.
		} else {
			//we found ground.
			final int groundY = getRotGroundYAndSetAngle(p1, p2); //groundY is a rotated coordinate.

			//final double angleChange = MyMath.angleDifference(oldAngle, angle);
			//Logger.debug(degreesFromRads(angleChange));

			// Note: because the ySpeed in the next line only applies while
			// falling, it doesn't need to be rotation aware.
			if (state.isGrounded() || ySpeed >= 0) { // when airborn, only hit ground while falling.
				final int myLandedY = groundY - (midHeight + PIXEL_SIZE) * state.getDir();
				// if grounded, we get sucked down - otherwise only pushed
				// up.
				if ((state.isGrounded()) || rotGreaterThan(rel.getDown(), myLandedY)) {
					final Point newRel = RelativePoint.FromRelative(rel.getRight(), myLandedY, state);
					setPositionFromPoint(newRel);
					if (!state.isGrounded()) { // We just landed, from
						// airborn (and therefore
						// from upright)
						int newGroundSpeed;
						Logger.debug(String.format("land angle %d", MyMath.degreesFromRads(angle)));
						state = MotionState.GROUND;
						if (angle < radFromDegrees(22) && angle > radFromDegrees(-22)) {
							newGroundSpeed = xSpeed;
						} else if (angle < radFromDegrees(45) && angle > radFromDegrees(-45)) {
							if (Math.abs(ySpeed) > Math.abs(xSpeed)) {
								newGroundSpeed = (int) (ySpeed * 0.5 * -Math.signum(angle));
							} else {
								newGroundSpeed = xSpeed;
							}
						} else { // for angles up to 90 degrees and -90
							// degrees.
							if (Math.abs(ySpeed) > Math.abs(xSpeed)) {
								newGroundSpeed = (int) (ySpeed * -Math.signum(angle));
							} else {
								newGroundSpeed = xSpeed;
							}
						}
						groundSpeed = newGroundSpeed;
						/**
						 *TODO When Moving Upward When the ceiling angle
						 * detected is in the range of 90 to 133 degrees,
						 * Sonic reattaches to the ceiling, and Gsp is set
						 * to Ysp*-sign(cos(angle)). When the angle is in
						 * the range of 133 to 222 degrees, Sonic hits his
						 * head like any ceiling, and doesn't reattach to
						 * it. Ysp is set to 0, and Xsp is unaffected.
						 */
					}
				}
			}
			rotateIfAppropriate(oldAngle);
		}
	}



	private void rotateIfAppropriate(final double oldAngle) {
		if (state.isGrounded()) {
			final MotionState oldState = state;
			state = getBestStateForAngle();
			if (state != oldState) {
				final double preStateChangeAngle = angle;
				//Test our new rotation is actually going to stick us to something - undo if it isn't.
				final Point newP1 = groundOrCeilingScan(LEFT * groundSensorWidth / 2, SCAN_DOWN);
				final Point newP2 = groundOrCeilingScan(RIGHT * groundSensorWidth / 2, SCAN_DOWN);
				boolean rotationIsValid;
				if (newP1 == null && newP2 == null) {
					rotationIsValid = false;
				} else {
					//this sets the angle.
					getRotGroundYAndSetAngle(newP1, newP2);
					if (getBestStateForAngle() == state) {
						rotationIsValid = true;
					} else {
						rotationIsValid = false;
					}
				}

				if (!rotationIsValid) {
					angle = preStateChangeAngle;
					Logger.debug("Cancel rotation state change");
					angle = oldAngle;
					state = oldState;
				}
			}
		}
	}




	private MotionState getBestStateForAngle() {
		if (angle > radFromDegrees(45) && angle < radFromDegrees(135)) {
			return MotionState.RIGHT_WALL;
		} else if (angle <= radFromDegrees(45) && angle >= radFromDegrees(-45)) {
			return MotionState.GROUND;
		} else if (angle < radFromDegrees(-45) && angle > radFromDegrees(-135)) {
			return MotionState.LEFT_WALL;
		} else {
			return MotionState.UPSIDE_DOWN;
		}
	}



	//returns greater than, unless we are backwards -then it returns less than.
	private boolean rotGreaterThan(final int a, final int b) {
		if (state.getDir() > 0) {
			return a > b;
		} else {
			return a < b;
		}
	}



	private void setPositionFromPoint(final Point p) {
		x = p.getX();
		y = p.getY();
	}

	//Get the minimum "y" considering my rotation.
	//if I'm sideways it will really be an x.
	//If i'm backwards it will really be a maximum.
	private int rotMinY(final Point p1, final Point p2) {
		final RelativePoint p1R = new RelativePoint(p1, state);
		final RelativePoint p2R = new RelativePoint(p2, state);
		if (state.getDir() == 1) {
			return Math.min(p1R.getDown(), p2R.getDown());
		} else {
			return Math.max(p1R.getDown(), p2R.getDown());
		}
	}

	//TODO: this is not how the original game calculated angles!
	//it just uses the angle of the highest cell.
	/**
	 * This method has the side effect of setting the angle variable.
	 */
	private int getRotGroundYAndSetAngle(final Point p1, final Point p2) {
		assert(p1 != null || p2 != null);
		final int groundY;
		if (p1 != null && p2 != null) {
			setAngleFromGroundSensors(p1, p2);
			groundY = rotMinY(p1, p2);
		} else {
			//we only have one leg on the ground.
			//let's try to find some more ground so we can calculate an angle.
			final Point pOld;
			final int dir;
			Point p3 = null;
			if (p1 != null) {
				pOld = p1;
				dir = -1;
			} else {
				pOld = p2;
				dir = 1;
			}
			int searchPos = dir *( groundSensorWidth / 2 - 3000);
			while (p3 == null && Math.abs(searchPos) > 300) {
				p3 = groundOrCeilingScan(searchPos, SCAN_DOWN);
				searchPos -= dir*2000;
			}
			//one last chance to find a flat platform - on the far side.
			boolean swappedSides = false;
			if (p3 == null) {
				p3 = groundOrCeilingScan(dir *( groundSensorWidth / 2 + 1500), SCAN_DOWN);
				swappedSides = true;
			}
			if (p3 != null) {
				//rise is always left minus right
				if (p1 != null != swappedSides) {
					setAngleFromGroundSensors(pOld, p3);
				} else {
					setAngleFromGroundSensors(p3, pOld);
				}
				groundY = rotMinY(p3, pOld);
			} else {
				//no luck. We only have one point to stand on. Cannot compute an angle.
				angle = state.getDefaultAngle(); //these default angles are unneccessary, added for no reason. remove them.
				groundY = rotMinY(pOld, pOld);
			}
		}
		return groundY;
	}

	private double radFromDegrees(final int degrees) {
		return (degrees * Math.PI * 2.0 / 360.0);
	}

	private void setAngleFromGroundSensors(final Point left, final Point right) {
		final int rise;
		final int width;
		if (state.isHorzontal()) {
			if (state.getDir() > 0) {
				rise =  left.getY() - right.getY();
				width = right.getX() - left.getX();
				angle = (Math.atan(1.0*rise/width));
			} else {
				rise =  left.getY() - right.getY();
				width = right.getX() - left.getX();
				angle = radFromDegrees(-180) +(Math.atan(1.0*rise/width));
			}
		} else {
			if (state.getDir() > 0) {
				rise = left.getX() - right.getX();
				width = right.getY() - left.getY();
				angle = radFromDegrees(90) - (Math.atan(1.0*rise/width));
			} else {
				rise = right.getX() - left.getX();
				width = right.getY() - left.getY();
				angle = radFromDegrees(-90) + (Math.atan(1.0*rise/width));
			}
		}
		while (angle < -Math.PI){
			angle += 2.0*Math.PI;
		}
		while (angle > Math.PI){
			angle -= 2.0*Math.PI;
		}
	}

	/**
	 * Scan for ground below or ceiling above.
	 */
	private RelativePoint groundOrCeilingScan(final int offset, final int upOrDown) {
		final RelativePoint rel = new RelativePoint(x, y, state);
		final int sensorRight = rel.getRight() + offset;
		final int depth = groundSensorDepth * state.getDir() * upOrDown;
		final RelativePoint start = RelativePoint.FromRelative(sensorRight, rel.getDown(), state);
		final RelativePoint end = RelativePoint.FromRelative(sensorRight, rel.getDown() + depth, state);
		final Sonic2Level.CollisionType scanType = (upOrDown == -1) ? CollisionType.SOLID : CollisionType.ALL;
		final Point hitPoint = world.getLevel().traceForCollisions(new Line2D.Float(start.getX(), start.getY(), end.getX(), end.getY()), scanType);
		if (hitPoint != null) {
			return new RelativePoint(hitPoint, state);
		}
		return null;
	}

	private void setRight(final int right) {
		if (state.isHorzontal()) {
			x = right;
		} else {
			y = right;
		}
	}

	private int getRightSpeed() {
		if (state.isHorzontal()) {
			return xSpeed;
		} else {
			return ySpeed;
		}
	}

	private void setRightSpeed(final int rightSpeed) {
		if (state.isHorzontal()) {
			xSpeed = rightSpeed;
		} else {
			ySpeed = rightSpeed;
		}
	}

	private void checkForWalls(final int dir) {
		//hackySize because there's something going on that I don't understand :(
		int hackySize = widthWhenPushing / 2 + PIXEL_SIZE;
		if (dir == LEFT) {
			hackySize += 1 * PIXEL_SIZE;
		}
		final Sonic2Level level = world.getLevel();

		RelativePoint closestPoint = null;

		final int numberOfScans = 4;
		for (int i = 0; i < numberOfScans; i++) {
			final RelativePoint scanStart = new RelativePoint(x, y, state).translateRelative(0, 4000 - i*(midHeight+3000)/(numberOfScans - 1));
			final RelativePoint scanEnd = scanStart.translateRelative(hackySize * dir, 0);
			final Point point = level.traceForCollisions(new Line2D.Float(scanStart.getX(), scanStart.getY(), scanEnd.getX(), scanEnd.getY()), CollisionType.SOLID);
			if (point != null) {
				final RelativePoint newPoint = new RelativePoint(point, state);
				if (closestPoint == null || closestPoint.getRight() * dir * state.getDir()  > newPoint.getRight() * dir * state.getDir() ) {
					closestPoint = newPoint;
				}
			}
		}

		if (closestPoint != null) {
			setRight(closestPoint.getRight() - dir * state.getDir() * hackySize);
			if (getRightSpeed() * dir * state.getDir() > 0) {
				setRightSpeed(0);
			}
			//this intentionally ignores state direction because
			//ground speed is not affected by that (it's based on angles)
			if (groundSpeed * dir * (state.isHorzontal() ? 1 : -1) > 0) {
				groundSpeed = 0;
			}
		}
	}

	private void flyInThisDirection(final int dir) {
		if (dir * xSpeed < airMaxSpeedNormal) {
			xSpeed += dir * airAcceleration;
			if (dir * xSpeed > airMaxSpeedNormal) {
				xSpeed = dir * airMaxSpeedNormal;
			}
		}
	}

	private void runInThisDirection(final int dir) {
		if (dir*groundSpeed >= 0) {
			if (dir*groundSpeed < groundMaxSpeedNormal) {
				groundSpeed += dir*groundAcceleration;
				if (dir*groundSpeed > groundMaxSpeedNormal) {
					groundSpeed = dir*groundMaxSpeedNormal;
				}
			}
		} else {
			groundSpeed += dir*groundDeceleration;
		}
	}

	private void rollDecelerateInThisDirection(final int dir) {
		if (dir*groundSpeed >= 0) {
			//You cannot accelerate while rolling.
		} else {
			groundSpeed += dir*rollingDeceleration;
		}
	}

	public void render() {
		final int halfHeight = (state.isHorzontal() ? midHeight : widthWhenPushing / 2);
		final int halfWidth = (state.isHorzontal() ? widthWhenPushing / 2 : midHeight);
		if (groundControlLockTimer > 0) {
			GLHelper.setColor(64,64,64);
		} else {
			GLHelper.setColor(128,128,255);
		}
		GLHelper.drawQuadFullScale((x-halfWidth), (y-halfHeight), halfWidth*2, halfHeight*2);
		GLHelper.setColor(192,192,192);
		switch (state) {
			case AIRBORN:
				break;
			case GROUND:
				debugDrawRect(-5,0,10,20);
				break;
			case LEFT_WALL:
				debugDrawRect(-10,-5,10,10);
				break;
			case RIGHT_WALL:
				debugDrawRect(0,-5,10,10);
				break;
			case UPSIDE_DOWN:
				debugDrawRect(-5,-20,10,20);
				break;
		}

		GLHelper.setColor(255, 255, 255);
		GLHelper.drawEmptyQuadFullScale(x-2000,y-2000,5000,5000);
		GLHelper.setColor(255, 0, 0);
		GLHelper.drawEmptyQuadFullScale((x-halfWidth), (y-halfHeight), halfWidth*2, halfHeight*2);


		if (isRolling) {
			//g.drawArc((x-halfWidth)/PIXEL_SIZE, (y-halfHeight)/PIXEL_SIZE, halfWidth*2/PIXEL_SIZE, halfHeight*2/PIXEL_SIZE, 0, 360);
		}

		if (debugPoint != null) {
			GLHelper.setColor(255, 200, 0); //ORANGE
			GLHelper.drawQuadFullScale(debugPoint.getX(), debugPoint.getY(),1000, 1000);
		}

		//final int renderFrame = (groundSpeed == 0 ? 0 : 12 + animFrame);
		/*g.drawImage(Art.sonics[renderFrame], x/PIXEL_SIZE-96/4, y/PIXEL_SIZE-96/4, x/PIXEL_SIZE+96/2-96/4, y/PIXEL_SIZE+96/2-96/4, 0, 0, 96, 96, null);
		 */}

	private int animFrame = 0;
	private int animDelay = 0;

	private void debugDrawRect( final int xOff, final int yOff, final int sizeX, final int sizeY) {
		GLHelper.drawQuadFullScale(x+xOff*PIXEL_SIZE, y+yOff*PIXEL_SIZE, sizeX*PIXEL_SIZE, sizeY*PIXEL_SIZE);
	}



	public int getX() {
		return x;
	}

	public int getY() {
		return y;
	}
}
