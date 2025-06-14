package ass03.view;

import ass03.model.Boid;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

import static ass03.utils.Constants.ENVIRONMENT_WIDTH;

public class BoidsPanel extends JPanel {

	private final BoidsView view;
    private List<Boid> boids;
    private int framerate;

    public BoidsPanel(BoidsView view) {
    	this.view = view;
        this.boids = new ArrayList<>();
    }

    public void setFrameRate(int framerate, List<Boid> boids) {
    	this.framerate = framerate;
        this.boids = new ArrayList<>(boids);
    }
    
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        setBackground(Color.WHITE);
        
        var w = view.getWidth();
        var h = view.getHeight();
        var xScale = w/ ENVIRONMENT_WIDTH;
        // var envHeight = model.getHeight();
        // var yScale = h/envHeight;


        g.setColor(Color.BLUE);
        for (Boid boid : this.boids) {
        	var x = boid.getPos().x();
        	var y = boid.getPos().y();
        	int px = (int)(w/2 + x*xScale);
        	int py = (int)(h/2 - y*xScale);
            g.fillOval(px,py, 5, 5);
        }
        
        g.setColor(Color.BLACK);
        g.drawString("Num. Boids: " + this.boids.size(), 10, 25);
        g.drawString("Framerate: " + framerate, 10, 40);
   }
}
