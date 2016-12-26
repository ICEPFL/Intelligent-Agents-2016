package utils;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import helpers.Step;

import logist.simulation.Vehicle;
import logist.task.Task;
import logist.topology.Topology.City;

public class Bidder {
	
	private Integer id;
	
	/*
	 * vehicles owned by the bidder, and all initial locations of
	 * these vehicles
	 */
	private List<EnVehicle> vehicles;
	private List<City> homeCities;
	
	private LinkedList<Task> tasks;
	
	/*
	 * the current delivery plan
	 */
	private HashMap<Integer, LinkedList<Step>> stepMap;
	
	/*
	 * the basic information of vehicle
	 */
	private double cost;
	private int minCostPerKm = Integer.MAX_VALUE;
	private int maxCapacity = 0;
	
	private double totalBid;
	
	/*
	 * the plan if the bidder win the next task
	 */
	private Result nextStep;
	
	public Bidder(int id, List<Vehicle> vehicles){
		this.id = id;
		this.vehicles = EnVehicle.transfer(vehicles);
		
		this.homeCities = new LinkedList<City> ();
		
		for(EnVehicle vehicle: this.vehicles){
			homeCities.add(vehicle.getHomeCity());
			
			if(vehicle.getCapacity()>maxCapacity)
				maxCapacity = vehicle.getCapacity();
			
			if(vehicle.getCostPerKm()<minCostPerKm)
				minCostPerKm = vehicle.getCostPerKm();
			
		}
		
		this.tasks = new LinkedList<Task> ();
		this.stepMap = new HashMap<Integer, LinkedList<Step>> ();
		
		this.cost = 0;
		
		this.totalBid = 0;
		
	}
	
	public Bidder(Integer id, List<City> homeCities, int capacity, int costPerKm){
		this.id = id;
		this.vehicles = EnVehicle.create(homeCities, capacity, costPerKm);
		
		this.homeCities = homeCities;
		
		this.maxCapacity = capacity;
		this.minCostPerKm = costPerKm;
		
		this.tasks = new LinkedList<Task> ();
		this.stepMap = new HashMap<Integer, LinkedList<Step>> ();
		
		this.cost = 0;
		
		this.totalBid = 0;
	}
	
	public double nextStepCost(Task task) {
		this.tasks.add(task);
		this.nextStep = Searcher.searchSolution(vehicles, tasks);
		
		this.tasks.remove(task);
		
		return nextStep.getCost();
	}
	
	public double nextAvgMarginalCost(List<Task> taskList) {
		this.tasks.addAll(taskList);
		Result result = Searcher.searchSolution(vehicles, tasks);
		
		double avgCost = (result.getCost() - this.cost)/taskList.size();
		this.tasks.removeAll(taskList);
		
		return avgCost;
	}
	
	
	public void obtainTask(Task task, long bid){
		if(!this.tasks.contains(task))
			this.tasks.add(task);
		
		this.cost = nextStep.getCost();
		this.stepMap = nextStep.getStepMap();
		this.totalBid += bid;
	}
	
	public void addVehicleAt(City city){
		this.homeCities.add(city);
		this.vehicles.add(new EnVehicle(this.maxCapacity, this.minCostPerKm, city));
	}
	
	public boolean hasVehicleStartAt(City city){
		return this.homeCities.contains(city);
	}
	
	public Result searchPlan(LinkedList<Task> finalTasks) {
		Result result = Searcher.searchSolution(vehicles, finalTasks);
		
		return result;
	}

	public void updateState() {
		Result result = this.searchPlan(tasks);
		this.cost = result.getCost();
		this.stepMap = result.getStepMap();
	}
	
	public double getBidThreshold() {
		return this.nextStep.getCost()-this.totalBid;
	}
	
	public int getNumberOfVehicles(){
		return this.vehicles.size();
	}

	public List<City> getHomeCities() {
		return homeCities;
	}

	public HashMap<Integer, LinkedList<Step>> getStepMap() {
		return stepMap;
	}

	public int getId() {
		return id;
	}

	public int getMinCostPerKm() {
		return minCostPerKm;
	}

	public int getMaxCapacity() {
		return maxCapacity;
	}

	public double getCost() {
		return cost;
	}

	public double getTotalBid() {
		return totalBid;
	}
	
	public int getNumberOfTasks(){
		return this.tasks.size();
	}

}
