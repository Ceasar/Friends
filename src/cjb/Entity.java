package cjb;

import java.awt.Color;
import java.io.Serializable;

public abstract class Entity implements Updatable, Drawable, Serializable, Comparable<Entity>{

	private static final long serialVersionUID = -6799215164515452595L;
	double x; double y;
	double dx = 0; double dy = 0;
	protected double radius;
	protected Color color;
	boolean selected = false;
	boolean solid = false;
	Main m;
	String name; String description;
	int z = 0;
	
	public int compareTo(Entity e){
		if (z > e.z)
			return 1;
		if (z < e.z)
			return -1;
		return 0;
	}
}
