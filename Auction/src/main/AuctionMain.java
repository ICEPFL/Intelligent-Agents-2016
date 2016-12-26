package main;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import helpers.CityEstimater;
import helpers.Delivery;
import helpers.Pickup;
import helpers.Step;
import logist.behavior.AuctionBehavior;
import logist.agent.Agent;
import logist.simulation.Vehicle;
import logist.plan.Plan;
import logist.task.Task;
import logist.task.TaskDistribution;
import logist.task.TaskSet;
import logist.topology.Topology;
import logist.topology.Topology.City;
import utils.Bidder;
import utils.Result;

@SuppressWarnings("unused")
public class AuctionMain implements AuctionBehavior {

	private Topology topology;
	private TaskDistribution distribution;
	private Agent agent;
	private List<City> cities;
	
	/*
	 * bidder myself and map<agentId, Bidder> saves all bidders
	 */
	private Bidder bidderSelf;
	private Map<Integer, Bidder> bidderMap;
	
	/*
	 * marginal cost myself and map<agentId, marginalCost> saves all marginal cost
	 */
	private double marginSelf;
	private Map<Integer, Double> marginCostMap;
	
	/*
	 * map<agentId, ratio> saves all ratios of bidders, use ratio * marginalCost
	 * to estimate opponent's bid 
	 */
	private Map<Integer, Double> ratioMap;
	
	private double defaultRatio = 1;
	private final double RATIO_UPPER = 1.5;
	private final double RATIO_LOWER = 0.8;
	private final int RANDOM_SIZE = 4;
	
	/*
	 * current round number
	 */
	private int round = 0;
	
	private CityEstimater estimater;
	
	@Override
	public void setup(Topology topology, TaskDistribution distribution,
			Agent agent) {

		this.topology = topology;
		this.distribution = distribution;
		this.agent = agent;
		this.cities = topology.cities();
		
		this.estimater = new CityEstimater(this.cities);
		
		// initialize myself
		this.bidderSelf = new Bidder(agent.id(), agent.vehicles());
		
		// bidder map just contains myself at the beginning 
		this.bidderMap = new HashMap<Integer, Bidder> ();
		this.bidderMap.put(agent.id(), bidderSelf);
		
		this.ratioMap = new HashMap<Integer, Double>();
		this.marginCostMap = new HashMap<Integer, Double> ();
	
	}

	@Override
	public Long askPrice(Task task) {
		
		if(task.weight>bidderSelf.getMaxCapacity())
			return null;

		/*
		 * in the first round, we don't have opponents' info, we just use our
		 * marginal cost and the minimum cost of delivering a task to estimate bid
		 */
		if(this.round==0){
			
			marginSelf = bidderSelf.nextStepCost(task) - bidderSelf.getCost();
			double taskCost = task.pathLength() * bidderSelf.getMinCostPerKm();
			round += 1;
			
			// System.out.println("Bid in this round " + round + " " +(long) Math.min(marginSelf, taskCost*0.8));
			
			return  (long) Math.min(marginSelf, taskCost*0.8);
			
		}
		
		/*
		 * after the first round, we use the info of opponents to estimate marginal cost
		 */
		double bidSelf = 0;
		double bidAnte = Double.MAX_VALUE;
		
		for(Bidder bidder: bidderMap.values()){
			
			// calculate marginal cost for each bidder
			double marginCost = bidder.nextStepCost(task) - bidder.getCost();
			marginCostMap.put(bidder.getId(), marginCost);
			
			double avgCost = bidder.nextAvgMarginalCost(generateRandomTasks(task.weight, bidder.getNumberOfTasks()));
			
			double alpha = Math.pow(Math.E, -round*0.1);
			
			if(bidder.getId()==agent.id())
				bidSelf = (marginCost*(1-alpha)+avgCost*alpha) * ratioMap.get(bidder.getId());
			else{
				double bidTemp = (marginCost*(1-alpha)+avgCost*alpha) * ratioMap.get(bidder.getId());
				
				// save the minimum bid of opponent bidder
				if(bidTemp<bidAnte)
					bidAnte = bidTemp;
			}
			
		}
		
		bidSelf = Math.min(bidSelf, bidAnte*1.2);
		
		/*
		 * at the beginning rounds, we make our bid a little smaller in order
		 * to get more tasks
		 */
		bidSelf *= 1.2/(1+Math.exp(-round*0.1));
		
		/*
		 * if marginal cost is zero, we use the minimum cost of delivering a
		 * task as our bid
		 */
		if(bidSelf<=10)
			bidSelf = task.pathLength() * bidderSelf.getMinCostPerKm() * 1;
		
		bidSelf = Math.max(bidSelf, bidderSelf.getBidThreshold()*0.25);
		
		round += 1;
		// System.out.println("Bid in this round " + round + " " + bidSelf);
		
		System.out.println("Round " + round + " Self id " + bidderSelf.getId());
		for(Integer bidderId: marginCostMap.keySet())
			System.out.printf("%d: %.6f\t", bidderId, marginCostMap.get(bidderId));
		
		System.out.println();
		
		return (long)bidSelf;
		
	}

	@Override
	public void auctionResult(Task previous, int winner, Long[] bids) {
		
		int agentLen = bids.length;
		
		for(int i=0; i<agentLen; i++)
			System.out.printf("%d: %.6f\t", i, bids[i].doubleValue());
		System.out.println();
		
		/*
		 * just after the first round, we use opponents' bids to estimate and construct
		 * these opponents
		 */
		if(this.round==1){
			
			for(int i=0; i<agentLen; i++){
				if(i!=agent.id()){
					
					// try to estimate the initial city of opponent
					// City initialCity = estimateInitialCity(bids[i], previous);
					City initialCity = this.estimater.estimateInitialCity(bids[i], previous, bidderSelf.getMinCostPerKm());
					Bidder bidder = createRandomBidder(i, initialCity, estimater.getUpperCities());
					bidder.nextStepCost(previous);
					
					bidderMap.put(i, bidder);
					
				}
				
				this.ratioMap.put(i, defaultRatio);
			}
			
		} 
		/*
		 * start from second round, we use opponents' bids to refine them
		 */
		else {
			for(int i=0; i<agentLen; i++){
				if((i!=agent.id())&&(bids[i]!=null)){
					
					double bidAnte = bids[i];
					double marginCost = marginCostMap.get(i);
					
					Bidder bidder = bidderMap.get(i);
					
					// if estimated bid is smaller than real bid, increase corresponding ratio
					if(marginCost<bidAnte)
						this.ratioMap.put(i, Math.min(ratioMap.get(i)+0.1, RATIO_UPPER));
					
					// if estimated bid is close to real bid, adjust the ratio
					else if((bidAnte<=marginCost)&&(marginCost*ratioMap.get(i)<bidAnte)){
						
						double average = 0.5 * (marginCost+marginCost*ratioMap.get(i));
						if(bidAnte>average)
							this.ratioMap.put(i, Math.min(ratioMap.get(i)+0.03, RATIO_UPPER));
						else
							this.ratioMap.put(i, Math.max(ratioMap.get(i)-0.03, RATIO_LOWER));
					
					// if estimated bid is bigger than real bid, refine opponent info
					} else {
						if(!bidder.hasVehicleStartAt(previous.pickupCity)){
							
							bidder.addVehicleAt(previous.pickupCity);
							bidder.updateState();
							bidder.nextStepCost(previous);
							
							ratioMap.put(i, RATIO_LOWER);
						} 
						
					}
					
				}
				
			}
			
		}
		
		// if the winner is me, increase my ratio
		if(winner==bidderSelf.getId()){
			bidderSelf.obtainTask(previous, bids[winner]);
			ratioMap.put(agent.id(), Math.min(RATIO_UPPER, ratioMap.get(agent.id())+0.05));
			
		// if the winner is an opponent, decrease its ratio
		} else {
			bidderMap.get(winner).obtainTask(previous, bids[winner]);
			ratioMap.put(agent.id(), Math.max(RATIO_LOWER, ratioMap.get(agent.id())-0.05));
		}
		
		/*
		System.out.println("Self id: "+bidderSelf.getId());
		for(Integer bidderId: this.ratioMap.keySet())
			System.out.printf("id: %d ratio: %.4f\t", bidderId, ratioMap.get(bidderId));
		
		System.out.println();
		*/
	}

	@Override
	public List<Plan> plan(List<Vehicle> vehicles, TaskSet tasks) {
		
		LinkedList<Task> allTasks = new LinkedList<Task> ();
		for(Task task: tasks)
			allTasks.add(task);
		
		// search the final plan
		Result finalPlan = bidderSelf.searchPlan(allTasks);
		HashMap<Integer, LinkedList<Step>> stepMap = finalPlan.getStepMap();
		double finalCost = finalPlan.getCost();
		
		System.out.println("Final profit is " + (bidderSelf.getTotalBid()-finalCost));
		
		List<Plan> plans = new ArrayList<Plan> ();
		
		for(Vehicle vehicle: vehicles){
			LinkedList<Step> steps = stepMap.get(vehicle.id());
			
			City currCity = vehicle.getCurrentCity();
			Plan plan = new Plan(currCity);
			
			if(steps.size()==0){
				plans.add(Plan.EMPTY);
				continue;
			}
			
			for(Step step: steps){
				if(step instanceof Pickup){
					
					if(step.getTask().pickupCity.equals(currCity)){
						
						plan.appendPickup(step.getTask());
						continue;
					}
					
					for (City city : currCity.pathTo(step.getTask().pickupCity)){
		                plan.appendMove(city);
		            }
					
					currCity = step.getTask().pickupCity;
					plan.appendPickup(step.getTask());
					
				} else if(step instanceof Delivery){
					
					if(step.getTask().deliveryCity.equals(currCity)){
						
						plan.appendDelivery(step.getTask());
						continue;
					}
					
					for (City city : currCity.pathTo(step.getTask().deliveryCity)){
		
		                plan.appendMove(city);
		            }
					
					currCity = step.getTask().deliveryCity;
					plan.appendDelivery(step.getTask());
				}
				
			}
			
			plans.add(plan);
		}
		
		return plans;
	}

	/**
	 * randomly create a bidder whose id is agentId and contains a vehicle at initial city
	 * @param agentId
	 * @param initialCity
	 * @return bidder
	 */
	private Bidder createRandomBidder(Integer agentId, City initialCity, List<City> upperCities) {
		// int anteNumbers = 1 + (int)Math.random() * 5;
		int anteNumbers = bidderSelf.getNumberOfVehicles();
		
		List<City> bidderCities = new LinkedList<City> ();
		
		int cityLen = upperCities.size();
		List<Integer> indices = new LinkedList<Integer> ();
		for(int i=0; i<cityLen; i++)
			indices.add(i);
		
		for(int i=0; i<Math.min(anteNumbers,cityLen); i++){
			int index = (int)Math.random()*indices.size();
			bidderCities.add(upperCities.get(indices.get(index)));
			indices.remove(index);
		}
		
		if((initialCity!=null)&&(!bidderCities.contains(initialCity)))
			bidderCities.add(initialCity);
		
		Bidder bidderAnte = new Bidder(agentId, bidderCities, bidderSelf.getMaxCapacity(), bidderSelf.getMinCostPerKm());
		
		return bidderAnte;
	}
	
	/**
	 * find a city from which our cost of delivering task is closest to value 
	 * @param value
	 * @param task
	 * @return City
	 */
	/*private City estimateInitialCity(Long value, Task task) {
		
		if(value==null)
			return null;
		
		City target = null;
		double costDiff = Double.MAX_VALUE;
		
		for(City city: cities){
			double currCost = (city.distanceTo(task.pickupCity)+task.pathLength())*bidderSelf.getMinCostPerKm();
			double currCostDiff = Math.abs(currCost-value);
			
			if(currCostDiff<costDiff){
				costDiff = currCostDiff;
				target = city;
			}
		}
		
		return target;
		
	}*/
	
	/**
	 * generate some randomly created tasks, the first id of these task is startId 
	 * @param weight
	 * @param startId
	 * @return taskList
	 */
	private List<Task> generateRandomTasks(int weight, int startId) {
		List<Task> ranTasks = new LinkedList<Task> ();
		for(int i=0; i<RANDOM_SIZE; i++){
			int srcIndex = (int)Math.random()*this.cities.size();
			int dstIndex = (int)Math.random()*this.cities.size();
		
			ranTasks.add(new Task(startId, cities.get(srcIndex), cities.get(dstIndex), 0, weight));
			startId += 1;
		}
		
		return ranTasks;
	}
	
}
