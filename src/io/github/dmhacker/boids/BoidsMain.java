package io.github.dmhacker.boids;

import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import io.github.dmhacker.boids.graphics.BoidsPanel;

/**
 * TODO:
 *  - Add GUI on bottom showing key commands
 *  
 * @author David Hacker
 *
 */
public class BoidsMain {

	public static void main(String[] args) {
		SwingUtilities.invokeLater(new Runnable() {

			@Override
			public void run() {
				JFrame frame = new JFrame();

				Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
				
				int width = screen.width + 4; // So the frame goes to the edge of the screen
				int height = screen.height - 60; // Minus the bottom programs bar
				// width = Math.min(width, height);
				// height = Math.min(width, height);
				
				frame.setSize(width, height);
				frame.setLocation(screen.width / 2 - width / 2, 0);
				frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
				frame.setResizable(false);
				frame.setTitle("Boids");
			
				BoidsPanel panel = new BoidsPanel(width, height);
				frame.add(panel);
				
				frame.addWindowListener(new WindowAdapter() {
		            @Override
		            public void windowClosing(WindowEvent we) {
		                panel.close();
		                System.exit(0);
		            }
				});

				panel.setVisible(true);
				panel.start();
				frame.setVisible(true);
			}
		});
	}
}
