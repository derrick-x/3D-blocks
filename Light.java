import java.util.*;

public class Light {
	static byte[][][] light;
	static boolean[][][] sunlight;
	public static double getLight(int x, int y, int z) {
		if (sunlight[x][y][z]) {
			double sun = 0;
			if (Main.time<2500) {
				sun = Main.time/250.0;
			} else if (Main.time<5000) {
				sun = 10;
			} else if (Main.time<7500) {
				sun = 30-Main.time/250.0;
			}
			return Math.max(sun, light[x][y][z]);
		} else {
			return light[x][y][z];
		}
	}
	public static void initialize() {
		light = new byte[300][100][300];
		sunlight = new boolean[300][100][300];
		for (int x=0; x<300; x++) {
			for (int z=0; z<300; z++) {
				for (int y=0; y<100; y++) {
					if (Main.map[x][y][z]!=null) {
						break;
					}
					sunlight[x][y][z] = true;
				}
			}
		}
		Queue<int[]> spread = new LinkedList<int[]>();
		for (int x=0; x<300; x++) {
			for (int y=0; y<100; y++) {
				for (int z=0; z<300; z++) {
					if (Main.map[x][y][z]!=null&&Main.map[x][y][z].getName().equals("Torch")) {
						light[x][y][z] = 10;
						int[] point = {x, y, z};
						spread.add(point);
					}
				}
			}
		}
		while (spread.size()>0) {
			int x = spread.peek()[0];
			int y = spread.peek()[1];
			int z = spread.poll()[2];
			if (light[x][y][z]>1) {
				if (x>0&&Main.map[x-1][y][z]==null&&light[x-1][y][z]<light[x][y][z]-1) {
					light[x-1][y][z] = (byte) (light[x][y][z]-1);
					int[] point = {x-1, y, z};
					spread.add(point);
				}
				if (y>0&&Main.map[x][y-1][z]==null&&light[x][y-1][z]<light[x][y][z]-1) {
					light[x][y-1][z] = (byte) (light[x][y][z]-1);
					int[] point = {x, y-1, z};
					spread.add(point);
				}
				if (z>0&&Main.map[x][y][z-1]==null&&light[x][y][z-1]<light[x][y][z]-1) {
					light[x][y][z-1] = (byte) (light[x][y][z]-1);
					int[] point = {x, y, z-1};
					spread.add(point);
				}
				if (x<299&&Main.map[x+1][y][z]==null&&light[x+1][y][z]<light[x][y][z]-1) {
					light[x+1][y][z] = (byte) (light[x][y][z]-1);
					int[] point = {x+1, y, z};
					spread.add(point);
				}

				if (y<99&&Main.map[x][y+1][z]==null&&light[x][y+1][z]<light[x][y][z]-1) {
					light[x][y+1][z] = (byte) (light[x][y][z]-1);
					int[] point = {x, y+1, z};
					spread.add(point);
				}
				if (z<299&&Main.map[x][y][z+1]==null&&light[x][y][z+1]<light[x][y][z]-1) {
					light[x][y][z+1] = (byte) (light[x][y][z]-1);
					int[] point = {x, y, z+1};
					spread.add(point);
				}
			}
		}
	}
	public static void update(int x, int y, int z) {
		for (int yy=0; yy<100; yy++) {
			if (Main.map[x][yy][z]!=null) {
				for (yy+=0; yy<100; yy++) {
					sunlight[x][yy][z] = false;
				}
				break;
			}
			sunlight[x][yy][z] = true;
		}
		Queue<int[]> spread = new LinkedList<int[]>();
		if (Main.map[x][y][z]!=null) {
			if (Main.map[x][y][z].getName().equals("Torch")) {
				light[x][y][z] = 10;
				int[] point = {x, y, z};
				spread.add(point);
			} else {
				light[x][y][z] = 0;
				int[] point = {x, y, z};
				spread.add(point);
			}
		} else {
			if (getRealLight(x, y, z)!=light[x][y][z]) {
				light[x][y][z] = getRealLight(x, y, z);
				int[] point = {x, y, z};
				spread.add(point);
			}
		}
		while (spread.size()>0) {
			int xx = spread.peek()[0];
			int yy = spread.peek()[1];
			int zz = spread.poll()[2];
			if (xx>0&&Main.map[xx-1][yy][zz]==null&&getRealLight(xx-1, yy, zz)!=light[xx-1][yy][zz]) {
				light[xx-1][yy][zz] = getRealLight(xx-1, yy, zz);
				int[] point = {xx-1, yy, zz};
				spread.add(point);
			}
			if (xx<299&&Main.map[xx+1][yy][zz]==null&&getRealLight(xx+1, yy, zz)!=light[xx+1][yy][zz]) {
				light[xx+1][yy][zz] = getRealLight(xx+1, yy, zz);
				int[] point = {xx+1, yy, zz};
				spread.add(point);
			}
			if (yy>0&&Main.map[xx][yy-1][zz]==null&&getRealLight(xx, yy-1, zz)!=light[xx][yy-1][zz]) {
				light[xx][yy-1][zz] = getRealLight(xx, yy-1, zz);
				int[] point = {xx, yy-1, zz};
				spread.add(point);
			}
			if (yy<99&&Main.map[xx][yy+1][zz]==null&&getRealLight(xx, yy+1, zz)!=light[xx][yy+1][zz]) {
				light[xx][yy+1][zz] = getRealLight(xx, yy+1, zz);
				int[] point = {xx, yy+1, zz};
				spread.add(point);
			}
			if (zz>0&&Main.map[xx][yy][zz-1]==null&&getRealLight(xx, yy, zz-1)!=light[xx][yy][zz-1]) {
				light[xx][yy][zz-1] = getRealLight(xx, yy, zz-1);
				int[] point = {xx, yy, zz-1};
				spread.add(point);
			}
			if (zz<299&&Main.map[xx][yy][zz+1]==null&&getRealLight(xx, yy, zz+1)!=light[xx][yy][zz+1]) {
				light[xx][yy][zz+1] = getRealLight(xx, yy, zz+1);
				int[] point = {xx, yy, zz+1};
				spread.add(point);
			}
		}
	}
	private static byte getRealLight(int x, int y, int z) {
		int rlight = 0;
		if (x>0) {
			rlight = Math.max(rlight, light[x-1][y][z]-1);
		}
		if (y>0) {
			rlight = Math.max(rlight, light[x][y-1][z]-1);
		}
		if (z>0) {
			rlight = Math.max(rlight, light[x][y][z-1]-1);
		}
		if (x<299) {
			rlight = Math.max(rlight, light[x+1][y][z]-1);
		}
		if (y<99) {
			rlight = Math.max(rlight, light[x][y+1][z]-1);
		}
		if (z<299) {
			rlight = Math.max(rlight, light[x][y][z+1]-1);
		}
		return (byte)rlight;
	}
}
