package planner;

//Java import
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Collection;
import java.lang.Exception;

//Logist import
import logist.simulation.Vehicle;

public class Solution implements Comparable<Solution>{
	private HashMap<PTask, PTask> nextTask_t;
	private HashMap<Vehicle, PTask> nextTask_v;
	private HashMap<PTask, Integer> time;
	private HashMap<PTask, Vehicle> vehicle;
	
	public Solution() {
		this.nextTask_t = new HashMap<PTask, PTask>();
		this.nextTask_v = new HashMap<Vehicle, PTask>();
		this.time = new HashMap<PTask, Integer>();
		this.vehicle = new HashMap<PTask, Vehicle>();
	}

	public Solution(Solution solution) {
		this.nextTask_t = new HashMap<PTask, PTask>();
		this.nextTask_v = new HashMap<Vehicle, PTask>();
		this.time = new HashMap<PTask, Integer>();
		this.vehicle = new HashMap<PTask, Vehicle>();
		
		this.nextTask_t.putAll(solution.getNextTask_t());
		this.nextTask_v.putAll(solution.getNextTask_v());
		this.time.putAll(solution.getTime());
		this.vehicle.putAll(solution.getVehicle());
		
		//for (PTask task: solution.getNextTask_t().keySet()) {
			//this.nextTask_t.put(task, solution.getNextTask_t().get(task));
			//this.time.put(task, solution.getTime().get(task));
			//this.vehicle.put(task, solution.getVehicle().get(task));
		//}
		//for (Vehicle v: solution.getNextTask_v().keySet())
			//this.nextTask_v.put(v, this.nextTask_v.get(v));
	}
	
	public void firstGuess(List<Vehicle> vehicles, List<PTask> pickups, List<PTask> deliveries) {
		//int t = 1;
		VehicleCapacityComparator comparator = new VehicleCapacityComparator();
		Vehicle vMax = Collections.max(vehicles, comparator);
		long capacity = vMax.capacity();

		//generate of list of vehicle times so we can generate a correct times variable
		List<Integer> times = new ArrayList<Integer>();
		for(int i = 0; i <vehicles.size(); i++){
			times.add(0);
		}

		//generate a list of "las	t pickups" so that we can correctly generate nextTask_t
		List<PTask> lastPickup = new ArrayList<PTask>();
		for (int i = 0; i < pickups.size(); i++) {
			lastPickup.add(null);
		}

		for(int i = 0; i < pickups.size(); i++) {
			this.nextTask_t.put(deliveries.get(i), null);
		}

		// iterate over every single task
		for(int i = 0; i < pickups.size(); i++) {
			System.out.println("pickups");
			System.out.println(pickups.get(i));
			System.out.println("deliveries");
			System.out.println(deliveries.get(i));
			int test = 1;
			if (pickups.get(i).getWeight() > capacity)
				test = 1;
			
			try {
				long w = capacity/test;
			}
			
			catch (Exception e){
				System.out.println("Problem unsolvable: task weight bigger that maximum capacity.");
			}

			int vID = 0;
			Vehicle v = null;
			
			do{
				vID = (int) (Math.random() * vehicles.size());
				v = vehicles.get(vID);
			}while(v.capacity() < pickups.get(i).getWeight());
			
			this.time.put(pickups.get(i), times.get(vID));
			times.set(vID,times.get(vID)+1);
			this.time.put(deliveries.get(i), times.get(vID));
			times.set(vID, times.get(vID) + 1);

			//this.time.put(pickups.get(i), t);
			//t++;
			//this.time.put(deliveries.get(i), t);
			//t++;
			
			this.nextTask_t.put(pickups.get(i), deliveries.get(i));
			
			System.out.println(v.id());

			if(times.get(vID) <=2){
				this.nextTask_v.put(v, pickups.get(i));
			}
			else{
				System.out.println(lastPickup.get(vID));
				this.nextTask_t.put(lastPickup.get(vID), pickups.get(i));
				System.out.println(this.nextTask_t.get(lastPickup.get(vID)));
			}
			lastPickup.set(vID, deliveries.get(i));
			
			/*if (i == 0) 
				this.nextTask_v.put(vMax, pickups.get(i));
			else 
				this.nextTask_t.put(deliveries.get(i-1), pickups.get(i));*/

			this.nextTask_t.put(pickups.get(i), deliveries.get(i));
			System.out.println(this.nextTask_t.get(pickups.get(i)));
			this.vehicle.put(pickups.get(i), v);
			this.vehicle.put(deliveries.get(i), v);

			System.out.println("NEXT STEP\n");
			
			//this.nextTask_t.put(pickups.get(i), deliveries.get(i));
			//this.vehicle.put(pickups.get(i), vMax);
			//this.vehicle.put(deliveries.get(i), vMax);
			
			/*if (i == deliveries.size()-1)
				this.nextTask_t.put(deliveries.get(i), null);*/
		}
		
		//for (Vehicle v: vehicles) {
		for (int vID = 0; vID<vehicles.size();vID++){
			//if (v != vMax)
			if(times.get(vID) == 0)
				//this.nextTask_v.put(v, null);
				this.nextTask_v.put(vehicles.get(vID), null);
		}
		System.out.println("this.nextTask_t");
		System.out.println(this.nextTask_t);
		System.out.println("this.nextTask_v");
		System.out.println(this.nextTask_v);
		System.out.println("this.time");
		System.out.println(this.time);
		System.out.println("this.vehicle");
		System.out.println(this.vehicle);
	}
	
	public boolean valid(List<Constraint> constraints) {
		boolean res = true;
		for (Constraint constraint: constraints) {
			if (!constraint.compatible(this)) {
				res = false;
				break;
			}
		}
		return res;
	}
	
	public void changeVehicle(Vehicle v1, Vehicle v2) {
		PTask firstTaskPickup = this.nextTask_v.get(v1);
		PTask justBeforeDeliver = firstTaskPickup;
		PTask firstTaskDeliver = this.nextTask_t.get(firstTaskPickup);
		int ID = firstTaskPickup.getID();
		while (ID != firstTaskDeliver.getID()) {
			try {
				justBeforeDeliver = firstTaskDeliver;
				firstTaskDeliver = this.nextTask_t.get(firstTaskDeliver);
			}
			catch (Exception e) {
				System.out.println("Error changeVehicle: Delivery task not found.");
			}
		}
		
		if(justBeforeDeliver == firstTaskPickup) {
			this.nextTask_v.put(v1, this.nextTask_t.get(firstTaskDeliver));		
			this.nextTask_t.put(firstTaskDeliver, this.nextTask_v.get(v2));
			this.nextTask_v.put(v2, firstTaskPickup);
			
			this.vehicle.put(firstTaskPickup, v2);
			this.vehicle.put(firstTaskDeliver, v2);
		}
		
		
		else {
			this.nextTask_v.put(v1, this.nextTask_t.get(firstTaskPickup));
			this.nextTask_t.put(justBeforeDeliver, this.nextTask_t.get(firstTaskDeliver));
			this.nextTask_t.put(firstTaskDeliver, this.nextTask_v.get(v2));
			this.nextTask_v.put(v2, firstTaskPickup);
			this.nextTask_t.put(firstTaskPickup, firstTaskDeliver);
			
			this.vehicle.put(firstTaskPickup, v2);
			this.vehicle.put(firstTaskDeliver, v2);
		}
		this.updateTime(v1);
		this.updateTime(v2);
	}
	
	public void updateTime(Vehicle v) {
		PTask firstTask = this.nextTask_v.get(v);
		if (firstTask != null) {
			this.time.put(firstTask, 1);
			PTask nextTask = this.nextTask_t.get(firstTask);
			int count = 1;
			while (nextTask != null) {
				this.time.put(nextTask, count+1);
				firstTask = nextTask;
				nextTask = this.nextTask_t.get(nextTask);
				count++;
			}
		}
	}
	
	public void changeTaskOrder(Vehicle v, PTask t1, PTask t2) {
		PTask postT1 = this.nextTask_t.get(t1);
		PTask postT2 = this.nextTask_t.get(t2);
		
		if (t2 == postT1) {
			PTask prev = null;
			PTask next = this.nextTask_v.get(v);
			while (next != t1) {
				try {
					prev = next;
					next = this.nextTask_t.get(next);
				}
				catch (Exception e) {
					System.out.println("Error changeTaskOrder: t1 not found");
				}
			}
			if (prev == null) 
				this.nextTask_v.put(v, t2);
			else
				this.nextTask_t.put(prev, t2);
			this.nextTask_t.put(t2, t1);
			this.nextTask_t.put(t1, postT2);
		}
		
		else {
			PTask prev = null;
			PTask next = this.nextTask_v.get(v);
			
			while  (next != t1) {
				try {
					prev = next;
					next = this.nextTask_t.get(next);
				}
				catch (Exception e) {
					System.out.println("Error changeTaskOrder: t1 not found");
				}
			}
			
			if (prev == null)
				this.nextTask_v.put(v, t2);
			else
				this.nextTask_t.put(prev, t2);
			this.nextTask_t.put(t2, postT1);
			
			while (next != t2) {
				try {
					prev = next;
					next = this.nextTask_t.get(next);
				}
				catch (Exception e) {
					System.out.println("Error changeTaskOrder: t2 not found");
				}
			}
			
			this.nextTask_t.put(prev, t1);
			this.nextTask_t.put(t1, postT2);
		}
		
		this.updateTime(v);
	}
	
	public List<Solution> neighbours(List<Constraint> constraints){
		List<Solution> solutions = new ArrayList<Solution>();
		for (Vehicle v1: this.nextTask_v.keySet()) {
			if (this.nextTask_v.get(v1) == null)
				continue;
			PTask currentTaskV1 = this.nextTask_v.get(v1);
			while(currentTaskV1 != null) {
				PTask nextTaskV1 = this.nextTask_t.get(currentTaskV1);
				while(nextTaskV1 != null) {
					Solution temp = new Solution(this);
					temp.changeTaskOrder(v1, currentTaskV1, nextTaskV1);
					nextTaskV1 = this.nextTask_t.get(nextTaskV1);
					//System.out.println(temp);
					if (temp.valid(constraints)) {
						solutions.add(temp);
						//System.out.println("Gard !");
					}
				}
				currentTaskV1 = this.nextTask_t.get(currentTaskV1);
			}
			//System.out.println(this);
			for(Vehicle v2: this.nextTask_v.keySet()) {
				if (v2 == v1)
					continue;
				Solution temp = new Solution(this);
				temp.changeVehicle(v1, v2);
				//System.out.println(temp);
				if (temp.valid(constraints)) {
					solutions.add(temp);
					//System.out.println("Gard.");
				}
			}
		}
		//System.out.println(solutions);
		return solutions;
	}
	
	public int cost() {
		int res = 0;
		for (Vehicle v : this.nextTask_v.keySet()) {
			double costV = 0;
			PTask currentTask = this.nextTask_v.get(v);
			if (currentTask != null)
				costV += currentTask.getCity().distanceTo(v.getCurrentCity());
			while(currentTask != null) {
				PTask nextTask = this.nextTask_t.get(currentTask);
				if (nextTask != null)
					costV += currentTask.getCity().distanceTo(nextTask.getCity());
				currentTask = nextTask;
			}
			costV *= v.costPerKm();
			res += costV;
		}
		return res;
	}
	
	@Override
	public int compareTo(Solution other) {
		return this.cost() - other.cost();
	}
	
	public HashMap<PTask, PTask> getNextTask_t(){
		return this.nextTask_t;
	}
	
	public HashMap<Vehicle, PTask> getNextTask_v(){
		return this.nextTask_v;
	}
	
	public HashMap<PTask, Integer> getTime(){
		return this.time;
	}
	
	public HashMap<PTask, Vehicle> getVehicle(){
		return this.vehicle;
	}
	
	@Override
	public String toString() {
		String r = "";
		for (Vehicle v: this.nextTask_v.keySet()) {
			r += v + " " + v.capacity() + ": " ;
			PTask currentTask = this.nextTask_v.get(v);
			while(currentTask != null) {
				r += currentTask.toString() + "; ";
				currentTask = this.nextTask_t.get(currentTask);
			}
			r += "\n";
		}
		return r;
	}
}

