package cjb;

import java.awt.Color;
import java.awt.Graphics2D;

public class Edge extends Entity{

	private static final long serialVersionUID = -5188794458550844002L;
	Friend source;
	Friend target;
	double k = 2.0;
	double length = 200;
	
	public Edge(Friend source, Friend target, Main m){
		this.source = source;
		this.target = target;
		this.m = m;
		radius = 5;
	}
	
	public void update(){
		x = source.x - target.x; y = source.y - target.y;
		double distance = Math.sqrt(x*x+y*y);
		source.dx += k * Math.cos(Math.atan2(y,x)) * (length - distance) / length;
		source.dy += k * Math.sin(Math.atan2(y,x)) * (length - distance) / length;
		target.dx += k * Math.cos(Math.atan2(-y,-x)) * (length - distance) / length;
		target.dy += k * Math.sin(Math.atan2(-y,-x)) * (length - distance) / length;
		x = source.x - x/2; y = source.y - y/2;
	}
	public void draw(Graphics2D g){
		if (selected){
			g.setColor(Color.green);
			int r = 5;
			g.drawOval((int)(x-(radius+r)), (int)(y-(radius+r)), (int)(2*(radius+r)), (int)(2*(radius+r)));
		}
		g.setColor(Color.black);
		g.drawLine((int)source.x, (int)source.y, (int)target.x, (int)target.y);
		g.drawOval((int)(x-radius), (int)(y-radius), (int)radius*2, (int)radius*2);
		if (name != null) g.drawString(name, (int)(x-radius), (int)(y-radius));
	}

}
