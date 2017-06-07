package io.github.dmhacker.boids.graphics;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import io.github.dmhacker.boids.simul.Boid;
import io.github.dmhacker.boids.simul.BoidSnapshot;
import io.github.dmhacker.boids.util.Vec2d;

public class BoidsPanel extends JPanel {
	private static final long serialVersionUID = 1L;
	
	private static final Dimension SCREEN = Toolkit.getDefaultToolkit().getScreenSize();
	private static final Vec2d TOP_LEFT = getWorldCoordinates(0, 0);
	private static final Vec2d BOTTOM_RIGHT = getWorldCoordinates(SCREEN.getWidth(), SCREEN.getHeight());
	
	public static final double X_MIN = TOP_LEFT.getX();
	public static final double X_MAX = BOTTOM_RIGHT.getX();
	public static final double Y_MIN = BOTTOM_RIGHT.getY();
	public static final double Y_MAX = TOP_LEFT.getY();
	
	private ScheduledExecutorService service;
	
	private List<Boid> boids;
	private List<Vec2d> obstacles;
	
	private Future<?> mouseDown;
	
	private AtomicBoolean running;
	private AtomicBoolean followMouse;
	private AtomicBoolean displayHitboxes;
	
	private AtomicInteger tps;
	private AtomicInteger ticks;
	private AtomicLong tpsTimestamp;
	
	private AtomicInteger fps;
	private AtomicInteger frames;
	private AtomicLong fpsTimestamp;

	public BoidsPanel(int width, int height) {
		this.service = Executors.newScheduledThreadPool(2);
		this.boids = new CopyOnWriteArrayList<>();
		this.obstacles = new CopyOnWriteArrayList<>();
		this.running = new AtomicBoolean();
		this.followMouse = new AtomicBoolean();
		this.displayHitboxes = new AtomicBoolean();
		this.tps = new AtomicInteger(0);
		this.ticks = new AtomicInteger(0);
		this.fps = new AtomicInteger(0);
		this.frames = new AtomicInteger(0);
		this.tpsTimestamp = new AtomicLong(System.currentTimeMillis());
		this.fpsTimestamp = new AtomicLong(System.currentTimeMillis());
		
		setFocusable(true);
		requestFocus();
		
		addKeyListener(new KeyAdapter() {
			
			@Override
			public void keyPressed(KeyEvent e) {
				if (e.getKeyChar() == 'b') { // Add boid
					Boid boid = new Boid(getWorldCoordinates(MouseInfo.getPointerInfo().getLocation()));
					boids.add(boid);
				}
				else if (e.getKeyChar() == 'r') { // Reset boids
					boids.clear();
				}
				else if (e.getKeyChar() == 'o') { // Add obstacle
					Point pt = MouseInfo.getPointerInfo().getLocation();
					obstacles.add(getWorldCoordinates(pt.getX() - 10, pt.getY() - 50));
				}
				else if (e.getKeyChar() == 'c') { // Clear obstacles
					obstacles.clear();
				}
				else if (e.getKeyChar() == 'h') { // Show boid hitboxes
					displayHitboxes.set(!displayHitboxes.get());
				}
				else if (e.getKeyChar() == 'f') { // Follow = on/off
					followMouse.set(!followMouse.get());
				}
			}
			
		});
		
		addMouseListener(new MouseAdapter() {
			
			@Override
			public void mousePressed(MouseEvent e) {
				if (mouseDown != null) {
					mouseDown.cancel(true);
				}
				mouseDown = service.scheduleAtFixedRate(new Runnable() {
					
					@Override
					public void run() {
						if (SwingUtilities.isLeftMouseButton(e)) {
							Boid boid = new Boid(getWorldCoordinates(MouseInfo.getPointerInfo().getLocation()));
							boids.add(boid);
						}
						else if (SwingUtilities.isRightMouseButton(e)) {
							Point pt = MouseInfo.getPointerInfo().getLocation();
							obstacles.add(getWorldCoordinates(pt.getX() - 10, pt.getY() - 50));
						}
					}
					
				}, 0, 15, TimeUnit.MILLISECONDS);
			}
			
			@Override
			public void mouseReleased(MouseEvent e) {
				if (mouseDown != null) {
					mouseDown.cancel(true);
					mouseDown = null;
				}
			}
		});
		
	}
	
	@Override
	protected void paintComponent(Graphics g) {
		g.clearRect(0, 0, getWidth(), getHeight());
		g.setColor(Color.BLACK);
		g.fillRect(0, 0, getWidth(), getHeight());
		g.setColor(Color.WHITE);
		for (Vec2d obstacle : obstacles) {
			Vec2d so = getScreenCoordinates(obstacle);
			g.fillRect((int) so.getX(), (int) so.getY(), 20, 20);
		}
		for (Boid boid : boids) {
			Vec2d pos = boid.getPosition();
			Vec2d screenPos = getScreenCoordinates(pos);
			int cx = (int) screenPos.getX();
			int cy = (int) screenPos.getY();
			g.setColor(boid.getColor());
			int[] xs = {cx + 20, cx - 10, cx - 10};
			int[] ys = {cy, cy - 10, cy + 10};
			AffineTransform transform = new AffineTransform();
			transform.rotate(3 * Math.PI / 2 + boid.getVelocity().theta(), cx, cy);
			((Graphics2D) g).setTransform(transform);
			g.fillPolygon(xs, ys, 3);
			((Graphics2D) g).setTransform(new AffineTransform());
			
			if (displayHitboxes.get()) {
				g.setColor(Color.WHITE);
				g.drawOval(cx - 20, cy - 20, 40, 40);
				
				Vec2d dpos = pos.add(boid.getVelocity().scale(10));
				Vec2d dposScreen = getScreenCoordinates(dpos);
				int dx = (int) dposScreen.getX();
				int dy = (int) dposScreen.getY();
				g.setColor(Color.RED);
				g.drawLine(cx, cy, dx, dy);
				
				/*
				Vec2d apos = pos.add(boid.getAcceleration().scale(100));
				Vec2d aposScreen = getScreenCoordinates(apos);
				int ax = (int) aposScreen.getX();
				int ay = (int) aposScreen.getY();
				((Graphics2D) g).setStroke(new BasicStroke(10));
				g.setColor(Color.CYAN);
				g.drawLine(cx, cy, ax, ay);
				((Graphics2D) g).setStroke(new BasicStroke(1));
				*/
			}
		}
		g.setColor(Color.YELLOW);
		g.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 28));
		g.drawString(""+fps.get(), 20, 40);
		g.setColor(Color.CYAN);
		g.drawString(""+tps.get(), 20, 80);
		g.setColor(Color.GREEN);
		g.drawString(""+boids.size(), getWidth() - 100, 40);
		g.setColor(Color.WHITE);
		g.drawString(""+obstacles.size(), getWidth() - 100, 80);
		frames.incrementAndGet();
	}
	
	private void tick() {
		
		Map<Boid, List<BoidSnapshot>> snapshots = new HashMap<>(); 
		Deque<Boid> boidsQueue = new ArrayDeque<>();
		boidsQueue.addAll(boids);
		while (!boidsQueue.isEmpty()) {
			Boid boid = boidsQueue.pop();
			List<BoidSnapshot> nearby;
			if (snapshots.containsKey(boid)) {
				nearby = snapshots.get(boid);
			}
			else {
				nearby = new ArrayList<>();
			}
			for (Boid other : boidsQueue) {
				double distance = boid.getPosition().subtract(other.getPosition()).distance();
				if (distance <= Boid.NEIGHBOR_RADIUS) {
					nearby.add(new BoidSnapshot(other, distance));
					List<BoidSnapshot> otherNearby;
					if (snapshots.containsKey(other)) {
						otherNearby = snapshots.get(other);
					}
					else {
						otherNearby = new ArrayList<>();
					}
					otherNearby.add(new BoidSnapshot(boid, distance));
					snapshots.put(other, otherNearby);
				}
			}
			snapshots.put(boid, nearby);
		}
		
		// We can't use the actual boids variable because it might have changed after the snapshot was built!
		for (Boid boid : snapshots.keySet()) {
			boid.update(this, snapshots.get(boid), obstacles);
		}
		
		ticks.incrementAndGet();
	}
	
	public void start() {
		
		running.set(true);
		
		// Render thread
		new Thread(){
			
			public void run() {
				while (running.get()) {
					repaint();
				}
			}
			
		}.start();
		
		// Ticking task
		service.scheduleAtFixedRate(new Runnable() {

			@Override
			public void run() {
				try {
					tick();
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}
			
		}, 0, 20, TimeUnit.MILLISECONDS);
		
		// FPS calculator
		service.scheduleAtFixedRate(new Runnable() {

			@Override
			public void run() {
				double s = (System.currentTimeMillis() - fpsTimestamp.get()) / 1000.0;
				fps.set((int) (frames.get() / s));
				frames.set(0);
				fpsTimestamp.set(System.currentTimeMillis());
			}
			
		}, 0, 200, TimeUnit.MILLISECONDS);
		
		// TPS calculator
		service.scheduleAtFixedRate(new Runnable() {

			@Override
			public void run() {
				double s = (System.currentTimeMillis() - tpsTimestamp.get()) / 1000.0;
				tps.set((int) (ticks.get() / s));
				ticks.set(0);
				tpsTimestamp.set(System.currentTimeMillis());
			}
			
		}, 0, 500, TimeUnit.MILLISECONDS);
	}
	
	public boolean isFollowingOn() {
		return followMouse.get();
	}
	
	public void close() {
		running.set(false);
		service.shutdownNow();
	}
	
	public static Vec2d getWorldCoordinates(Point point) {
		return getWorldCoordinates(point.getX(), point.getY());
	}
	
	public static Vec2d getWorldCoordinates(double screenX, double screenY) {
		double len = Math.min(SCREEN.getWidth(), SCREEN.getHeight()) / 2;
		return new Vec2d((screenX - SCREEN.getWidth() / 2) / len, -(screenY - SCREEN.getHeight() / 2) / len);
	}
	
	public static Vec2d getScreenCoordinates(Vec2d location) {
		return getScreenCoordinates(location.getX(), location.getY());
	}
	
	public static Vec2d getScreenCoordinates(double worldX, double worldY) {
		double len = Math.min(SCREEN.getWidth(), SCREEN.getHeight()) / 2;
		return new Vec2d(worldX * len + SCREEN.getWidth() / 2, -worldY * len + SCREEN.getHeight() / 2);
	}
}
