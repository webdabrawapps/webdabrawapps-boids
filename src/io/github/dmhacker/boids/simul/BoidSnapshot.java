package io.github.dmhacker.boids.simul;

import io.github.dmhacker.boids.util.Vec2d;

/**
 * 
 * Represents a boid frozen in time. 
 * Used to update the boids all at once rather than in sequence
 * 
 * @author David Hacker
 *
 */
public class BoidSnapshot {
	private final Vec2d pos;
	private final Vec2d vel;
	private final double distance;
	
	public BoidSnapshot(Boid boid, double distance) {
		this.pos = boid.getPosition();
		this.vel = boid.getVelocity();
		this.distance = distance;
	}
	
	public Vec2d getPosition() {
		return pos;
	}
	
	public Vec2d getVelocity() {
		return vel;
	}
	
	public double getDistance() {
		return distance;
	}
}
