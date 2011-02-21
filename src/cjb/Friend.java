package cjb;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

public class Friend extends Entity{

	private static final long serialVersionUID = 8352888594796595976L;
	Set<Friend> friends;
	Set<Edge> edges;
	static double damp = 0.90;
	String number;
	
	public Friend(Main m, int x, int y){
		this.x = x; this.y = y;
		radius = 10;
		friends = new HashSet<Friend>();
		edges = new HashSet<Edge>();
		Random rand = new Random();
		int r = rand.nextInt(255);
		int g = rand.nextInt(255-r);
		int b = rand.nextInt(255-g/2);
		color = new Color(r, b, g);
		this.m = m;
		z = 1;
	}
	
	public void draw(Graphics2D g){
		if (selected){
			g.setColor(Color.green);
			int r = 5;
			g.drawOval((int)(x-(radius+r)), (int)(y-(radius+r)), (int)(2*(radius+r)), (int)(2*(radius+r)));
			g.setColor(color);
			if (description != null) g.drawString(description, (int)(x+radius), (int)(y+radius));
		}
		if (solid){
			g.setColor(Color.black);
			g.drawOval((int)(x-(radius+1)), (int)(y-(radius+1)), (int)(2*(radius+1)), (int)(2*(radius+1)));
		}
		g.setColor(color);
		g.fillOval((int)(x-radius), (int)(y-radius), (int)(2*radius), (int)(2*radius));
		if (name != null) g.drawString(name, (int)(x-radius), (int)(y-radius));
	}
	
	public void update(){
		if (solid) dx = dy = 0;
		else{
			x += dx; y += dy;
			dx *= damp; dy *= damp;
		}
	}

}
