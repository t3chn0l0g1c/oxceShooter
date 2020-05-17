package sim;

import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

public class Simulator {

	// TODO change to threadlocalrandom
	private static Random RANDOM = new Random();
	
	private static int generate(int maxInclusive) {
//		return RANDOM.nextInt()%(maxInclusive + 1);
		return RANDOM.nextInt(maxInclusive + 1);
	}

	private static double deg2Rad(double deg) {
		return deg * Math.PI / 180.0;
	}

	static class Position {
		int x;
		int y;
		int z;
		
		@Override
		public String toString() {
			return "Position (" + x + "/" +y+"/"+z+")";
		}
		
		public static Position copy(Position p) {
			Position p2 = new Position();
			p2.x = p.x;
			p2.y = p.y;
			p2.z = p.z;
			return p2;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + x;
			result = prime * result + y;
			result = prime * result + z;
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if(obj instanceof Position) {
				Position p = (Position) obj;
				return x==p.x && y==p.y && z==p.z;
			}
			return false;
		}
		
	}


	
	enum ShotType {
		Aimed,
		Snap,
		Auto;
	}
	
	public static final int VOXEL_PER_TILE = 16;
	
	public static void applyAccuracy(Position origin, Position target, double accuracy, boolean keepRange,
			boolean extendLine, Weapon weapon, ShotType shotType) {
		int xdiff = origin.x - target.x;
		int ydiff = origin.y - target.y;
		double realDistance = Math.sqrt((double) (xdiff * xdiff) + (double) (ydiff * ydiff));
		double maxRange = keepRange ? realDistance : VOXEL_PER_TILE * 1000; // 1000 tiles
			double modifier = 0.0;
			int upperLimit = weapon.aimRange;
			int lowerLimit = weapon.minRange;
				if (shotType == ShotType.Auto) {
					upperLimit = weapon.autoRange;
				} else if (shotType == ShotType.Snap) {
					upperLimit = weapon.snapRange;
				}
			if (realDistance / VOXEL_PER_TILE < lowerLimit) {
				modifier = (weapon.dropOff * (lowerLimit - realDistance / VOXEL_PER_TILE)) / 100;
			} else if (upperLimit < realDistance / VOXEL_PER_TILE) {
				modifier = (weapon.dropOff * (realDistance / VOXEL_PER_TILE - upperLimit)) / 100;
			}
			accuracy = Math.max(0.0, accuracy - modifier);

		int xDist = Math.abs(origin.x - target.x);
		int yDist = Math.abs(origin.y - target.y);
		int zDist = Math.abs(origin.z - target.z);
		int xyShift, zShift;

		if (xDist / 2 <= yDist) { // yes, we need to add some x/y non-uniformity
			xyShift = xDist / 4 + yDist; // and don't ask why, please. it's The Commandment
		} else {
			xyShift = (xDist + yDist) / 2; // that's uniform part of spreading
		}
		if (xyShift <= zDist) { // slight z deviation
			zShift = xyShift / 2 + zDist;
		} else {
			zShift = xyShift + zDist / 2;
		}
		int deviation = generate(100) - (int) (accuracy * 100);

		if (deviation >= 0) {
			deviation += 50; // add extra spread to "miss" cloud
		} else {
			deviation += 10; // accuracy of 109 or greater will become 1 (tightest spread)
		}
		deviation = Math.max(1, zShift * deviation / 200); // range ratio

		target.x += generate(deviation) - deviation / 2;
		target.y += generate(deviation) - deviation / 2;
		target.z += generate(deviation / 2) / 2 - deviation / 8;

		if (extendLine) {
			double rotation, tilt;
			rotation = Math.atan2((double) (target.y - origin.y), (double) (target.x - origin.x)) * 180 / Math.PI;
			tilt = Math.atan2((double) (target.z - origin.z),
					Math.sqrt((double) (target.x - origin.x) * (double) (target.x - origin.x)
							+ (double) (target.y - origin.y) * (double) (target.y - origin.y)))
					* 180 / Math.PI;
			// calculate new target
			// this new target can be very far out of the map, but we don't care about that
			// right now
			double cos_fi = Math.cos(deg2Rad(tilt));
			double sin_fi = Math.sin(deg2Rad(tilt));
			double cos_te = Math.cos(deg2Rad(rotation));
			double sin_te = Math.sin(deg2Rad(rotation));
			target.x = (int) (origin.x + maxRange * cos_te * cos_fi);
			target.y = (int) (origin.y + maxRange * sin_te * cos_fi);
			target.z = (int) (origin.z + maxRange * sin_fi);
		}
	}

	static class Target {
		int x;
		int y;
		int z;
		int width;
		int height;
		
		private static boolean isBetween(int x, int range, int t) {
			return t>=x && t<=x+range;
		}
		
		public boolean isHit(Position p) {
			return isBetween(x, width, p.x) && isBetween(z, height, p.z);
		}
	}
	
	private static double toPercent(double shots, double hits) {
		return hits*100 / shots;
	}
	
	public static double simulateAcc(Position source, Target target, Weapon w, double acc, int simulations, int shots, ShotType type, Cache cache) {
		int hit = 0;
		for(int i = 0; i<simulations; i++) {
			for(int s = 0; s<shots; s++) {
				Position p = new Position();
				p.x = target.x + target.width/2;
				p.y = target.y;
				p.z = target.z + target.height/2;
				
				applyAccuracy(source, p, acc, true, false, w, type);
				
				Boolean r = cache.get(p);
				if(r==null) {
					r = Test.intersects(Position.copy(source), Position.copy(p), target);
//					cache.put(p, r);
				}
				if(r) {
					hit++;
				}
			}
		}
		
		return toPercent(simulations, hit);
	}
	
}
