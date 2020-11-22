package planner;

//the list of imports
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.io.File;

import logist.LogistSettings;
import logist.Measures;
import logist.behavior.AuctionBehavior;
import logist.behavior.CentralizedBehavior;
import logist.agent.Agent;
import logist.config.Parsers;
import logist.simulation.Vehicle;
import logist.plan.Plan;
import logist.task.Task;
import logist.task.TaskDistribution;
import logist.task.TaskSet;
import logist.topology.Topology;
import logist.topology.Topology.City;

public class CentralizedPlanner implements CentralizedBehavior{
	
	private Topology topology;
	private TaskDistribution distribution;
	private Agent agent;
	private long timeout_setup;
	private long timeout_plan;
	private Random randomGenerator;
	private double probability;
	private List<PTask> pickups;
	private List<PTask> deliveries;
	private List<Constraint> constraints;
	
	@Override
	public void setup(Topology topology, TaskDistribution distribution, Agent agent) {
		
		// this code is used to get the timeouts
        /*LogistSettings ls = null;
        try {
            ls = Parsers.parseSettings("config" + File.separator + "settings_default.xml");
        }
        catch (Exception exc) {
            System.out.println("There was a problem loading the configuration file.");
        }*/
        
     // the setup method cannot last more than timeout_setup milliseconds
        timeout_setup = 10000; //ls.get(LogistSettings.TimeoutKey.SETUP);
        // the plan method cannot execute more than timeout_plan milliseconds
        timeout_plan = 10000; //ls.get(LogistSettings.TimeoutKey.PLAN);
        
        this.topology = topology;
        this.distribution = distribution;
        this.agent = agent;
        this.randomGenerator = new Random(12345);
        this.probability = 0.4;
	}
	
	public List<Plan> plan(List<Vehicle> vehicles, TaskSet tasks) {
		List<Plan> plans = new ArrayList<Plan>();
		
		System.out.println("Building Tasks");
		this.pickups = convertPickup(tasks);
		this.deliveries = convertDeliveries(tasks);
		System.out.println("Done !");
		
		System.out.println("Building Constraints");
		this.constraints = new ArrayList<Constraint>();
		for(Vehicle vehicle: vehicles) 
			this.constraints.add(new CapacityConstraint(vehicle));
		for (PTask pickup: this.pickups) {
			for (PTask delivery: this.deliveries)
				if (pickup.getID() == delivery.getID()) {
					this.constraints.add(new SameVehicle(pickup, delivery));
					this.constraints.add(new OrderConstraint(pickup, delivery));
					break;
				}
		}
		System.out.println("Done !");
		
		System.out.println("Building first Guess");
		Solution guess = new Solution();
		List<Solution> neighbours = new ArrayList<Solution>();
		guess.firstGuess(vehicles, this.pickups, this.deliveries);
		System.out.println("Done !");
		
		System.out.println("Building plan. Time allowed = " + (this.timeout_plan/60000));
		long time_start = System.currentTimeMillis();
		int iteration = 0;
		while(System.currentTimeMillis()-time_start <= this.timeout_plan) {
			System.out.println("Time" + (System.currentTimeMillis()-time_start));
			System.out.println("Iteration: " + iteration);
			System.out.println("Building Neighbours");
			neighbours = guess.neighbours(constraints, this.randomGenerator, 0.1);
			//System.out.println(neighbours);
			System.out.println("Done !");
			System.out.println("Choosing Guess");
			guess = this.localChoice(guess, neighbours, this.probability);
			//System.out.println(guess);
			System.out.println("Done !");
			iteration++;
		}
		
		for (Vehicle v: vehicles) {
			if (guess.getNextTask_v().get(v) == null){
				plans.add(Plan.EMPTY);
			}
			else {
				PTask currentTask = guess.getNextTask_v().get(v);
				Plan planV = new Plan(v.getCurrentCity());
				City previous = v.getCurrentCity();
				while (currentTask != null) {
					for (Task temp: tasks) {
						if (temp.id == currentTask.getID()) {
							for (City city: previous.pathTo(currentTask.getCity()))
								planV.appendMove(city);
							if (currentTask.getPickup())
								planV.appendPickup(temp);
							else
								planV.appendDelivery(temp);
							previous = currentTask.getCity();
							currentTask = guess.getNextTask_t().get(currentTask);
							break;
						}
					}
				}
				plans.add(planV);
			}
		}
			
			
		System.out.println("Done !");
		
		return plans;
	}
	
	private List<PTask> convertPickup(TaskSet tasks){
		List<PTask> pTasks = new ArrayList<PTask>();
		for (Task task: tasks) {
			pTasks.add(new PTask(task.id, task.pickupCity, true, task.weight));
		}
		return pTasks;
	}	
	
	private List<PTask> convertDeliveries(TaskSet tasks){
		List<PTask> deliveries = new ArrayList<PTask>();
		for (Task task: tasks)
			deliveries.add(new PTask(task.id, task.deliveryCity, false, task.weight));
		return deliveries;
	}
							
	private Solution localChoice(Solution currentGuess, List<Solution> currentNeighbours, double probability) {
		Collections.shuffle(currentNeighbours);
		//System.out.println(currentNeighbours);
		Solution min = Collections.min(currentNeighbours);
		//for (Solution sol: currentNeighbours)
			//System.out.println(sol.cost());
		System.out.println("Chosen: " + currentGuess.toString() + min.cost());
		
		if (this.randomGenerator.nextDouble() <= this.probability) {
			return currentNeighbours.get(0);
		}
		else {
			return min;
		}
	}
	

}


