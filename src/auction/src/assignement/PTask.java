package assignement;

import logist.topology.Topology.City;


public class PTask {
	private int id;
	private City city;
	private boolean pickup; //Here true = pickup, false = delivery
	private long weight;
	
	public PTask(int i, City c, boolean p, long w) {
		this.id = i;
		this.city = c;
		this.pickup = p;
		this.weight = w;
	}
	
	public int getID() {
		return id;
	}
	
	public City getCity() {
		return city;
	}
	
	public boolean getPickup() {
		return pickup;
	}
	
	public long getWeight() {
		return weight;
	}
	
	public String toString() {
		return "(" + this.id + ", " + this.pickup + ", " + this.city.toString() + ", " + this.weight + ")";
	}
}
