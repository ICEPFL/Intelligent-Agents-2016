package main;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import logist.LogistSettings;
import logist.agent.Agent;
import logist.behavior.CentralizedBehavior;
import logist.config.Parsers;
import logist.plan.Plan;
import logist.simulation.Vehicle;
import logist.task.TaskDistribution;
import logist.task.TaskSet;
import logist.topology.Topology;
import logist.topology.Topology.City;
import mains.Delivery;
import mains.Pickup;
import mains.Searcher;
import mains.Solution;
import mains.Step;

@SuppressWarnings("unused")
public class CentralizedMain implements CentralizedBehavior{

	private Topology topology;
    private TaskDistribution distribution;
    private Agent agent;
    private long timeout_setup;
    private long timeout_plan;
    
	@Override
	public void setup(Topology topology, TaskDistribution distribution, Agent agent) {
		
		// this code is used to get the timeouts
        LogistSettings ls = null;
        try {
            ls = Parsers.parseSettings("config\\settings_default.xml");
        }
        catch (Exception exc) {
            System.out.println("There was a problem loading the configuration file.");
        }
        
        // the setup method cannot last more than timeout_setup milliseconds
        timeout_setup = ls.get(LogistSettings.TimeoutKey.SETUP);
        
        // the plan method cannot execute more than timeout_plan milliseconds
        timeout_plan = ls.get(LogistSettings.TimeoutKey.PLAN);
        
        this.topology = topology;
        this.distribution = distribution;
        this.agent = agent;
	}

	@Override
	public List<Plan> plan(List<Vehicle> vehicles, TaskSet tasks) {
		
		System.out.println("Total number of veicles " + vehicles.size());
		
		Searcher searcher = new Searcher(vehicles, tasks);
		
		long startTime = System.currentTimeMillis();
		
		searcher.searchSolution();
		
		long duration = System.currentTimeMillis() - startTime;
		
		System.out.println("Search time "+duration+" ms");
		System.out.println("Final cost "+searcher.getBestCost());
		
		HashMap<Integer, LinkedList<Step>> stepMap = searcher.getBestStepMap();
		List<Plan> plans = new ArrayList<Plan> ();
		
		for(Vehicle vehicle: vehicles){
			LinkedList<Step> steps = stepMap.get(vehicle.id());
			
			City currCity = vehicle.getCurrentCity();
			Plan plan = new Plan(currCity);
			
			if(steps.size()==0){
				plans.add(Plan.EMPTY);
				continue;
			}
			
			System.out.println(vehicle.name() + " Task Size " + steps.size()/2);
			System.out.println("The current city is " + currCity.name);
			
			for(Step step: steps){
				if(step instanceof Pickup){
					
					if(step.getTask().pickupCity.equals(currCity)){
						
						System.out.println("Pickup task " + step.getTask().id + " at " + step.getTask().pickupCity.name);
						plan.appendPickup(step.getTask());
						continue;
					}
					
					for (City city : currCity.pathTo(step.getTask().pickupCity)){
						System.out.println("Move to " + city.name);
		                plan.appendMove(city);
		            }
					
					System.out.println("Pickup task " + step.getTask().id + " at " + step.getTask().pickupCity.name);
					currCity = step.getTask().pickupCity;
					plan.appendPickup(step.getTask());
					
				} else if(step instanceof Delivery){
					
					if(step.getTask().deliveryCity.equals(currCity)){
						
						System.out.println("Delivery task " + step.getTask().id + " at " + step.getTask().deliveryCity.name);
						plan.appendDelivery(step.getTask());
						continue;
					}
					
					for (City city : currCity.pathTo(step.getTask().deliveryCity)){
						System.out.println("Move to " + city.name);
		                plan.appendMove(city);
		            }
					
					System.out.println("Delivery task " + step.getTask().id + " at " + step.getTask().deliveryCity.name);
					currCity = step.getTask().deliveryCity;
					plan.appendDelivery(step.getTask());
				}
				
			}
			
			plans.add(plan);
		}
		
		return plans;
	}

}
