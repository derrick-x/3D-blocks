import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;
import javax.swing.JFrame;

public class Main extends Canvas{
	private static final long serialVersionUID = 1L;
	static final int mapX = 300;
	static final int mapY = 100;
	static final int mapZ = 300;
	static boolean[] keys = new boolean[65490];
	static boolean closing = false;
	static boolean canJump;
	static boolean rightClick;
	static boolean genNewWorld = false;
	static boolean debug;
	static boolean creative = true;
	static Block chest;
	static long refresh;
	static long start;
	static double x;
	static double y;
	static double z;
	static double yVel;
	static double xAngle;
	static double yAngle;
	static double initXangle;
	static double initYangle = Math.PI/2;
	static int speed;
	static int fog = 100;
	static int render = 1400;
	static int selected = 0;
	static int health;
	static int recharge;
	static int regen;
	static int death;
	static int time = 2500; //t=0 is early twilight, 1 day is 10000t (500sec)
	static int xMin, xMax, yMin, yMax, zMin, zMax;
	static int craftTier = 0;
	static int gameStage = 0;
	static int hostileCap = 0;
	static int day;
	static String worldName;
	static Block[][][] map;
	static Block facing;
	static Point mouse;
	static JFrame frame;
	static Canvas canvas;
	static BufferedImage sun;
	static BufferedImage image = new BufferedImage(1000, 600, BufferedImage.TYPE_3BYTE_BGR);
	static Color skyColor = Color.cyan;
	static ArrayList<Integer> craftable;
	static ArrayList<String> worldNames = new ArrayList<String>();
	static ArrayList<Entity> entities = new ArrayList<Entity>();
	static PriorityQueue<Visual> dispOrder = new PriorityQueue<Visual>();
	public static void main(String[] args) throws Exception{
		if (creative) {
			Main.Inventory.add("Iron pickaxe", null);
		}
		map = new Block[mapX][mapY][mapZ];
		new File("worlds").mkdirs();
		File[] worlds = new File("worlds").listFiles();
		for (int i=0; i<worlds.length; i++) {
			worldNames.add(worlds[i].getName().substring(0, worlds[i].getName().length()-4));
		}
		Block.initialize();
		Items.initialize();
		sun = ImageIO.read(new File("sun.png"));
		frame = new JFrame();
		canvas = new Main();
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		canvas.setSize(1000, 600);
		frame.add(canvas);
		canvas.setBackground(Color.black);
		frame.add(canvas);
		frame.pack();
		canvas.setFocusable(false);
		frame.addWindowListener(new WindowAdapter() {
			@Override
		    public void windowClosing(WindowEvent e) {
				if (gameStage==0) {
					System.exit(0);
				}
				closing = true;
				if (health>0) {
					//Files.saveWorld(worldName.substring(0, worldName.length()-4));
				} else {
					new File("worlds\\"+worldName).delete();
				}
		        System.exit(0);
		    }
		});
		canvas.addMouseListener(new MouseListener() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if (gameStage==0) {
					if (e.getX()>600) {
						if (e.getY()<30) {
							genNewWorld = true;
							canvas.paint(canvas.getGraphics());
							Scanner scan = new Scanner(System.in);
							System.out.println("Enter world name:");
							worldName = scan.nextLine()+".txt";
							scan.close();
							genNewWorld = false;
							day = 1;
							int[][] elevations = new int[mapX][mapZ];
							for (int i=0; i<mapX; i++) {
								elevations[i][0] = mapY/2;
							}
							for (int i=0; i<mapZ; i++) {
								elevations[0][i] = mapY/2;
							}
							genTerrain(0.01, 0, 20, new Color(0, 200, 50), new Color(0, 200, 50), elevations);
							x = mapX*50+50;
							z = mapZ*50+50;
							y = 90;
							while (map[(int)(x/100)][(int)(y/100)][(int)(z/100)]==null) {
								y+=100;
							}
							y-=100;
							health = 100;
							regen = 0;
							gameStage = 1;
							Light.initialize();
						} else {
							try {
								Files.loadWorld(e.getY()/30-1);
							} catch (Exception ex) {
								ex.printStackTrace();
								System.out.println("Error occured while loading world.");
							}
						}
					}
				}
			}
			@Override
			public void mousePressed(MouseEvent e) {
				if (e.getButton()==1) {
					if (chest!=null) {
						if (e.getPoint().x<800) {
							ArrayList<String> disp = new ArrayList<String>(chest.contains.keySet());
							int select = e.getY()/50-1;
							if (select<disp.size()&&select>=0) {
								Inventory.add(disp.get(select), Block.getBlockInfo(disp.get(select))==null?null:Block.getBlockInfo(disp.get(select)).color);
								if (chest.contains.get(disp.get(select))<2) {
									chest.contains.remove(disp.get(select));
								} else {
									chest.contains.put(disp.get(select), chest.contains.get(disp.get(select))-1);
								}
							}
						} else {
							if (Inventory.display.size()>0) {
								Inventory item = Inventory.display.get(selected);
								if (chest.contains.containsKey(item.name)) {
									chest.contains.put(item.name, chest.contains.get(item.name)+1);
								} else {
									chest.contains.put(item.name, 1);
								}
								Inventory.total--;
								item.amount--;
								if (item.amount==0) {
									Inventory.display.remove(selected);
								}
							}
						}
					}
					if (mouse==null) {
						initXangle = xAngle;
						initYangle = yAngle;
						mouse = e.getPoint();
					}
				} else {
					rightClick = true;
				}
			}
			@Override
			public void mouseReleased(MouseEvent e) {
				mouse = null;
			}
			@Override
			public void mouseEntered(MouseEvent e) {
			}
			@Override
			public void mouseExited(MouseEvent e) {
			}
		});
		frame.addKeyListener(new KeyListener() {
			@Override
			public void keyTyped(KeyEvent e) {
				if (e.getKeyChar()=='y') {
					Light.initialize();
				}
			}
			@Override
			public void keyPressed(KeyEvent e) {
				keys[e.getKeyCode()] = true;
			}
			@Override
			public void keyReleased(KeyEvent e) {
				keys[e.getKeyCode()] = false;
			}
		});
		worldName = "test.txt";
		genNewWorld = false;
		day = 1;
		int[][] elevations = new int[mapX][mapZ];
		for (int i=0; i<mapX; i++) {
			elevations[i][0] = mapY/2;
		}
		for (int i=0; i<mapZ; i++) {
			elevations[0][i] = mapY/2;
		}
		genTerrain(0.01, 0.01, 20, new Color(0, 200, 50), new Color(0, 200, 50), elevations);
		x = mapX*50+50;
		z = mapZ*50+50;
		y = 90;
		while (map[(int)(x/100)][(int)(y/100)][(int)(z/100)]==null) {
			y+=100;
		}
		y-=100;
		health = 100;
		regen = 0;
		gameStage = 1;
		Light.initialize();
		canvas.repaint();
		frame.setVisible(true);
		while (gameStage==0) {
			Thread.sleep(50);
			drawDisplay();
			canvas.paint(canvas.getGraphics());
		}
		xMin = yMin = zMin = 0;
		xMax = mapX-1;
		yMax = mapY-1;
		zMax = mapZ-1;
		Block.checkCovered(map);
		refresh = System.currentTimeMillis();
		recharge = 10;
		speed = 12;
		start = System.currentTimeMillis();
		while (true) {
			if (closing) {
				return;
			}
			if (System.currentTimeMillis()<refresh) {
				continue;
			}
			refresh = System.currentTimeMillis()+50;
			if (death>0) {
				canvas.paint(canvas.getGraphics());
				continue;
			}
			xMin = Math.max(0, (int)(x/100)-render/100);
			xMax = Math.min(mapX-1, (int)(x/100)+render/100);
			yMin = Math.max(0, (int)(y/100)-render/100);
			yMax = Math.min(mapY-1, (int)(y/100)+render/100);
			zMin = Math.max(0, (int)(z/100)-render/100);
			zMax = Math.min(mapZ-1, (int)(z/100)+render/100);
			if (refresh>System.currentTimeMillis()) {
				for (int x=xMin; x<xMax; x++) {
					for (int y=yMin; y<yMax; y++) {
						for (int z=zMin; z<zMax; z++) {
							for (int i=0; i<6; i++) {
								if (map[x][y][z]!=null&&map[x][y][z].walls[i].needToDraw&&map[x][y][z].walls[i].getLayer()<render) {
									dispOrder.add(map[x][y][z].walls[i]);
								}
							}
						}
					}
				}
				for (int i=0; i<entities.size(); i++) {
					if (entities.get(i).getLayer()<render) {
						dispOrder.add(entities.get(i));
					}
				}
				drawDisplay();
				canvas.paint(canvas.getGraphics());
			}
			if (Entity.passives<10) {
				int spawnX = ((int)(Math.random()*(xMax-xMin+20)+xMin-10))*100+50;
				int spawnZ = ((int)(Math.random()*(zMax-zMin+20)+zMin-10))*100+50;
				if (spawnX<0) {
					spawnX = 0;
				}
				if (spawnX>=mapX*100) {
					spawnX = mapX*100-50;
				}
				if (spawnZ<0) {
					spawnZ = 0;
				}
				if (spawnZ>=mapZ*100) {
					spawnX = mapZ*100-50;
				}
				int spawnY = 75;
				while (map[spawnX/100][spawnY/100][spawnZ/100]==null) {
					spawnY+=100;
					if (spawnY/100>=Main.mapY) {
						break;
					}
				}
				spawnY-=100;
				try {
					entities.add(new Entity(spawnX, spawnY, spawnZ, false, 25, 50, 5, Color.yellow));
				} catch (Exception e) {}
			}
			time+=1;
			if (time==2500) {
				day++;
			}
			if (time>=10000) {
				time = 0;
			}
			if (time>7500) {
				hostileCap = 5;
				skyColor = Color.black;
			} else if (time>5000) {
				hostileCap = time/500-10;
				double val = (time-5000)*(255/2500.0);
				skyColor = new Color(0, 255-(int)val, 255-(int)val);
			} else if (time>2500) {
				hostileCap = 0;
				skyColor = Color.cyan;
			} else {
				hostileCap = (2500-time)/500;
				skyColor = new Color(0, (int)(time*255/2500.0), (int)(time*255/2500.0));
			}
			hostileCap*=Math.max(day, 6);
			if (Entity.hostiles<hostileCap) {
				int spawnX = ((int)(Math.random()*mapX))*100+50;
				int spawnZ = ((int)(Math.random()*mapX))*100+50;
				int spawnY = 75;
				while (map[spawnX/100][spawnY/100][spawnZ/100]==null) {
					spawnY+=100;
				}
				spawnY-=100;
				try {
					entities.add(new Entity(spawnX, spawnY, spawnZ, true, 25, 50, 10, Color.red));
				} catch (Exception e) {}
			}
			Block checkNew = getFacing();
			if (checkNew!=facing) {
				if (facing!=null) {
					facing.changeColor(facing.trueColor);
				}
				facing = checkNew;
				if (facing!=null) {
					facing.select();
				}
			}
			if (recharge>0) {
				recharge--;
			}
			if (keys['W']) {
				x+=Math.sin(xAngle)*speed;
				if (Block.collide((int)x, (int)y, (int)z, 10, 180)) {
					x-=Math.sin(xAngle)*speed;
				}

				z+=Math.cos(xAngle)*speed;
				if (Block.collide((int)x, (int)y, (int)z, 10, 180)) {
					z-=Math.cos(xAngle)*speed;
				}
			}
			if (keys['S']) {
				x-=Math.sin(xAngle)*speed;
				if (Block.collide((int)x, (int)y, (int)z, 10, 180)) {
					x+=Math.sin(xAngle)*speed;
				}

				z-=Math.cos(xAngle)*speed;
				if (Block.collide((int)x, (int)y, (int)z, 10, 180)) {
					z+=Math.cos(xAngle)*speed;
				}
			}
			if (keys['D']) {
				x+=Math.sin(xAngle+Math.PI/2)*speed;
				if (Block.collide((int)x, (int)y, (int)z, 10, 180)) {
					x-=Math.sin(xAngle+Math.PI/2)*speed;
				}

				z+=Math.cos(xAngle+Math.PI/2)*speed;
				if (Block.collide((int)x, (int)y, (int)z, 10, 180)) {
					z-=Math.cos(xAngle+Math.PI/2)*speed;
				}
			}
			if (keys['A']) {
				x+=Math.sin(xAngle-Math.PI/2)*speed;
				if (Block.collide((int)x, (int)y, (int)z, 10, 180)) {
					x-=Math.sin(xAngle-Math.PI/2)*speed;
				}

				z+=Math.cos(xAngle-Math.PI/2)*speed;
				if (Block.collide((int)x, (int)y, (int)z, 10, 180)) {
					z-=Math.cos(xAngle-Math.PI/2)*speed;
				}
			}
			if (keys[' ']&&canJump) {
				yVel = -30;
			}
			if (rightClick) {
				if (chest!=null) {
					if (Inventory.display.size()>0) {
						Inventory item = Inventory.display.get(selected);
						if (chest.contains.containsKey(item.name)) {
							chest.contains.put(item.name, chest.contains.get(item.name)+item.amount);
						} else {
							chest.contains.put(item.name, item.amount);
						}
						Inventory.total-=item.amount;
						Inventory.display.remove(selected);
					}
				} else if (craftable==null) {
					craftTier = 0;
					craftable = Items.getCraftable(0);
				} else {
					craftable = null;
				}
				rightClick = false;
			}
			if (keys['X']) {
				boolean hit = false;
				for (int i=0; recharge<=0&&i<entities.size(); i++) {
					if (entities.get(i).hitByPlayer()) {
						if (Inventory.display.size()>0&&Inventory.display.get(selected).name.equals("Stone sword")) {
							entities.get(i).health-=20;
						} else if (Inventory.display.size()>0&&Inventory.display.get(selected).name.equals("Wooden sword")) {
							entities.get(i).health-=10;
						} else if (Inventory.display.size()>0&&Inventory.display.get(selected).name.equals("Iron sword")) {
							entities.get(i).health-=35;
						} else {
							entities.get(i).health-=5;
						}
						double distX = entities.get(i).x-Main.x;
						double distZ = entities.get(i).z-Main.z;
						entities.get(i).xVel = distX*10/Math.hypot(distX, distZ);
						entities.get(i).zVel = distZ*10/Math.hypot(distX, distZ);
						entities.get(i).yVel = -20;
						entities.get(i).canJump = false;
						keys['X'] = false;
						recharge = 10;
						hit = true;
						break;
					}
				}
				if (!hit&&facing!=null) {
					if (Inventory.display.size()>0) {
						if (Inventory.display.get(selected).name.equals("Wooden pickaxe")) {
							facing.mine(3);
						} else if (Inventory.display.get(selected).name.equals("Stone pickaxe")) {
							facing.mine(5);
						} else if (Inventory.display.get(selected).name.equals("Iron pickaxe")){
							facing.mine(8);
						} else {
							facing.mine(1);
						}
					} else {
						facing.mine(1);
					}
				}
			}
			if (keys['C']) {
				if (chest!=null) {
					chest = null;
				} else if (craftable!=null) {
					craftable = null;
				} else if (facing!=null&&facing.use()) {} else {
					if (Inventory.display.size()>0&&Inventory.display.get(selected).color!=null) {
						int[] location = getPlace();
						if (location!=null&&selected<Inventory.display.size()) {
							map[location[0]][location[1]][location[2]] = new Block(location[0]*100, location[1]*100, location[2]*100, Inventory.display.get(selected).color);
							Inventory.display.get(selected).amount--;
							Inventory.total--;
							if (Inventory.display.get(selected).amount==0) {
								Inventory.display.remove(selected);
								if (selected>0) {
									selected--;
								}
							}
							Block.checkCovered(map);
							Light.update(location[0], location[1], location[2]);
						}
					}
				}
				keys['C'] = false;
			}
			if (keys['Q']) {
				selected--;
				if (selected<0) {
					selected = Inventory.display.size()-1;
				}
				keys['Q'] = false;
			}
			if (keys['E']) {
				selected++;
				if (selected>=Inventory.display.size()) {
					selected = 0;
				}
				keys['E'] = false;
			}
			if (keys[KeyEvent.VK_UP]) {
				if (yAngle>-1.4) {
					yAngle-=0.1;
				}
			}
			if (keys[KeyEvent.VK_DOWN]) {
				if (yAngle<1.4) {
					yAngle+=0.1;
				}
			}
			if (keys[KeyEvent.VK_LEFT]) {
				xAngle-=0.1;
			}
			if (keys[KeyEvent.VK_RIGHT]) {
				xAngle+=0.1;
			}
			if (regen>0) {
				regen--;
			} else {
				if (health<100) {
					health++;
				}
			}
			if (creative) {
				health = 100;
			}
			if (health<=0) {
				death = 1;
			}
			yVel+=3;
			if (yVel>120) {
				yVel = 120;
			}
			for (int i=0; i<Math.abs(yVel); i++) {
				y+=yVel>0?1:-1;
				if (Block.collide((int)x, (int)y, (int)z, 10, 180)) {
					if (yVel>0) {
						y--;
						if (yVel>45) {
							health-=(yVel-45)*2;
							regen = 300;
						}
						yVel = 0;
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
			try {
				if (mouse!=null) {
					if (craftable==null) {
						xAngle = initXangle+(frame.getMousePosition().x-mouse.x)/100.0-0.07;
						yAngle = Math.max(Math.PI*-0.5, Math.min(initYangle+(frame.getMousePosition().y-mouse.y)/100.0-0.29, Math.PI*0.5));
					} else {
						if (mouse.x>200&&mouse.x<800) {
							int selected = mouse.y/50;
							if (selected<craftable.size()) {
								Items.craft(craftable.get(selected));
								craftable = Items.getCraftable(craftTier);
								mouse = null;
							}
						}
					}
				}
			} catch (NullPointerException e) {}
			for (int i=0; i<entities.size(); i++) {
				if (entities.get(i).getLayer()<render*2) {
					entities.get(i).tick();
				} else {
					entities.get(i).despawn();
					i--;
				}
			}
		}
	}
	public void paint(Graphics g) {
		if (death>0) {
			if (death==1) {
				g.setColor(Color.white);
				g.setFont(new Font(Font.MONOSPACED, Font.BOLD, 100));
				g.drawString("GAME OVER!", 220, 200);
			}
			death++;
			if (death>25) {
				death = 25;
			}
			g.setColor(new Color(255, 0, 0, death*10));
			g.fillRect(200, 0, 600, 600);
		} else {
			g.drawImage(image, 0, 0, null);
		}
	}
	public static void drawDisplay() {
		Graphics g = image.getGraphics();
		g.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 15));
		if (gameStage==0) {
			if (genNewWorld) {
				g.setColor(Color.cyan);
				g.fillRect(0, 0, 1000, 600);
				g.setColor(Color.black);
				g.setFont(new Font(Font.MONOSPACED, Font.BOLD, 50));
				g.drawString("Follow instructions on STDIN", 50, 300);
				return;
			}
			if (Files.progress==0) {
				g.setColor(Color.cyan);
				g.fillRect(0, 0, 600, 600);
				g.setColor(Color.gray);
				g.fillRect(600, 0, 600, 600);
				g.setColor(Color.white);
				g.drawString("Create new world", 610, 20);
				g.drawLine(600, 30, 1000, 30);
				for (int i=0; i<worldNames.size(); i++) {
					g.drawString(worldNames.get(i), 610, i*30+50);
					g.drawLine(600, i*30+60, 1000, i*30+60);
				}
				g.setColor(new Color(0, 200, 50));
				g.setFont(new Font(Font.MONOSPACED, Font.BOLD, 80));
				g.drawString("MINECRAFN\'T", 40, 270);
			} else {
				g.setColor(Color.cyan);
				g.fillRect(0, 0, 1000, 600);
				g.setColor(Color.white);
				g.fillRect(200, 275, Files.progress*2, 50);
				g.setColor(Color.black);
				g.drawRect(200, 275, 600, 50);
				g.drawString("Loading...", 50, 200);
			}
			return;
		}
		g.setColor(skyColor);
		g.fillRect(200, 0, 600, 600);
		double sunX = 10000*Math.cos(Math.PI*time/5000-Math.PI/4);
		double sunY = -10000*Math.sin(Math.PI*time/5000-Math.PI/4);
		Point disp = get3D(sunX, sunY, 0, xAngle, yAngle);
		disp.translate(500, 300);
		if (time>7500) {
			g.setColor(new Color(127, 255, 0));
		} else if (time>5000) {
			g.setColor(new Color(255-(int)((time-5000)*0.0255), 255, 255-(int)((time-5000)*0.051)));
		} else if (time>2500) {
			g.setColor(Color.white);
		} else {
			g.setColor(new Color(127+(int)((time-5000)*0.0255), 255, (int)(time*0.051)));
		}
		g.drawImage(sun, disp.x-300, disp.y-300, 600, 600, null);
		g.setColor(new Color(0, 200, 50));
		while (dispOrder.size()>0) {
			g = dispOrder.poll().drawSelf(g);
		}
		g.setColor(Color.black);
		g.fillRect(800, 0, 200, 600);
		g.setColor(Color.black);
		g.fillRect(0, 0, 200, 600);
		g.setColor(Color.white);
		g.drawLine(500, 295, 500, 305);
		g.drawLine(495, 300, 505, 300);
		g.setColor(Color.red);
		g.setColor(Color.white);
		g.drawString("Block facing: ", 810, 20);
		if (facing!=null) {
			g.drawString(facing.getName(), 810, 40);
			g.drawRect(850, 50, 100, 10);
			g.fillRect(850, 50, facing.getMineProgress(), 10);
		}
		if (Inventory.total==50) {
			g.drawString("Inventory full!", 810, 80);
		} else {
			g.drawString("Inventory:", 810, 80);			
		}
		if (craftable!=null) {
			g.setColor(new Color(254, 254, 254, 200));
			g.fillRect(200, 0, 600, 600);
			g.setColor(Color.black);
			for (int i=0; i<craftable.size(); i++) {
				g.drawString(Items.items.get(craftable.get(i))+(Items.amount[craftable.get(i)]>1?(" x"+Items.amount[craftable.get(i)]):""), 210, i*50+15);
				StringBuilder recipe = new StringBuilder("Required:");
				for (int j=1; j<Items.items.size(); j++) {
					if (Items.crafting[craftable.get(i)][j]>0) {
						recipe.append(" "+Items.items.get(j)+" x"+Items.crafting[craftable.get(i)][j]);
					}
				}
				g.drawString(recipe.toString(), 210, i*50+40);
				g.drawLine(200, i*50+50, 800, i*50+50);
			}
		}
		if (chest!=null) {
			g.setColor(new Color(254, 254, 254, 200));
			g.fillRect(200, 0, 600, 600);
			g.setColor(Color.black);
			g.drawString("Chest contents:", 210, 30);
			g.drawLine(200, 50, 800, 50);
			int dispY = 0;
			for (String i : chest.contains.keySet()) {
				dispY++;
				g.drawString(i+" x"+chest.contains.get(i), 210, dispY*50+30);
				g.drawLine(200, dispY*50+50, 800, dispY*50+50);
			}
		}
		g.setColor(Color.white);
		g.drawRect(850, 90, 100, 10);
		g.fillRect(850, 90, Inventory.total*2, 10);
		for (int i=0; i<Inventory.display.size(); i++) {
			if (i==selected) {
				g.setColor(Color.white);
			} else {
				g.setColor(Color.gray);
			}
			g.drawString(Inventory.display.get(i).name+": "+Inventory.display.get(i).amount, 810, i*20+120);
		}
		g.setColor(Color.red);
		g.fillRect(50, 550, health, 10);
		g.setColor(Color.white);
		g.drawString("Health:", 10, 540);
		g.drawRect(50, 550, 100, 10);
		g.drawString("fps: "+1000/(System.currentTimeMillis()-start), 10, 580);
		start = System.currentTimeMillis();
		//debug
		try {
			g.drawString((int)(Main.x/100)+" "+(int)(Main.y/100)+" "+(int)(Main.z/100), 10, 20);
		} catch (Exception e) {}
	}
	public static Point get3D(double x, double y, double z, double xAngle, double yAngle) {
		y+=150;
		double dispX = 0;
		double dispY = 0;
		double xView = 0;
		double yView = 0;
		double zView = 0;
		double newXangle = Math.atan2(x, z)-xAngle;
		xView = Math.sin(newXangle)*Math.hypot(x, z);
		zView = Math.cos(newXangle)*Math.hypot(x, z);
		double newYangle = Math.atan2(y, zView)-yAngle;
		yView = Math.sin(newYangle)*Math.hypot(y, zView);
		zView = Math.cos(newYangle)*Math.hypot(y, zView);
		if (zView<=0) {
			zView = 0.01;
		}
		dispX = xView*(50.0/zView)*6;
		dispY = yView*(50.0/zView)*6;
		if (dispY>9999) {
			dispY = 9999;
		}
		return new Point((int)dispX, (int)dispY);
	}
	public static boolean inView(double x, double y, double z, double xAngle, double yAngle) {
		y+=150;
		double zView = 0;
		double newXangle = Math.atan2(x, z)-xAngle;
		zView = Math.cos(newXangle)*Math.hypot(x, z);
		double newYangle = Math.atan2(y, zView)-yAngle;
		zView = Math.cos(newYangle)*Math.hypot(y, zView);
		return zView>0;
	}
	public static int[] getPlace() {
		double projX = x;
		double projY = y-150;
		double projZ = z;
		double xVel = Math.sin(xAngle)*Math.cos(yAngle);
		double zVel = Math.cos(xAngle)*Math.cos(yAngle);
		double yVel = Math.sin(yAngle);
		zVel*=Math.cos(yAngle);
		while (Math.sqrt(Math.pow(projX-x, 2)+Math.pow(projY-(y-150), 2)+Math.pow(projZ-z, 2))<300) {
			double xDist = (projX%100)*-1;
			double yDist = (projY%100)*-1;
			double zDist = (projZ%100)*-1;
			if (xDist==0) {
				if (xVel>0) {
					xDist = 100;
				} else {
					xDist = -100;
				}
			}
			if (yDist==0) {
				if (yVel>0) {
					yDist = 100;
				} else {
					yDist = -100;
				}
			}
			if (zDist==0) {
				if (zVel>0) {
					zDist = 100;
				} else {
					zDist = -100;
				}
			}
			if (xVel>0&&xDist<0) {
				xDist+=100;
			}
			if (yVel>0&&yDist<0) {
				yDist+=100;
			}
			if (zVel>0&&zDist<0) {
				zDist+=100;
			}
			try {
				if (xDist/xVel<yDist/yVel&&xDist/xVel<zDist/zVel) {
					projX+=xDist;
					projY+=yVel*(xDist/xVel);
					projZ+=zVel*(xDist/xVel);
					if (map[(int)(projX+xVel)/100][(int)(projY)/100][(int)(projZ)/100]!=null) {
						projX-=xDist;
						int[] location = {(int)(projX/100), (int)(projY/100), (int)(projZ/100)};
						return location;
					}
				} else if (yDist/yVel<xDist/xVel&&yDist/yVel<zDist/zVel) {
					projY+=yDist;
					projX+=xVel*(yDist/yVel);
					projZ+=zVel*(yDist/yVel);
					if (map[(int)(projX)/100][(int)(projY+yVel)/100][(int)(projZ)/100]!=null) {
						projY-=yDist;
						int[] location = {(int)(projX/100), (int)(projY/100), (int)(projZ/100)};
						return location;
					}
				} else {
					projZ+=zDist;
					projX+=xVel*(zDist/zVel);
					projY+=yVel*(zDist/zVel);
					if (map[(int)(projX)/100][(int)(projY)/100][(int)(projZ+zVel)/100]!=null) {
						projZ-=zDist;
						int[] location = {(int)(projX/100), (int)(projY/100), (int)(projZ/100)};
						return location;
					}
				}
			} catch (ArrayIndexOutOfBoundsException e) {
				return null;
			}
		}
		return null;
	}
	public static Block getFacing() {
		double projX = x;
		double projY = y-150;
		double projZ = z;
		double xVel = Math.sin(xAngle)*Math.cos(yAngle);
		double zVel = Math.cos(xAngle)*Math.cos(yAngle);
		double yVel = Math.sin(yAngle);
		zVel*=Math.cos(yAngle);
		while (Math.sqrt(Math.pow(projX-x, 2)+Math.pow(projY-(y-150), 2)+Math.pow(projZ-z, 2))<500) {
			double xDist = (projX%100)*-1;
			double yDist = (projY%100)*-1;
			double zDist = (projZ%100)*-1;
			if (xDist==0) {
				if (xVel>0) {
					xDist = 100;
				} else {
					xDist = -100;
				}
			}
			if (yDist==0) {
				if (yVel>0) {
					yDist = 100;
				} else {
					yDist = -100;
				}
			}
			if (zDist==0) {
				if (zVel>0) {
					zDist = 100;
				} else {
					zDist = -100;
				}
			}
			if (xVel>0&&xDist<0) {
				xDist+=100;
			}
			if (yVel>0&&yDist<0) {
				yDist+=100;
			}
			if (zVel>0&&zDist<0) {
				zDist+=100;
			}
			try {
				if (xDist/xVel<yDist/yVel&&xDist/xVel<zDist/zVel) {
					projX+=xDist;
					projY+=yVel*(xDist/xVel);
					projZ+=zVel*(xDist/xVel);
					if (map[(int)(projX+xVel)/100][(int)(projY)/100][(int)(projZ)/100]!=null) {
						return map[(int)(projX+xVel)/100][(int)(projY)/100][(int)(projZ)/100];
					}
				} else if (yDist/yVel<xDist/xVel&&yDist/yVel<zDist/zVel) {
					projY+=yDist;
					projX+=xVel*(yDist/yVel);
					projZ+=zVel*(yDist/yVel);
					if (map[(int)(projX)/100][(int)(projY+yVel)/100][(int)(projZ)/100]!=null) {
						return map[(int)(projX)/100][(int)(projY+yVel)/100][(int)(projZ)/100];
					}
				} else {
					projZ+=zDist;
					projX+=xVel*(zDist/zVel);
					projY+=yVel*(zDist/zVel);
					if (map[(int)(projX)/100][(int)(projY)/100][(int)(projZ+zVel)/100]!=null) {
						return map[(int)(projX)/100][(int)(projY)/100][(int)(projZ+zVel)/100];
					}
				}
			} catch (ArrayIndexOutOfBoundsException e) {
				return null;
			}
			if (projX<0||projY<0||projZ<0) {
				return null;
			}
		}
		return null;
	}
	public static void genTree(int baseX, int baseY, int baseZ) {
		if (map[baseX][baseY+1][baseZ]==null||!map[baseX][baseY+1][baseZ].color.equals(new Color(0, 200, 50))) {
			return;
		}
		if (baseY<5) {
			return;
		}
		for (int x=baseX-2; x<baseX+3; x++) {
			for (int y=baseY; y>baseY-5; y--) {
				for (int z=baseZ-2; z<baseZ+3; z++) {
					if (map[x][y][z]!=null) {
						return;
					}
				}
			}
		}
		map[baseX][baseY-4][baseZ] = new Block(baseX*100, baseY*100-400, baseZ*100, new Color(0, 127, 0));
		for (int x=baseX-1; x<baseX+2; x++) {
			for (int y=baseY-2; y>baseY-4; y--) {
				for (int z=baseZ-1; z<baseZ+2; z++) {
					if (x==baseX&&z==baseZ) {
						continue;
					}
					map[x][y][z] = new Block(x*100, y*100, z*100, new Color(0, 127, 0));
				}
			}
		}
		for (int y=baseY; y>baseY-4; y--) {
			map[baseX][y][baseZ] = new Block(baseX*100, y*100, baseZ*100, new Color(120, 60, 0));
		}
	}
	public static void genTerrain(double roughness, double trees, int amplitude, Color surface, Color material, int[][] elevations) {
		Files.progress = 1;
		long seed = (int)(Math.random()*Long.MAX_VALUE);
		for (int x=0; x<mapX; x++) {
			for (int z=0; z<mapZ; z++) {
				elevations[x][z] = (int)(Noise.noise2(seed, x*roughness, z*roughness)*amplitude)+40+amplitude;
			}
		}
		for (int x=0; x<mapX; x++) {
			Files.progress++;
			for (int z=0; z<mapZ; z++) {
				for (int y=mapY-1; y>elevations[x][z]-1; y--) {
					map[x][y][z] = new Block(x*100, y*100, z*100, material);
					if (Math.random()<0.03) {
						map[x][y][z] = new Block(x*100, y*100, z*100, Color.darkGray);
					}
					if (Math.random()<(y-50)*0.0005) {
						map[x][y][z] = new Block(x*100, y*100, z*100, new Color(225, 190, 170));
					}
				}
				map[x][elevations[x][z]-1][z] = new Block(x*100, (elevations[x][z]-1)*100, z*100, surface);
			}
		}
		double freq = 0.01;
		for (int x=0; x<mapX; x++) {
			for (int z=0; z<mapZ; z++) {
				if (Noise.noise2(seed, x*freq, z*freq)>-0.1&&Noise.noise2(seed, x*freq, z*freq)<0.1) {
					map[x][79][z] = null;
					map[x][80][z] = null;
					map[x][81][z] = null;
					map[x][82][z] = null;
					if (x>75&&z>1&&Math.random()<0.005) {
						for (int i=0; i<75; i++) {
							map[x-i][82-i][z] = null;
							map[x-i][81-i][z] = null;
							map[x-i][80-i][z] = null;
							map[x-i][79-i][z] = null;
							map[x-i][82-i][z-1] = null;
							map[x-i][81-i][z-1] = null;
							map[x-i][80-i][z-1] = null;
							map[x-i][79-i][z-1] = null;
							map[x-i][82-i][z-2] = null;
							map[x-i][81-i][z-2] = null;
							map[x-i][80-i][z-2] = null;
							map[x-i][79-i][z-2] = null;
						}
					}
				}
			}
		}
		for (int x=3; x<mapX-3; x++) {
			for (int z=3; z<mapZ-3; z++) {
				if (Math.random()<trees) {
					genTree(x, elevations[x][z]-2, z);
				}
			}
		}
	}
	static class Inventory{
		static ArrayList<Inventory> display = new ArrayList<Inventory>();
		String name;
		Color color;
		int amount;
		static int total;
		public Inventory(String n, Color c) {
			name = n;
			amount = 1;
			color = c;
		}
		public Inventory(String n, Color c, int a) {
			name = n;
			amount = a;
			color = c;
		}
		public static void add(String n, Color c) {
			if (total>50) {
				return;
			}
			if (display.size()==0) {
				display.add(new Inventory(n, c));
			} else {
				for (int i=0; i<display.size(); i++) {
					if (display.get(i).name.equals(n)) {
						display.get(i).amount++;
						break;
					}
					if (i==display.size()-1) {
						display.add(new Inventory(n, c));
						break;
					}
				}
			}
			total++;
		}
		public static void add(String n, Color c, int a) {
			if (total>50) {
				return;
			}
			if (display.size()==0) {
				display.add(new Inventory(n, c, a));
			} else {
				for (int i=0; i<display.size(); i++) {
					if (display.get(i).name.equals(n)) {
						display.get(i).amount+=a;
						break;
					}
					if (i==display.size()-1) {
						display.add(new Inventory(n, c, a));
						break;
					}
				}
			}
			total++;
		}
		public static boolean contains(String n) {
			for (int i=0; i<display.size(); i++) {
				if (display.get(i).name.equals(n)) {
					return true;
				}
			}
			return false;
		}
	}
}
