import javax.sound.sampled.*;
import java.awt.*;
import java.io.*;
import java.util.*;

public class Files {
	static final HashMap<String, Image> imageList = new HashMap<>();
	static Clip music;
	static String currMusic = "";
	static HashMap<String, File> getMusic;
	static int progress = 0;
	public static void loadWorld(int index) throws IOException {
		File[] worlds = new File("worlds").listFiles();
		if (index>=worlds.length) {
			return;
		}
		progress = 1;
		Main.drawDisplay();
		Main.canvas.paint(Main.canvas.getGraphics());
		File world = worlds[index];
		Main.worldName = world.getName();
		BufferedReader worldReader = new BufferedReader(new FileReader(world));
		Main.day = Integer.parseInt(worldReader.readLine());
		Main.time = Integer.parseInt(worldReader.readLine());
		String[] pos = worldReader.readLine().split(" ");
		Main.x = Integer.parseInt(pos[0]);
		Main.y = Integer.parseInt(pos[1]);
		Main.z = Integer.parseInt(pos[2]);
		Main.yVel = Integer.parseInt(worldReader.readLine());
		Main.health = Integer.parseInt(worldReader.readLine());
		Main.regen = Integer.parseInt(worldReader.readLine());
		int inventory = Integer.parseInt(worldReader.readLine());
		for (int i=0; i<inventory; i++) {
			String[] args = worldReader.readLine().split(" ");
			String name = args[0].replace('~', ' ');
			int amount = Integer.parseInt(args[1]);
			try {
				Main.Inventory.display.add(new Main.Inventory(name, new Color(Integer.parseInt(args[2]), Integer.parseInt(args[3]), Integer.parseInt(args[4])), amount));
			} catch (Exception e) {
				Main.Inventory.display.add(new Main.Inventory(name, null, amount));
			}
			Main.Inventory.total+=amount;
		}
		int entities = Integer.parseInt(worldReader.readLine());
		for (int i=0; i<entities; i++) {
			String[] args = worldReader.readLine().split(" ");
			try {
				Main.entities.add(new Entity(Integer.parseInt(args[0]), Integer.parseInt(args[1]), Integer.parseInt(args[2]), args[3].equals("true"), Integer.parseInt(args[4]), Integer.parseInt(args[5]), Integer.parseInt(args[6]), new Color(Integer.parseInt(args[7]), Integer.parseInt(args[8]), Integer.parseInt(args[9]))));
			} catch (Exception e) {}
		}
		for (int x=0; x<300; x++) {
			progress++;
			for (int y=0; y<100; y++) {
				String[] args = worldReader.readLine().split(" ");
				for (int z=0; z<300; z++) {
					try {
						Main.map[x][y][z] = new Block(x*100, y*100, z*100, new Color(Integer.parseInt(args[z*3]), Integer.parseInt(args[z*3+1]), Integer.parseInt(args[z*3+2])));
					} catch (Exception e) {}
				}
			}
			Main.drawDisplay();
			Main.canvas.paint(Main.canvas.getGraphics());
		}
		int chests = Integer.parseInt(worldReader.readLine());
		for (int i=0; i<chests; i++) {
			String[] args = worldReader.readLine().split(" ");
			int x = Integer.parseInt(args[0]);
			int y = Integer.parseInt(args[1]);
			int z = Integer.parseInt(args[2]);
			int items = Integer.parseInt(args[3]);
			for (int j=0; j<items; j++) {
				Main.map[x][y][z].contains.put(args[j*2+4].replace('~', ' '), Integer.parseInt(args[j*2+5]));
			}
		}
		worldReader.close();
		Light.initialize();
		Main.gameStage = 1;
	}
	public static void saveWorld(String name) {
		System.out.println("Saving world...");
		try {
			FileWriter save = new FileWriter("worlds\\"+name+".txt");
			save.append(Main.day+"\n");
			save.append(Main.time+"\n");
			save.append((int)Main.x+" "+(int)Main.y+" "+(int)Main.z+"\n");
			save.append((int)Main.yVel+"\n");
			save.append((int)Main.health+"\n");
			save.append((int)Main.regen+"\n");
			save.append(Main.Inventory.display.size()+"\n");
			for (Main.Inventory i : Main.Inventory.display) {
				save.append(i.name.replace(' ', '~')+" "+i.amount+" ");
				if (i.color==null) {
					save.append("256 0 0\n");
				} else {
					save.append(i.color.getRed()+" "+i.color.getGreen()+" "+i.color.getBlue()+"\n");
				}
			}
			save.append(Main.entities.size()+"\n");
			for (int i=0; i<Main.entities.size(); i++) {
				save.append(Main.entities.get(i).toString()+"\n");
			}
			ArrayList<int[]> chests = new ArrayList<int[]>();
			for (int x=0; x<300; x++) {
				for (int y=0; y<100; y++) {
					for (int z=0; z<300; z++) {
						if (Main.map[x][y][z]==null) {
							save.append("256 0 0 ");
						} else {
							save.append(Main.map[x][y][z].color.getRed()+" "+Main.map[x][y][z].color.getGreen()+" "+Main.map[x][y][z].color.getBlue()+" ");
							if (Main.map[x][y][z].contains!=null) {
								int[] point = {x, y, z};
								chests.add(point);
							}
						}
					}
					save.append("\n");
				}
			}
			save.append(chests.size()+"\n");
			for (int i=0; i<chests.size(); i++) {
				int x = chests.get(i)[0];
				int y = chests.get(i)[1];
				int z = chests.get(i)[2];
				save.append(x+" "+y+" "+z+" "+Main.map[x][y][z].contains.size());
				for (String t : Main.map[x][y][z].contains.keySet()) {
					save.append(" "+t.replace(' ', '~')+" "+Main.map[x][y][z].contains.get(t));
				}
				save.append('\n');
			}
			System.out.println("World saved!");
			save.close();
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("Error occured while saving world");
		}
	}
	public static void playSound(String name) {
		File file = new File(name+".wav");
		try {
			Clip sound = AudioSystem.getClip();
			sound.open(AudioSystem.getAudioInputStream(file));
			sound.start();
		} catch (Exception e) {
			System.out.println("File not found");
		}
	}
	public static void doMusic() {
		try {
			if (music==null) {
				music = AudioSystem.getClip();
				music.open(AudioSystem.getAudioInputStream(new File("ambience.wav")));
				music.start();
			}
			if (!music.isActive()) {
				music.setFramePosition(0);
				music.start();
			}
		} catch (Exception e) {
			System.out.println("File not found");
		}
	}
	public static Image getImage(String name) {
		if (imageList.size()==0) {
			File[] images = new File("images").listFiles();
			for (int i=0; i<images.length; i++) {
				imageList.put(images[i].getName(), Toolkit.getDefaultToolkit().getImage("images\\"+images[i].getName()));
			}
		}
		return imageList.get(name+".jpg");
	}
}
