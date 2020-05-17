package sim;

import java.util.LinkedHashMap;

import sim.Simulator.Position;

public class Cache extends LinkedHashMap<Position, Boolean>{

	private final int targetSize;

	public Cache(int targetSize) {
		this.targetSize = targetSize;
	}

	@Override
	protected boolean removeEldestEntry(java.util.Map.Entry<Position, Boolean> eldest) {
		return size() > targetSize;
	}
}
