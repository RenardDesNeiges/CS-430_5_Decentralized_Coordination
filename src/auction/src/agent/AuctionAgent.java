package agent;

//the list of imports
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import javax.swing.plaf.synth.SynthMenuBarUI;
import javax.swing.text.StyledEditorKit;

import java.io.File;
import java.util.Collections;

import java.util.HashSet;

import logist.config.Parsers;
import logist.Measures;
import logist.behavior.AuctionBehavior;
import logist.agent.Agent;
import logist.simulation.Vehicle;
import logist.plan.Plan;
import logist.task.Task;
import logist.task.TaskDistribution;
import logist.task.TaskSet;
import logist.topology.Topology;
import logist.topology.Topology.City;
import logist.LogistSettings;
import logist.task.TaskSet;

import agent.AuctionController;
import planner.Constraint;
import planner.CapacityConstraint;
import planner.OrderConstraint;
import planner.SameVehicle;
import planner.PTask;
import planner.Solution;


/**
 * A very simple auction agent that assigns all tasks to its first vehicle and
 * handles them sequentially.
 * 
 */
@SuppressWarnings("unused")
public class AuctionAgent implements AuctionBehavior {

	private Topology topology;
	private TaskDistribution distribution;
	private Agent agent;
	private long timeout_setup;
	private long timeout_plan;
	private long timeout_bid;
	private double probability;
	private Random randomGenerator;
	private List<PTask> pickups;
	private List<PTask> deliveries;
	private List<Constraint> constraints;
	private AuctionController auctionController;
	private List<Plan> currentPlans;
	private List<Plan> prevPlans;
	private List<Task> currentTasks;
	private List<Task> prevTasks;

	@Override
	public void setup(Topology topology, TaskDistribution distribution, Agent agent) {

		// this code is used to get the timeouts
         //LogistSettings ls = null;
        // try {
        //     ls = Parsers.parseSettings("config" + File.separator + "settings_default.xml");
        // }
        // catch (Exception exc) {
        //     System.out.println("There was a problem loading the configuration file.");
        // }
        
        // the setup method cannot last more than timeout_setup milliseconds
        this.timeout_setup = 30000;//ls.get(LogistSettings.TimeoutKey.SETUP);
        // the plan method cannot execute more than timeout_plan milliseconds
        this.timeout_plan = 30000;// ls.get(LogistSettings.TimeoutKey.PLAN);
        // the bid method cannot execute more than timeout_bid milliseconds
        this.timeout_bid = 30000;// ls.get(LogistSettings.TimeoutKey.BID);
        
        this.topology = topology;
        this.distribution = distribution;
		this.agent = agent;
		
		this.auctionController = new AuctionController(this.topology,this.agent.id());
        
        long seed = -9019554669489983951L * this.agent.hashCode() * agent.id();
		this.randomGenerator = new Random(seed);
		
		this.currentPlans = new ArrayList<Plan>();
		this.prevPlans = new ArrayList<Plan>();
		for (Vehicle v: this.agent.vehicles()) {
			this.currentPlans.add(Plan.EMPTY);
			this.prevPlans.add(Plan.EMPTY);
		}
		Task[] empty = {};
		this.currentTasks = new ArrayList<Task>();
		this.prevTasks = new ArrayList<Task>();
	}

	@Override
	/* handles auction results, if an auction is won, transfers it to our control algorithm */
	public void auctionResult(Task previous, int winner, Long[] bids) {
		
		if (winner != this.agent.id()) {
			System.out.println("lost...");
			this.currentPlans = this.prevPlans;
			this.currentTasks = this.prevTasks;
		}
		else{
			System.out.println("WON!");
		}
		this.auctionController.updateBidHistory(winner, bids, previous);
		
	}
	
	public double costPlans(List<Plan> plans) {
		double res = 0.0;
		for (int i = 0; i< this.agent.vehicles().size(); i++) {
			res += this.agent.vehicles().get(i).costPerKm()*plans.get(i).totalDistance();
		}
		return res;
	}

	
	@Override
	/* method that is called when an auction is thrown */
	public Long askPrice(Task task) {
		this.prevTasks = new ArrayList<Task>();
		for (Task t: this.currentTasks)
			this.prevTasks.add(t);
		this.currentTasks.add(task);
		
		this.prevPlans = this.currentPlans;
		this.currentPlans = this.planify(this.agent.vehicles(), this.currentTasks);
		
		
		double marginalCost = this.costPlans(this.currentPlans) - this.costPlans(this.prevPlans);
		return this.auctionController.returnPrice(task, marginalCost);
		
	}

	/* handles the creation of the delivery plan (this is where we should call centralized) */
	public List<Plan> planify(List<Vehicle> vehicles, List<Task> tasks) {
		List<Plan> plans = new ArrayList<Plan>();
		this.pickups = convertPickup(tasks);
		this.deliveries = convertDeliveries(tasks);
		
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
		Solution guess = new Solution();
		List<Solution> neighbours = new ArrayList<Solution>();
		guess.firstGuess(vehicles, this.pickups, this.deliveries);
		
		int iterations = 0;
		long time_start = System.currentTimeMillis();
		while(System.currentTimeMillis()-time_start <= this.timeout_bid-this.timeout_bid/10.0) {
			neighbours = guess.neighbours(this.constraints, this.randomGenerator, 0.1);
			guess = this.localChoice(guess, neighbours, this.probability);
			iterations++;
		}

		for (Vehicle v: vehicles) {
			if (guess.getNextTask_v().get(v) == null)
				plans.add(Plan.EMPTY);
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
			
		return plans;
	}
	
	
	
	private List<PTask> convertPickup(List<Task> tasks){
		List<PTask> pTasks = new ArrayList<PTask>();
		for (Task task: tasks) {
			pTasks.add(new PTask(task.id, task.pickupCity, true, task.weight));
		}
		return pTasks;
	}	
	
	private List<PTask> convertDeliveries(List<Task> tasks){
		List<PTask> deliveries = new ArrayList<PTask>();
		for (Task task: tasks)
			deliveries.add(new PTask(task.id, task.deliveryCity, false, task.weight));
		return deliveries;
	}
							
	private Solution localChoice(Solution currentGuess, List<Solution> currentNeighbours, double probability) {
		Collections.shuffle(currentNeighbours);
		Solution min = Collections.min(currentNeighbours);

		if (this.randomGenerator.nextDouble() <= this.probability) {
			return currentNeighbours.get(0);
		}
		else {
			return min;
		}
	}
	
	public List<Plan> plan(List<Vehicle> vehicle, TaskSet tasks){
		// System.out.print(this.currentPlans);
		// for(Plan p : this.currentPlans){
		// 	System.out.println(p);
		// 	System.out.println();
		// }
		// System.out.println(tasks);
		ArrayList<Task> tl = new ArrayList<Task>();
		for(Task t : tasks){
			tl.add(t);
		}
		return this.planify(vehicle, tl);
	}
}
