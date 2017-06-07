package io.github.dmhacker.boids.util;

public class Vec2d implements Cloneable {
	private final double x;
	private final double y;
	
	public Vec2d(double x, double y) {
		this.x = x;
		this.y = y;
	}
	
	public double getX() {
		return x;
	}
	
	public double getY() {
		return y;
	}
	
	public double distance() {
		return Math.sqrt(x * x + y * y);
	}
	
	public double distanceSquared() {
		return x * x + y * y;
	}
	
	public double theta() {
		return Math.atan2(x, y);
	}
	
	public Vec2d normalize() {
		double r = distance();
		return new Vec2d(x / r, y / r);
	}
	
	public Vec2d add(double nx, double ny) {
		return new Vec2d(x + nx, y + ny);
	}
	
	public Vec2d add(Vec2d vec) {
		return add(vec.x, vec.y);
	}
	
	public Vec2d subtract(double nx, double ny) {
		return add(-nx, -ny);
	}
	
	public Vec2d subtract(Vec2d vec) {
		return add(vec.negative());
	}
	
	public Vec2d scale(double scalar) {
		return new Vec2d(x * scalar, y * scalar);
	}
	
	public Vec2d negative() {
		return new Vec2d(-x, -y);
	}
	
	@Override
	public Vec2d clone() {
		return new Vec2d(x, y);
	}
	
	public String toString() {
		return "<" + x + "," + y + ">";
	}
}
