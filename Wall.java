import java.awt.*;

public class Wall extends Visual{
	Color color = Color.white;
	short x1, y1, z1, x2, y2, z2, x, y, z, direction;
	//0 = north, 1 = west, 2 = east, 3 = south, 4 = lower, 5 = upper
	public Wall(int x1, int z1, int x2, int z2, int y1, int y2, int x, int y, int z, int d) {
		this.x1 = (short)x1;
		this.y1 = (short)y1;
		this.z1 = (short)z1;
		this.x2 = (short)x2;
		this.y2 = (short)y2;
		this.z2 = (short)z2;
		this.x = (short)x;
		this.y = (short)y;
		this.z = (short)z;
		direction = (short)d;
	}
	public Wall(int x1, int z1, int x2, int z2, int y, int x, int ypos, int z, int d) {
		this.x1 = (short)x1;
		this.y1 = (short)y;
		this.z1 = (short)z1;
		this.x2 = (short)x2;
		this.y2 = (short)y;
		this.z2 = (short)z2;
		this.x = (short)x;
		this.y = (short)ypos;
		this.z = (short)z;
		direction = (short)d;
	}
	public Wall(int x1, int z1, int x2, int z2, int y1, int y2, Color c, int x, int y, int z, int d) {
		this.x1 = (short)x1;
		this.y1 = (short)y1;
		this.z1 = (short)z1;
		this.x2 = (short)x2;
		this.y2 = (short)y2;
		this.z2 = (short)z2;
		color = c;
		this.x = (short)x;
		this.y = (short)y;
		this.z = (short)z;
		direction = (short)d;
	}
	public Wall(int x1, int z1, int x2, int z2, int y, Color c, int x, int ypos, int z, int d) {
		this.x1 = (short)x1;
		this.y1 = (short)y;
		this.z1 = (short)z1;
		this.x2 = (short)x2;
		this.y2 = (short)y;
		this.z2 = (short)z2;
		color = c;
		this.x = (short)x;
		this.y = (short)ypos;
		this.z = (short)z;
		direction = (short)d;
	}
	@Override
	public boolean inView() {
		return Main.inView(x1-Main.x, y1-Main.y+50, z1-Main.z, Main.xAngle, Main.yAngle)||Main.inView(x2-Main.x, y1-Main.y+50, z2-Main.z, Main.xAngle, Main.yAngle)||Main.inView(x2-Main.x, y2-Main.y+50, z2-Main.z, Main.xAngle, Main.yAngle)||Main.inView(x1-Main.x, y2-Main.y+50, z1-Main.z, Main.xAngle, Main.yAngle);
	}
	@Override
	public Graphics drawSelf(Graphics g) {
		if (!needToDraw) {
			return g;
		}
		Polygon p = new Polygon();
		if (!inView()) {
			return g;
		}
		if (x1==x2||z1==z2) {
			p.addPoint(Main.get3D(x1-Main.x, y1-Main.y, z1-Main.z, Main.xAngle, Main.yAngle).x, Main.get3D(x1-Main.x, y1-Main.y, z1-Main.z, Main.xAngle, Main.yAngle).y);
			p.addPoint(Main.get3D(x2-Main.x, y1-Main.y, z2-Main.z, Main.xAngle, Main.yAngle).x, Main.get3D(x2-Main.x, y1-Main.y, z2-Main.z, Main.xAngle, Main.yAngle).y);
			p.addPoint(Main.get3D(x2-Main.x, y2-Main.y, z2-Main.z, Main.xAngle, Main.yAngle).x, Main.get3D(x2-Main.x, y2-Main.y, z2-Main.z, Main.xAngle, Main.yAngle).y);
			p.addPoint(Main.get3D(x1-Main.x, y2-Main.y, z1-Main.z, Main.xAngle, Main.yAngle).x, Main.get3D(x1-Main.x, y2-Main.y, z1-Main.z, Main.xAngle, Main.yAngle).y);
		} else {
			p.addPoint(Main.get3D(x1-Main.x, y1-Main.y, z1-Main.z, Main.xAngle, Main.yAngle).x, Main.get3D(x1-Main.x, y1-Main.y, z1-Main.z, Main.xAngle, Main.yAngle).y);
			p.addPoint(Main.get3D(x1-Main.x, y1-Main.y, z2-Main.z, Main.xAngle, Main.yAngle).x, Main.get3D(x1-Main.x, y1-Main.y, z2-Main.z, Main.xAngle, Main.yAngle).y);
			p.addPoint(Main.get3D(x2-Main.x, y2-Main.y, z2-Main.z, Main.xAngle, Main.yAngle).x, Main.get3D(x2-Main.x, y2-Main.y, z2-Main.z, Main.xAngle, Main.yAngle).y);
			p.addPoint(Main.get3D(x2-Main.x, y2-Main.y, z1-Main.z, Main.xAngle, Main.yAngle).x, Main.get3D(x2-Main.x, y2-Main.y, z1-Main.z, Main.xAngle, Main.yAngle).y);
		}
		p.translate(500, 300);
		for (int i=0; i<4; i++) {
			if (p.xpoints[i]>200&&p.xpoints[i]<800&&p.ypoints[i]>0&&p.ypoints[i]<600) {
				break;
			}
			if (i==3) {
				return g;
			}
		}
		g.setColor(Block.getColor(this));
		try {
			if (direction==0) {
				g.setColor(brightness(g.getColor(), (Light.getLight(x, y, z-1)+5)/15.0));
			} else if (direction==1) {
				g.setColor(brightness(g.getColor(), (Light.getLight(x-1, y, z)+5)/15.0));
			} else if (direction==2) {
				g.setColor(brightness(g.getColor(), (Light.getLight(x+1, y, z)+5)/15.0));
			} else if (direction==3) {
				g.setColor(brightness(g.getColor(), (Light.getLight(x, y, z+1)+5)/15.0));
			} else if (direction==4) {
				g.setColor(brightness(g.getColor(), (Light.getLight(x, y+1, z)+5)/15.0));
			} else if (direction==5) {
				g.setColor(brightness(g.getColor(), (Light.getLight(x, y-1, z)+5)/15.0));
			}
		} catch (ArrayIndexOutOfBoundsException e) {}
		g.fillPolygon(p);
		g.setColor(brightness(g.getColor(), 0.8));
		g.drawPolygon(p);
		return g;
	}
	@Override
	public double getLayer() {
		return ((int)Math.hypot((x1+x2)/2-Main.x, Math.hypot((y1+y2)/2-Main.y+150, (z1+z2)/2-Main.z))*10)/10.0;
	}
}
