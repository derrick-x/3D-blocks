import java.util.*;
import java.io.*;

public class Items {
	static ArrayList<String> items = new ArrayList<String>();
	static int[][] crafting;
	static int[] amount;
	public static void initialize() throws IOException{
		Scanner scan = new Scanner(new File("crafting.txt"));
		String next = scan.nextLine();
		items.add(null);
		while (!next.equals("-----")) {
			items.add(next);
			next = scan.nextLine();
		}
		crafting = new int[items.size()][];
		amount = new int[items.size()];
		while (scan.hasNext()) {
			int item = scan.nextInt();
			int tier = scan.nextInt();
			int recipe = scan.nextInt();
			amount[item] = scan.nextInt();
			crafting[item] = new int[items.size()];
			crafting[item][0] = tier;
			for (int i=0; i<recipe; i++) {
				crafting[item][scan.nextInt()] = scan.nextInt();
			}
		}
		return;
	}
	public static ArrayList<Integer> getCraftable(int tier){
		ArrayList<Integer> craftable = new ArrayList<Integer>();
		int[] inventory = new int[items.size()];
		for (Main.Inventory i : Main.Inventory.display) {
			inventory[items.indexOf(i.name)] = i.amount;
		}
		for (int i=1; i<items.size(); i++) {
			if (crafting[i]!=null) {
				boolean canCraft = true;
				for (int j=1; j<items.size(); j++) {
					if (inventory[j]<crafting[i][j]) {
						canCraft = false;
						break;
					}
				}
				if (canCraft&&crafting[i][0]<=tier) {
					craftable.add(i);
				}
			}
		}
		return craftable;
	}
	public static void craft(int item) {
		boolean added = false;
		for (int i=0; i<Main.Inventory.display.size(); i++) {
			if (Main.Inventory.display.get(i).name.equals(items.get(item))) {
				Main.Inventory.display.get(i).amount+=amount[item];
				added = true;
			}
			Main.Inventory.display.get(i).amount-=crafting[item][items.indexOf(Main.Inventory.display.get(i).name)];
			Main.Inventory.total-=crafting[item][items.indexOf(Main.Inventory.display.get(i).name)];
			if (Main.Inventory.display.get(i).amount==0) {
				Main.Inventory.display.remove(i);
				i--;
			}
		}
		if (!added) {
			Main.Inventory.display.add(new Main.Inventory(items.get(item), Block.getBlockInfo(items.get(item))==null?null:Block.getBlockInfo(items.get(item)).color, amount[item]));
			Main.Inventory.total+=amount[item];
		}
	}
}
