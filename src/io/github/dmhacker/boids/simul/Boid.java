package io.github.dmhacker.boids.simul;

import java.awt.Color;
import java.awt.MouseInfo;
import java.util.List;
import java.util.UUID;

import io.github.dmhacker.boids.graphics.BoidsPanel;
import io.github.dmhacker.boids.util.Vec2d;

public class Boid {
	public static final double MAXIMUM_VELOCITY_MAGNITUDE = 0.01;
	public static final double NEIGHBOR_RADIUS = 0.2;
	public static final double SEPARATION_RADIUS = 0.11;
	public static final double OBSTACLE_RADIUS = 0.16;
	
	private static final Color DENSE = Color.RED;
	private static final Color OPEN = Color.GREEN;
	
	private Vec2d pos;
	private Vec2d vel;
	private Vec2d acc;
	private Color color;
	private final UUID id;
	
	private double density;
	
	public Boid(Vec2d pos) {
		this(pos, new Vec2d(Math.random() * 2 - 1, Math.random() * 2 - 1));
	}
	
	public Boid(Vec2d pos, Vec2d vel) {
		this.pos = pos;
		this.vel = vel.normalize().scale(MAXIMUM_VELOCITY_MAGNITUDE * (Math.random() * 0.99 + 0.01));
		this.color = Color.BLACK;
		this.id = UUID.randomUUID();
		this.acc = new Vec2d(0, 0);
	}
	
	public void update(BoidsPanel panel, List<BoidSnapshot> nearby, List<Vec2d> obstacles) {
		
		Vec2d acceleration = new Vec2d(0, 0);
		
		Vec2d centerOfMass = pos;
		Vec2d averageHeading = vel;

		for (BoidSnapshot snapshot : nearby) {
			centerOfMass = centerOfMass.add(snapshot.getPosition());
			averageHeading = averageHeading.add(snapshot.getVelocity());
			if (snapshot.getDistance() <= SEPARATION_RADIUS) {
				acceleration = acceleration.add(pos.subtract(snapshot.getPosition()).scale(0.00002 / Math.pow(snapshot.getDistance(), 2.2)));
			} 
		}
		
		for (Vec2d obstacle : obstacles) {
			double dist = pos.subtract(obstacle).distanceSquared();
			if (dist <= OBSTACLE_RADIUS * OBSTACLE_RADIUS) {
				acceleration = acceleration.add(pos.subtract(obstacle).scale(0.0001 / dist));
			}
		}
		
		double scalar = 1.0 / (1 + nearby.size());
			
		centerOfMass = centerOfMass.scale(scalar);
		acceleration = acceleration.add(centerOfMass.subtract(pos).scale(0.005));
			
		averageHeading = averageHeading.scale(scalar);
		acceleration = acceleration.add(averageHeading.scale(0.1));
		
		if (panel.isFollowingOn()) {
			Vec2d mouse = BoidsPanel.getWorldCoordinates(MouseInfo.getPointerInfo().getLocation());
			acceleration = acceleration.add(mouse.subtract(pos).scale(0.02));
		}
		
		acc = acceleration;
		vel = vel.add(acceleration);
		double distSq = vel.distanceSquared();
		if (distSq > MAXIMUM_VELOCITY_MAGNITUDE * MAXIMUM_VELOCITY_MAGNITUDE) {
			vel = vel.scale(MAXIMUM_VELOCITY_MAGNITUDE / Math.sqrt(distSq));
		}
		pos = pos.add(vel);
		
		if (pos.getX() < BoidsPanel.X_MIN) {
			pos = new Vec2d(BoidsPanel.X_MAX, pos.getY());
		}
		else if (pos.getX() > BoidsPanel.X_MAX){
			pos = new Vec2d(BoidsPanel.X_MIN, pos.getY());
		}
		
		if (pos.getY() < BoidsPanel.Y_MIN) {
			pos = new Vec2d(pos.getX(), BoidsPanel.Y_MAX);
		}
		else if (pos.getY() > BoidsPanel.Y_MAX) {
			pos = new Vec2d(pos.getX(), BoidsPanel.Y_MIN);
		}
		
		/*
		double leftEdgeDistance = pos.getX() - BoidsPanel.X_MIN;
		double rightEdgeDistance = BoidsPanel.X_MAX - pos.getX();
		double topEdgeDistance = BoidsPanel.Y_MAX - pos.getY();
		double bottomEdgeDistance = pos.getY() - BoidsPanel.Y_MIN;
		if (pos.getX() < BoidsPanel.X_MIN) {
			if (topEdgeDistance < bottomEdgeDistance) {
				pos = new Vec2d(BoidsPanel.X_MAX, BoidsPanel.Y_MIN + topEdgeDistance);
			}
			else {
				pos = new Vec2d(BoidsPanel.X_MAX, BoidsPanel.Y_MAX - bottomEdgeDistance);
			}
		}
		else if (pos.getX() > BoidsPanel.X_MAX) {
			if (topEdgeDistance < bottomEdgeDistance) {
				pos = new Vec2d(BoidsPanel.X_MIN, BoidsPanel.Y_MIN + topEdgeDistance);
			}
			else {
				pos = new Vec2d(BoidsPanel.X_MIN, BoidsPanel.Y_MAX - bottomEdgeDistance);
			}
		}
		
		if (pos.getY() < BoidsPanel.Y_MIN) {
			if (rightEdgeDistance < leftEdgeDistance) {
				pos = new Vec2d(BoidsPanel.X_MIN + rightEdgeDistance, BoidsPanel.Y_MAX);
			}
			else {
				pos = new Vec2d(BoidsPanel.X_MAX - leftEdgeDistance, BoidsPanel.Y_MAX);
			}
		}
		else if (pos.getY() > BoidsPanel.Y_MAX){
			if (rightEdgeDistance < leftEdgeDistance) {
				pos = new Vec2d(BoidsPanel.X_MIN + rightEdgeDistance, BoidsPanel.Y_MIN);
			}
			else {
				pos = new Vec2d(BoidsPanel.X_MAX - leftEdgeDistance, BoidsPanel.Y_MIN);
			}
		}
		*/
		
		this.density = Math.min(1, nearby.size() / 30.0);
		
		double r = Math.min(255, DENSE.getRed() * density + OPEN.getRed() * (1 - density)); 
		double g = Math.min(255, DENSE.getGreen() * density + OPEN.getGreen() * (1 - density)); 
		double b = Math.min(255, DENSE.getBlue() * density + OPEN.getBlue() * (1 - density)); 
		this.color = new Color((int) r, (int) g, (int) b);
	}
	
	public UUID getId() {
		return id;
	}
	
	public Vec2d getPosition() {
		return pos;
	}
	
	public Vec2d getVelocity() {
		return vel;
	}
	
	public Vec2d getAcceleration() {
		return acc;
	}
	
	public Color getColor() {
		return color;
	}
	
	public double getDensity() {
		return density;
	}
	
	@Override
	public int hashCode() {
		return id.hashCode();
	}
}
