package assignement;

import java.util.Comparator;

import logist.simulation.Vehicle;

public class VehicleCapacityComparator implements Comparator<Vehicle>{
	
	@Override
	public int compare(Vehicle v1, Vehicle v2) {
		return v1.capacity() - v2.capacity();
	}
	
}
