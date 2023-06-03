import java.awt.*;
import java.util.*;

public class Entity extends Visual{
	static int hostiles = 0;
	static int passives = 0;
	static class PathNode{
		int x, y, z;
		PathNode prev;
		public PathNode(int x, int y, int z, PathNode p) {
			this.x = x;
			this.y = y;
			this.z = z;
			prev = p;
		}
		public String toString() {
			return x+", "+y+", "+z;
		}
	}
	double x, y, z;
	double xVel, yVel, zVel;
	boolean canJump;
	boolean hostile;
	int radius, health, recharge, speed;
	Color color;
	ArrayList<int[]> path;
	int[] target = new int[3];
	public Entity(double x, double y, double z, boolean o, int r, int h, int s, Color c) throws Exception {
		if (hostile&&Math.hypot(x-Main.x, Math.hypot(y-Main.y, z-Main.z))<Main.render) {
			throw new Exception();
		}
		if (Math.hypot(x-Main.x, Math.hypot(y-Main.y, z-Main.z))>Main.render*2) {
			throw new Exception();
		}
		if (o) {
			hostiles++;
		} else {
			passives++;
		}
		this.x = x;
		this.y = y-10;
		this.z = z;
		radius = r;
		hostile = o;
		health = h;
		recharge = 20;
		speed = s;
		color = c;
		path = new ArrayList<int[]>();
		canJump = true;
		pathTo(null);
	}
	public String toString() {
		return (int)x+" "+(int)y+" "+(int)z+" "+hostile+" "+radius+" "+health+" "+speed+" "+color.getRed()+" "+color.getGreen()+" "+color.getBlue();
	}
	public PathNode pathFind(int x, int y, int z) {
		boolean[][][] visited = new boolean[Main.mapX][Main.mapY][Main.mapZ];
		Queue<PathNode> visitQueue = new LinkedList<PathNode>();
		visitQueue.add(new PathNode((int)(this.x/100), (int)(this.y/100), (int)(this.z/100), null));
		while (visitQueue.size()>0) {
			int[] current = {visitQueue.peek().x, visitQueue.peek().y, visitQueue.peek().z};
			if (visited[current[0]][current[1]][current[2]]) {
				visitQueue.poll();
				continue;
			}
			if (Math.hypot(current[0]*100-this.x, Math.hypot(current[1]*100-this.y, current[2]*100-this.z))>1500) {
				visitQueue.poll();
				continue;
			}
			visited[current[0]][current[1]][current[2]] = true;
			if (current[0]==x&&current[1]==y&&current[2]==z) {
				return visitQueue.peek();
			}
			if (current[0]>0) {
				if (Main.map[current[0]-1][current[1]][current[2]]!=null) {
					try {
						if (current[1]<Main.mapY-2&&Main.map[current[0]-1][current[1]-1][current[2]]==null) {
							visitQueue.add(new PathNode(current[0]-1, current[1]-1, current[2], visitQueue.peek()));
						}
					} catch (ArrayIndexOutOfBoundsException e) {
						
					}
				} else {
					int landY = current[1];
					while (Main.map[current[0]-1][landY][current[2]]==null) {
						landY++;
						if (landY==Main.mapY) {
							break;
						}
						if (Main.map[current[0]-1][landY][current[2]]!=null) {
							visitQueue.add(new PathNode(current[0]-1, landY-1, current[2], visitQueue.peek()));
						}
					}
				}
			}
			if (current[0]<Main.mapX-1) {
				if (Main.map[current[0]+1][current[1]][current[2]]!=null) {
					try {
						if (current[1]<Main.mapY-2&&Main.map[current[0]+1][current[1]-1][current[2]]==null) {
							visitQueue.add(new PathNode(current[0]+1, current[1]-1, current[2], visitQueue.peek()));
						}
					} catch (ArrayIndexOutOfBoundsException e) {
						
					}
				} else {
					int landY = current[1];
					while (Main.map[current[0]+1][landY][current[2]]==null) {
						landY++;
						if (landY==Main.mapY) {
							break;
						}
						if (Main.map[current[0]+1][landY][current[2]]!=null) {
							visitQueue.add(new PathNode(current[0]+1, landY-1, current[2], visitQueue.peek()));
						}
					}
				}
			}
			if (current[2]>0) {
				if (Main.map[current[0]][current[1]][current[2]-1]!=null) {
					try {
						if (current[1]<Main.mapY-2&&Main.map[current[0]][current[1]-1][current[2]-1]==null) {
							visitQueue.add(new PathNode(current[0], current[1]-1, current[2]-1, visitQueue.peek()));
						}
					} catch (ArrayIndexOutOfBoundsException e) {
						
					}
				} else {
					int landY = current[1];
					while (Main.map[current[0]][landY][current[2]-1]==null) {
						landY++;
						if (landY==Main.mapY) {
							break;
						}
						if (Main.map[current[0]][landY][current[2]-1]!=null) {
							visitQueue.add(new PathNode(current[0], landY-1, current[2]-1, visitQueue.peek()));
						}
					}
				}
			}
			if (current[2]<Main.mapZ-1) {
				if (Main.map[current[0]][current[1]][current[2]+1]!=null) {
					try {
						if (current[1]<Main.mapY-2&&Main.map[current[0]][current[1]-1][current[2]+1]==null) {
							visitQueue.add(new PathNode(current[0], current[1]-1, current[2]+1, visitQueue.peek()));
						}
					} catch (ArrayIndexOutOfBoundsException e) {
					
					}
				} else {
					int landY = current[1];
					while (Main.map[current[0]][landY][current[2]+1]==null) {
						landY++;
						if (landY==Main.mapY) {
							break;
						}
						if (Main.map[current[0]][landY][current[2]+1]!=null) {
							visitQueue.add(new PathNode(current[0], landY-1, current[2]+1, visitQueue.peek()));
						}
					}
				}
			}
			visitQueue.poll();
		}
		return null;
	}
	public void targetPlayer() {
		if (path.size()==0||(int)(Main.x/100)!=path.get(path.size()-1)[0]||(int)(Main.y/100)!=path.get(path.size()-1)[1]||(int)(Main.z/100)!=path.get(path.size()-1)[2]) {
			int[] target = {(int)(Main.x/100), (int)(Main.y/100), (int)(Main.z/100)};
			pathTo(target);
		}
	}
	public void tick() {
		if (health<=0) {
			Main.entities.remove(this);
			return;
		}
		if (recharge>0) {
			recharge--;
		}
		if (!Main.creative&&recharge<=0&&hostile&&Math.hypot(Main.x-x, Math.hypot(Main.y-y, Main.z-z))<75) {
			Main.health-=15;
			Main.yVel = -10;
			Main.regen = 300;
			Main.canJump = false;
			recharge = 20;
		}
		for (int i=0; i<1; i++) {
			if (canJump) { //Only pathfind while on the ground
				if (hostile) {
					targetPlayer();
				}
				if (path.size()<1) {
					if (Math.random()<0.01) {
						pathTo(null);
					} else {
						xVel = 0;
						zVel = 0;
						break;
					}
				}
				for (int j=0; j<path.size(); j++) {
					if (Math.hypot(x-path.get(j)[0]*100-50, z-path.get(j)[2]*100-50)<10) {
						for (int k=0; k<=j; k++) {
							path.remove(0);
						}
						break;
					}
				}
				if (path.size()<1) {
					if (Math.random()<0.01) {
						pathTo(null);
					} else {
						xVel = 0;
						zVel = 0;
						break;
					}
				}
				try {
					double xDist = path.get(0)[0]*100+50-x;
					double zDist = path.get(0)[2]*100+50-z;
					double factor = Math.hypot(xDist, zDist);
					if (factor!=0) {
						xVel = xDist*speed/factor;
						zVel = zDist*speed/factor;
					}
					if ((int)(y/100)>path.get(0)[1]) {
						yVel = -30;
					}
				} catch (Exception e) {}
			}
		}
		yVel+=3;
		if (yVel>120) {
			yVel = 120;
		}
		if (Main.time>2500&&Main.time<5000&&hostile) {
			y-=2;
			yVel = 0;
			canJump = false;
		}
		for (int i=0; i<Math.abs(yVel); i++) {
			y+=yVel>0?1:-1;
			if (Block.collide((int)x, (int)y, (int)z, radius)) {
				if (yVel>0) {
					y--;
					yVel=0;
					canJump = true;
				} else {
					y++;
					yVel*=-1;
				}
				break;
			} else {
				canJump = false;
			}
		}
		if (y>Main.mapY*100) {
			health-=10;
		}
		x+=xVel;
		z+=zVel;
		if (Block.collide((int)x, (int)y, (int)z, radius)) {
			x-=xVel;
			z-=zVel;
			pathTo(target);
		}
	}
	public void despawn() {
		if (hostile) {
			hostiles--;
		} else {
			passives--;
		}
		Main.entities.remove(this);
	}
	public void pathTo(int[] spot){
		try {
			int targetX = 0;
			int targetZ = 0;
			int targetY = 0;
			if (spot==null) {
				int xMin = Math.max(0, (int)(x/100)-10);
				int xMax = Math.min(Main.mapX-1, (int)(x/100)+10);
				int zMin = Math.max(0, (int)(z/100)-10);
				int zMax = Math.min(Main.mapZ-1, (int)(z/100)+10);
				targetX = (int)(Math.random()*(xMax-xMin))+xMin;
				targetZ = (int)(Math.random()*(zMax-zMin))+zMin;
			} else {
				targetX = spot[0];
				targetY = spot[1];
				targetZ = spot[2];
			}
			while (Main.map[targetX][targetY][targetZ]==null) {
				targetY++;
			}
			targetY--;
			PathNode pathnode = pathFind(targetX, targetY, targetZ);
			path = new ArrayList<int[]>();
			while (pathnode!=null) {
				int[] location = {pathnode.x, pathnode.y, pathnode.z};
				path.add(location);
				pathnode = pathnode.prev;
			}
			Collections.reverse(path);
			target[0] = targetX;
			target[1] = targetY;
			target[2] = targetZ;
			try {
				path.remove(0);
			} catch (IndexOutOfBoundsException e) {}
		} catch (ArrayIndexOutOfBoundsException e) {}
	}
	public boolean hitByPlayer() {
		if (!inView()) {
			return false;
		}
		Point p = Main.get3D(x-Main.x, y-Main.y, z-Main.z, Main.xAngle, Main.yAngle);
		double distance = Math.hypot(p.x, p.y);
		return distance<360*radius/getLayer();
	}
	@Override
	public boolean inView() {
		return Main.inView(x-Main.x, y-Main.y, z-Main.z, Main.xAngle, Main.yAngle);
	}
	@Override
	public Graphics drawSelf(Graphics g) {
		Point p = Main.get3D(x-Main.x, y-Main.y, z-Main.z, Main.xAngle, Main.yAngle);
		if (!inView()) {
			return g;
		}
		p.translate(500, 300);
		g.setColor(color);
		g.fillOval(p.x-(int)(180*radius/getLayer()), p.y-(int)(360*radius/getLayer()), (int)(720*radius/getLayer()), (int)(720*radius/getLayer()));
		g.setColor(brightness(color, 0.8));
		g.drawOval(p.x-(int)(180*radius/getLayer()), p.y-(int)(360*radius/getLayer()), (int)(720*radius/getLayer()), (int)(720*radius/getLayer()));
		return g;
	}
	@Override
	public double getLayer() {
		return Math.hypot(x-Main.x, Math.hypot(y-Main.y+150, z-Main.z))-50;
	}
}
