package sim;

import sim.Simulator.Position;
import sim.Simulator.Target;

public class Test {
	static class Vector3 {
		public float x, y, z;

		public Vector3(float x, float y, float z) {
			this.x = x;
			this.y = y;
			this.z = z;
		}

		public Vector3 add(Vector3 other) {
			return new Vector3(x + other.x, y + other.y, z + other.z);
		}

		public Vector3 sub(Vector3 other) {
			return new Vector3(x - other.x, y - other.y, z - other.z);
		}

		public Vector3 scale(float f) {
			return new Vector3(x * f, y * f, z * f);
		}

		public Vector3 cross(Vector3 other) {
			return new Vector3(y * other.z - z * other.y, z - other.x - x * other.z, x - other.y - y * other.x);
		}

		public float dot(Vector3 other) {
			return x * other.x + y * other.y + z * other.z;
		}
	}
	
	public static boolean intersects(Position ray1, Position ray2, Target t) {
		Vector3 square1 = new Vector3(t.x, t.y, t.z);
		Vector3 square2 = new Vector3(t.x + t.width, t.y, t.z);
		Vector3 square3 = new Vector3(t.x + t.width, t.y, t.z+t.height);
		Vector3 rayOrigin = new Vector3(ray1.x, ray1.y, ray1.z);
		Vector3 rayDir = new Vector3(ray2.x, ray2.y, ray2.z);
		return intersectRayWithSquare(rayOrigin, rayDir, square1, square2, square3);
	}

	public static boolean intersectRayWithSquare(Vector3 rayOrigin, Vector3 rayDir, Vector3 square1, Vector3 square2, Vector3 square3) {
		// 1.
		Vector3 dS21 = square2.sub(square1);
		Vector3 dS31 = square3.sub(square1);
		Vector3 n = dS21.cross(dS31);

		// 2.
		Vector3 dR = rayOrigin.sub(rayDir);

		float ndotdR = n.dot(dR);

		if (Math.abs(ndotdR) < 1e-6f) { // Choose your tolerance
			return false;
		}

		float t = -n.dot(rayOrigin.sub(square1)) / ndotdR;
		Vector3 M = rayOrigin.add(dR.scale(t));

		// 3.
		Vector3 dMS1 = M.sub(square1);
		float u = dMS1.dot(dS21);
		float v = dMS1.dot(dS31);

		// 4.
		return (u >= 0.0f && u <= dS21.dot(dS21) && v >= 0.0f && v <= dS31.dot(dS31));
	}

}