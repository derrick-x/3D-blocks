import java.awt.Color;
import java.util.*;

public class Block {
	Wall[] walls;
	Color color;
	Color trueColor;
	int x, y, z;
	int hardness;
	TreeMap<String, Integer> contains;
	static ArrayList<BlockInfo> blockInfos = new ArrayList<BlockInfo>();
	static class BlockInfo{
		Color color;
		String name;
		int hardness;
		List<Integer> chance;
		List<String> items;
		boolean canMine;
		public BlockInfo(Color c, String n, int h, List<String> i, List<Integer> a, boolean m) {
			color = c;
			name = n;
			hardness = h;
			chance = a;
			items = i;
			canMine = m;
		}
	}
	public static void initialize() {
		blockInfos.add(new BlockInfo(new Color(0, 200, 50), "Grass block", 50,
				Arrays.asList(), Arrays.asList(), true));
		blockInfos.add(new BlockInfo(Color.gray, "Stone", 200,
				Arrays.asList(), Arrays.asList(), true));
		blockInfos.add(new BlockInfo(new Color(120, 60, 0), "Wood log", 100,
				Arrays.asList("Wood"), Arrays.asList(4), false));
		blockInfos.add(new BlockInfo(new Color(0, 127, 0), "Leaves", 30,
				Arrays.asList("Stick", "Apple"), Arrays.asList(2, 1), false));
		blockInfos.add(new BlockInfo(Color.blue, "Blue block", 80,
				Arrays.asList(), Arrays.asList(), true));
		blockInfos.add(new BlockInfo(new Color(232, 189, 136), "Wood", 80, Arrays.asList(), Arrays.asList(), true));
		blockInfos.add(new BlockInfo(new Color(182, 139, 86), "Crafting table", 100, Arrays.asList(), Arrays.asList(), true));
		blockInfos.add(new BlockInfo(new Color(122, 99, 66), "Chest", 100, Arrays.asList(), Arrays.asList(), true));
		blockInfos.add(new BlockInfo(new Color(255, 255, 127), "Torch", 20, Arrays.asList(), Arrays.asList(), true));
		blockInfos.add(new BlockInfo(Color.darkGray, "Coal", 300, Arrays.asList(), Arrays.asList(), true));
		blockInfos.add(new BlockInfo(new Color(225, 190, 170), "Iron", 300, Arrays.asList(), Arrays.asList(), true));
	}
	public Block(int x, int y, int z, Color c) {
		this.x = x;
		this.y = y;
		this.z = z;
		walls = new Wall[6];
		walls[0] = new Wall(x, z, x+100, z, y, y+100, c, x/100, y/100, z/100, 0); //North wall
		walls[1] = new Wall(x, z, x, z+100, y, y+100, c, x/100, y/100, z/100, 1); //West wall
		walls[2] = new Wall(x+100, z, x+100, z+100, y, y+100, c, x/100, y/100, z/100, 2); //East wall
		walls[3] = new Wall(x, z+100, x+100, z+100, y, y+100, c, x/100, y/100, z/100, 3); //South wall
		walls[4] = new Wall(x, z, x+100, z+100, y+100, c, x/100, y/100, z/100, 4); //Lower wall
		walls[5] = new Wall(x, z, x+100, z+100, y, c, x/100, y/100, z/100, 5); //Upper wall
		color = c;
		trueColor = c;
		hardness = getBlockInfo(this.color).hardness;
		if (getBlockInfo(c).name.equals("Chest")) {
			contains = new TreeMap<String, Integer>();
		}
	}
	public static Color getColor(Wall w) {
		Color c;
		if (w.getLayer()>Main.render-Main.fog) {
			double red = w.color.getRed()+(Main.skyColor.getRed()-w.color.getRed())*(Main.fog-Main.render+w.getLayer())/(double)Main.fog;
			double green = w.color.getGreen()+(Main.skyColor.getGreen()-w.color.getGreen())*(Main.fog-Main.render+w.getLayer())/(double)Main.fog;
			double blue = w.color.getBlue()+(Main.skyColor.getBlue()-w.color.getBlue())*(Main.fog-Main.render+w.getLayer())/(double)Main.fog;
			red = Math.min(255, Math.max(0, red));
			green = Math.min(255, Math.max(0, green));
			blue = Math.min(255, Math.max(0, blue));
			c = new Color((int)red, (int)green, (int)blue);
		} else {
			c = w.color;
		}
		return c;
	}
	public static boolean collide(int x, int y, int z, int radius) {
		int x1 = Main.xMin+2;
		int y1 = Main.yMin+2;
		int z1 = Main.zMin+2;
		int x2 = Main.xMax-2;
		int y2 = Main.yMax-2;
		int z2 = Main.zMax-2;
		if (x1*100>Main.x) {
			x1 = (int)Main.x/100;
		}
		if (y1*100>Main.y) {
			y1 = (int)Main.y/100;
		}
		if (z1*100>Main.z) {
			z1 = (int)Main.z/100;
		}
		if (x2*100<Main.x) {
			x2 = (int)Main.x/100;
		}
		if (y2*100<Main.y) {
			y2 = (int)Main.y/100;
		}
		if (z2*100<Main.z) {
			z2 = (int)Main.z/100;
		}
		for (int xx=x1; xx<x2; xx++) {
			for (int yy=y1; yy<y2; yy++) {
				for (int zz=z1; zz<z2; zz++) {
					try {
						Block b = Main.map[xx][yy][zz];
						if (b==null) {
							continue;
						}
						if (x-b.x>0-radius&&x-b.x<100+radius) {
							if (y-b.y>0-radius&&y-b.y<100+radius) {
								if (z-b.z>0-radius&&z-b.z<100+radius) {
									return true;
								}
							}
						}
					} catch (Exception e) {
					}
				}
			}
		}
		return false;
	}
	public static boolean collide(int x, int y, int z, int radius, int height) {
		int x1 = Main.xMin+2;
		int y1 = Main.yMin+2;
		int z1 = Main.zMin+2;
		int x2 = Main.xMax-2;
		int y2 = Main.yMax-2;
		int z2 = Main.zMax-2;
		if (x1*100>Main.x) {
			x1 = (int)Main.x/100;
		}
		if (y1*100>Main.y) {
			y1 = (int)Main.y/100;
		}
		if (z1*100>Main.z) {
			z1 = (int)Main.z/100;
		}
		if (x2*100<Main.x) {
			x2 = (int)Main.x/100;
		}
		if (y2*100<Main.y) {
			y2 = (int)Main.y/100;
		}
		if (z2*100<Main.z) {
			z2 = (int)Main.z/100;
		}
		for (int xx=x1; xx<x2; xx++) {
			for (int yy=y1; yy<y2; yy++) {
				for (int zz=z1; zz<z2; zz++) {
					try {
						Block b = Main.map[xx][yy][zz];
						if (b==null) {
							continue;
						}
						if (x-b.x>0-radius&&x-b.x<100+radius) {
							if (y-b.y>0-radius&&y-b.y<100+height) {
								if (z-b.z>0-radius&&z-b.z<100+radius) {
									return true;
								}
							}
						}
					} catch (Exception e) {}
				}
				Math.abs(0);
			}
		}
		return false;
	}
	public static void checkCovered(Block[][][] blocks) {
		for (int x=Main.xMin; x<Main.xMax; x++) {
			for (int y=Main.yMin; y<Main.yMax; y++) {
				for (int z=Main.zMin; z<Main.zMax; z++) {
					if (y>=Main.yMax-1) {
						for (int i=0; i<6; i++) {
							try {
								blocks[x][y][z].walls[i].needToDraw = false;
							} catch (NullPointerException e) {}
						}
						try {
							blocks[x][y][z].walls[5].needToDraw = true;
						} catch (NullPointerException e) {}
						continue;
					}
					try {
						if (blocks[x][y][z]!=null) {
							if (blocks[x][y][z-1]!=null) {
								blocks[x][y][z].walls[0].needToDraw = false;
							} else {
								blocks[x][y][z].walls[0].needToDraw = true;
							}
							if (blocks[x-1][y][z]!=null) {
								blocks[x][y][z].walls[1].needToDraw = false;
							} else {
								blocks[x][y][z].walls[1].needToDraw = true;
							}
							if (blocks[x+1][y][z]!=null) {
								blocks[x][y][z].walls[2].needToDraw = false;
							} else {
								blocks[x][y][z].walls[2].needToDraw = true;
							}
							if (blocks[x][y][z+1]!=null) {
								blocks[x][y][z].walls[3].needToDraw = false;
							} else {
								blocks[x][y][z].walls[3].needToDraw = true;
							}
							if (blocks[x][y+1][z]!=null) {
								blocks[x][y][z].walls[4].needToDraw = false;
							} else {
								blocks[x][y][z].walls[4].needToDraw = true;
							}
							if (blocks[x][y-1][z]!=null) {
								blocks[x][y][z].walls[5].needToDraw = false;
							} else {
								blocks[x][y][z].walls[5].needToDraw = true;
							}
						}
					} catch (ArrayIndexOutOfBoundsException e) {
						for (int i=0; i<6; i++) {
							blocks[x][y][z].walls[i].needToDraw = true;
						}
					}
				}
			}
		}
	}
	public void changeColor(Color c) {
		color = c;
		for (int i=0; i<6; i++) {
			walls[i].color = c;
		}
	}
	public void select() {
		int r = color.getRed()/2+127;
		int g = color.getGreen()/2+127;
		int b = color.getBlue()/2+127;
		color = new Color(r, g, b);
		for (int i=0; i<6; i++) {
			walls[i].color = color;
		}
	}
	public boolean use() {
		if (getBlockInfo(trueColor).name.equals("Crafting table")) {
			Main.craftTier = 1;
			if (Main.Inventory.contains("Hammer")) {
				Main.craftTier = 2;
			}
			Main.craftable = Items.getCraftable(Main.craftTier);
			return true;
		}
		if (getBlockInfo(trueColor).name.equals("Chest")) {
			Main.chest = this;
			return true;
		}
		return false;
	}
	public void mine(int power) {
		hardness-=power;
		List<String> names = getBlockInfo(trueColor).items;
		List<Integer> chance = getBlockInfo(trueColor).chance;
		for (int i=0; i<power; i++) {
			for (int j=0; j<chance.size(); j++) {
				if (chance.get(j)>Math.random()*100) {
					Main.Inventory.add(names.get(j), getBlockInfo(names.get(j))==null?null:getBlockInfo(names.get(j)).color);
				}
			}
		}
		if (hardness<=0) {
			if (getBlockInfo(trueColor).canMine) {
				Main.Inventory.add(getName(), trueColor);
			}
			Main.map[x/100][y/100][z/100] = null;
			Block.checkCovered(Main.map);
			Light.update(x/100, y/100, z/100);
		}
	}
	public Block delete() {
		return this;
	}
	public static BlockInfo getBlockInfo(Color c) {
		for (int i=0; i<blockInfos.size(); i++) {
			int r = c.getRed();
			int g = c.getGreen();
			int b = c.getBlue();
			if (r==blockInfos.get(i).color.getRed()&&g==blockInfos.get(i).color.getGreen()&&b==blockInfos.get(i).color.getBlue()) {
				return blockInfos.get(i);
			}
		}
		return null;
	}
	public static BlockInfo getBlockInfo(String n) {
		for (int i=0; i<blockInfos.size(); i++) {
			if (blockInfos.get(i).name.equals(n)) {
				return blockInfos.get(i);
			}
		}
		return null;
	}
	public String getName() {
		BlockInfo block = getBlockInfo(this.trueColor);
		if (block==null) {
			return "unknown block";
		} else {
			return block.name;
		}
	}
	public int getMineProgress() {
		return 100-100*hardness/getBlockInfo(this.trueColor).hardness;
	}
}
