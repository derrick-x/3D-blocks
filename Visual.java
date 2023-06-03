import java.awt.Color;
import java.awt.Graphics;

public abstract class Visual implements Comparable<Visual>{
	boolean needToDraw = true;
	public abstract Graphics drawSelf(Graphics g);
	public abstract double getLayer();
	public abstract boolean inView();
	public int compareTo(Visual o) {
		return (int)(o.getLayer()-getLayer());
	}
	public Color brightness(Color color, double factor) {
		return new Color(Math.min(255, (int)(color.getRed()*factor)), Math.min(255, (int)(color.getGreen()*factor)), Math.min(255, (int)(color.getBlue()*factor)));		
	}
}