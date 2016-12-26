package utils;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import helpers.Delivery;
import helpers.OrderChanger;
import helpers.Pickup;
import helpers.Step;
import helpers.VehiChanger;
import logist.task.Task;
import logist.topology.Topology.City;

public class Solution {
	
	private List<EnVehicle> vehicles;
	private List<Task> tasks;
	 
	private HashMap<Integer, LinkedList<Step>> stepMap;
	private LinkedList<EnVehicle> vehiclesWithTasks;
	
	private int solutionCost;
	
	public Solution(List<EnVehicle> vehicles, List<Task> tasks){
		this.vehicles = vehicles;
		this.tasks = tasks;
		
		this.stepMap = new HashMap<Integer, LinkedList<Step>>();
		this.vehiclesWithTasks = new LinkedList<EnVehicle> ();
		
		if(vehicles.size()>0)
			this.initialSolution();
		
		this.solutionCost = this.costToDeliveryAll();
		
	}
	
	/**
	 * update the current solution by the best neighbor
	 */
	public void updateSolutionBylocalChoice(){
		
		// choice a better solution by exchanging tasks between vehicles
		VehiChanger exchanger = updateSolutionByChangeVehicle();
		
		// choice a better solution by changing the order of task
		OrderChanger changer = updateSolutionByChangeOrder();
		
		if((exchanger!=null)&&(changer!=null)){
			
			if(exchanger.getCost()<=changer.getCost()){
				
				changeVehicle(exchanger);
				
			} else {
				
				changeOrder(changer);
			}
			
		} else if(exchanger!=null){
			
			changeVehicle(exchanger);
			
		} else if(changer!=null){
			
			changeOrder(changer);
			
		} 
		
	}

	/**
	 * update the current solution by a random neighbor
	 */
	public void updateSolutionByRandomChoice() {
		
		int choice = Math.random() > 0.5? 1:0;
		
		switch(choice){
		case 0: updateSolutionByRandomChangeVehicle(); 
				break;
		case 1: updateSolutionByRandomChangeOrder();
				break;
		default:
				System.out.println("Unsupport choice");
		
		}
		
	}

	private void updateSolutionByRandomChangeOrder() {
		
		if(this.vehiclesWithTasks.size()<1)
			return;
		
		EnVehicle vehicle = this.vehiclesWithTasks.get((int)(Math.random()*this.vehiclesWithTasks.size()));
		LinkedList<Step> steps = this.stepMap.get(vehicle.getId());
		
		if(steps.size()<=2)
			return;
		
		LinkedList<Integer> pickIndices = new LinkedList<Integer> ();
		for(int i=0; i<steps.size(); i++){
			if(steps.get(i) instanceof Pickup)
				pickIndices.add(i);
		}
		
		int pickupIndex = pickIndices.get((int)(Math.random()*pickIndices.size()));
		
		Step pickupFrom = steps.get(pickupIndex);
		Step deliveryFrom = ((Pickup)pickupFrom).getSibling();
		
		int deliveryIndex = steps.indexOf(deliveryFrom);
		
		boolean valid = false;
		
		while(!valid){
			
			int pickupToIndex = pickIndices.get((int)(Math.random()*pickIndices.size()));
			
			Step pickupTo = steps.get(pickupToIndex);
			Step deliveryTo = ((Pickup)pickupTo).getSibling();
			
			int deliveryToIndex = steps.indexOf(deliveryTo);
			
			steps.set(pickupToIndex, pickupFrom);
			steps.set(deliveryToIndex, deliveryFrom);
			steps.set(pickupIndex, pickupTo);
			steps.set(deliveryIndex, deliveryTo);
			
			if(this.isValidStepSequence(vehicle, steps, Math.max(deliveryIndex, deliveryToIndex)))
				valid = true;
			else{
				steps.set(pickupToIndex, pickupTo);
				steps.set(deliveryToIndex, deliveryTo);
			}
			
		}
		
		this.solutionCost = this.costToDeliveryAll();
		
	}

	private void updateSolutionByRandomChangeVehicle() {
		
		if(this.vehiclesWithTasks.size()<1)
			return;
		
		EnVehicle vehicleFrom = this.vehiclesWithTasks.get((int)(Math.random()*this.vehiclesWithTasks.size()));
		EnVehicle vehicleTo = this.vehicles.get((int)(Math.random()*this.vehicles.size()));
		
		LinkedList<Step> stepFrom = this.stepMap.get(vehicleFrom.getId());
		LinkedList<Step> stepTo = this.stepMap.get(vehicleTo.getId());
		
		LinkedList<Integer> pickIndices = new LinkedList<Integer> ();
		for(int i=0; i<stepFrom.size(); i++){
			if(stepFrom.get(i) instanceof Pickup)
				pickIndices.add(i);
		}
		
		Step pickupFrom = stepFrom.get(pickIndices.get((int)(Math.random()*pickIndices.size())));
		Step deliveryFrom = ((Pickup)pickupFrom).getSibling();
		
		stepFrom.remove(pickupFrom);
		stepFrom.remove(deliveryFrom);
		
		boolean valid = false;
		
		while(!valid){
			int pickupToIndex = (int)(Math.random()*stepTo.size());
			stepTo.add(pickupToIndex, pickupFrom);
			
			int deliveryToIndex = (int)(Math.random()*(stepTo.size()-pickupToIndex))+pickupToIndex+1;
			stepTo.add(deliveryToIndex, deliveryFrom);
			
			if(this.isValidStepSequence(vehicleTo, stepTo, deliveryToIndex))
				valid = true;
			else{
				stepTo.remove(deliveryToIndex);
				stepTo.remove(pickupToIndex);
			}
		}
		
		this.solutionCost = this.costToDeliveryAll();
		
		if(stepFrom.size()==0)
			this.vehiclesWithTasks.remove(vehicleFrom);
		
		if(!this.vehiclesWithTasks.contains(vehicleTo))
			this.vehiclesWithTasks.add(vehicleTo);
		
	}

	/**
	 * changer the order of tasks assigned to a vehicle
	 * @param changer
	 */
	private void changeOrder(OrderChanger changer) {
		
		LinkedList<Step> steps = this.stepMap.get(changer.getVehicle().getId());
		
		Step pickupFrom = steps.get(changer.getPickupFrom());
		Step deliveryFrom = steps.get(changer.getDeliveryFrom());
		
		Step pickupTo = steps.get(changer.getPickupTo());
		Step deliveryTo = steps.get(changer.getDeliveryTo());
		
		steps.set(changer.getPickupFrom(), pickupTo);
		steps.set(changer.getDeliveryFrom(), deliveryTo);
		
		steps.set(changer.getPickupTo(), pickupFrom);
		steps.set(changer.getDeliveryTo(), deliveryFrom);
		
		this.solutionCost = changer.getCost();
	}

	/**
	 * exchange tasks between two vehicles
	 * @param exchanger
	 */
	private void changeVehicle(VehiChanger changer) {
		
		LinkedList<Step> fromSteps = this.stepMap.get(changer.getVehicleFrom().getId());
		LinkedList<Step> toSteps = this.stepMap.get(changer.getVehicleTo().getId());
		
		Step pickupFrom = fromSteps.get(changer.getPickupFrom());
		Step deliveryFrom = ((Pickup)pickupFrom).getSibling();
		
		fromSteps.remove(pickupFrom);
		fromSteps.remove(deliveryFrom);
		
		toSteps.add(changer.getPickupTo(), pickupFrom);
		toSteps.add(changer.getDeliveryTo(), deliveryFrom);
		
		this.solutionCost = changer.getCost();
		
		// update the vehicles which have tasks
		if(this.stepMap.get(changer.getVehicleFrom().getId()).size()==0)
			this.vehiclesWithTasks.remove(changer.getVehicleFrom());
		
		if(!this.vehiclesWithTasks.contains(changer.getVehicleTo()))
			this.vehiclesWithTasks.add(changer.getVehicleTo());
	}

	/**
	 * find a better solution by changing the order of tasks
	 * @return
	 */
	private OrderChanger updateSolutionByChangeOrder() {
		int currCost = this.solutionCost;
		
		if(this.vehiclesWithTasks.size()<1)
			return null;
		
		// choose a vehicle randomly
		EnVehicle selectedEnVehicle= this.vehiclesWithTasks.get((int)(Math.random()*this.vehiclesWithTasks.size()));
		
		LinkedList<Step> steps = this.stepMap.get(selectedEnVehicle.getId());
		
		LinkedList<Integer> pickIndices = new LinkedList<Integer> ();
		for(int i=0; i<steps.size(); i++){
			if(steps.get(i) instanceof Pickup)
				pickIndices.add(i);
		}
		
		if(pickIndices.size()<2)
			return null;
		
		int pickupIndex = pickIndices.get((int)(Math.random()*pickIndices.size()));
		
		Step pickupStep = steps.get(pickupIndex);
		Step deliveryStep = ((Pickup)pickupStep).getSibling();
		
		int deliveryIndex = steps.indexOf(deliveryStep);
		
		
		pickIndices.remove(new Integer(pickupIndex));
		
		int optPickupTo = -1;
		int optDeliveryTo = -1;
		
		for(Integer pickupToIndex: pickIndices){
			
			Step pickupToStep = steps.get(pickupToIndex);
			Step deliveryToStep = ((Pickup)pickupToStep).getSibling();
			
			int deliveryToIndex = steps.indexOf(deliveryToStep);
			
			steps.set(pickupToIndex, pickupStep);
			steps.set(deliveryToIndex, deliveryStep);
			steps.set(pickupIndex, pickupToStep);
			steps.set(deliveryIndex, deliveryToStep);
			
			if(!this.isValidStepSequence(selectedEnVehicle, steps, Math.max(deliveryIndex, deliveryToIndex))){
				steps.set(pickupToIndex, pickupToStep);
				steps.set(deliveryToIndex, deliveryToStep);
				
				continue;
			}
			
			int cost = this.costToDeliveryAll();
			
			if(cost<currCost){
				currCost = cost;
				optPickupTo = pickupToIndex;
				optDeliveryTo = deliveryToIndex;
			}
			
			steps.set(pickupToIndex, pickupToStep);
			steps.set(deliveryToIndex, deliveryToStep);
		}
		
		steps.set(pickupIndex, pickupStep);
		steps.set(deliveryIndex, deliveryStep);
		
		if(optPickupTo<0)
			return null;
	
		return new OrderChanger(selectedEnVehicle, pickupIndex, deliveryIndex, optPickupTo, optDeliveryTo, currCost);
	}

	/**
	 * find a better solution by assigning a task to another vehicle
	 * @return
	 */
	private VehiChanger updateSolutionByChangeVehicle() {
		int currCost = this.solutionCost;
		
		if(this.vehiclesWithTasks.size()<1)
			return null;
		
		// choose a vehicle randomly
		EnVehicle selectedEnVehicle= this.vehiclesWithTasks.get((int)(Math.random()*this.vehiclesWithTasks.size()));
		
		LinkedList<Step> steps = this.stepMap.get(selectedEnVehicle.getId());
		
		LinkedList<Integer> pickIndices = new LinkedList<Integer> ();
		for(int i=0; i<steps.size(); i++){
			if(steps.get(i) instanceof Pickup)
				pickIndices.add(i);
		}
		
		int pickupIndex = pickIndices.get((int)(Math.random()*pickIndices.size()));
		
		Step pickupStep = steps.get(pickupIndex);
		Step deliveryStep = ((Pickup)pickupStep).getSibling();
		
		int deliveryIndex = steps.indexOf(deliveryStep);
		
		steps.remove(deliveryIndex);
		steps.remove(pickupIndex);
		
		EnVehicle optEnVehicle= null;
		int optPickIndex = 1;
		int optDeliIndex = 1;
		
		for(EnVehicle vehicle: this.vehicles){
		
			
			LinkedList<Step> vSteps = this.stepMap.get(vehicle.getId());
			
			int size = vSteps.size();
			for(int i=0; i<size+1; i++){
				vSteps.add(i, pickupStep);
				
				for(int j=i+1; j<size+2; j++){
					vSteps.add(j, deliveryStep);
					
					if(!this.isValidStepSequence(vehicle, vSteps, j)){
						vSteps.remove(j);
						continue;
					}
					
					int cost = this.costToDeliveryAll();
					if(cost < currCost){
						currCost = cost;
						optEnVehicle= vehicle;
						optPickIndex = i;
						optDeliIndex = j;
					}
					
					vSteps.remove(j);
				}
				
				vSteps.remove(i);
			}
			
		}
		
		steps.add(pickupIndex, pickupStep);
		steps.add(deliveryIndex, deliveryStep);
		
		if(optEnVehicle==null)
			return null;
		
		return new VehiChanger(selectedEnVehicle, optEnVehicle, pickupIndex, optPickIndex, optDeliIndex, currCost);
		
	}

	/**
	 * test whether 1 ~ end steps is valid, which means during these steps, the
	 * vehicle can load corresponding tasks
	 * @param vehicle
	 * @param vSteps
	 * @param end
	 * @return
	 */
	private boolean isValidStepSequence(EnVehicle vehicle, LinkedList<Step> vSteps, int end) {
		int capacity = vehicle.getCapacity();
		int load = 0;
		
		for(int i=0; i<end; i++){
			if(vSteps.get(i) instanceof Pickup){
				load += vSteps.get(i).getTask().weight;
				
				if(load > capacity)
					return false;
				
			} else {
				load -= vSteps.get(i).getTask().weight;
			}
		}
		
		return true;
	}

	public int costToDeliveryAll() {
		List<EnVehicle> vehicles = this.getEnVehicles();
		int cost = 0;
		
		// for each vehicle, compute the traveled distance
		for(EnVehicle vehicle: vehicles){
			City currCity = vehicle.getHomeCity();
			LinkedList<Step> steps = this.getStepMap().get(vehicle.getId());
			
			int costPerKm = vehicle.getCostPerKm();
			
			// take the step one by one
			for(Step step: steps){
				if(step instanceof Pickup){
					cost += currCity.distanceTo(step.getTask().pickupCity)*costPerKm;
					currCity = step.getTask().pickupCity;
				} else {
					cost += currCity.distanceTo(step.getTask().deliveryCity)*costPerKm;
					currCity = step.getTask().deliveryCity;
				}
				
			}
					
		}
		
		return cost;
	}

	/**
	 * assign all tasks to the first vehicle, the vehicle pickup a task, and then 
	 * delivery this task, until all the tasks are delivered.
	 */
	private void initialSolution() {
		
		for(EnVehicle vehicle: this.vehicles)
			stepMap.put(vehicle.getId(), new LinkedList<Step>());
		
		int vLen = this.vehicles.size();
		
		for(Task task: this.tasks){
			
			int vIndex = task.id % vLen;
			
			/* in current configuration, the weight of task is a constant 3,
			 * and the capacity of vehicle is 30, so the following statement 
			 * always return true
			 */
			// initialize the pickup step
			EnVehicle vehicle = this.vehicles.get(vIndex);
			
			Pickup pickup = new Pickup(task);
				
			// initialize the delivery step
			Delivery delivery = new Delivery(task);
				
			pickup.setSibling(delivery);
			delivery.setSibling(pickup);
				
			stepMap.get(vehicle.getId()).add(pickup);
			stepMap.get(vehicle.getId()).add(delivery);
			
		}
		
		for(EnVehicle vehicle: this.vehicles){
			if(!(stepMap.get(vehicle.getId()).size()==0))
				this.vehiclesWithTasks.add(vehicle);
		}
		
	}

	public int getSolutionCost() {
		return solutionCost;
	}

	public void setSolutionCost(int solutionCost) {
		this.solutionCost = solutionCost;
	}

	public List<EnVehicle> getEnVehicles() {
		return vehicles;
	}

	public void setEnVehicles(List<EnVehicle> vehicles) {
		this.vehicles = vehicles;
	}

	public HashMap<Integer, LinkedList<Step>> getStepMap() {
		return stepMap;
	}

	public void setStepMap(HashMap<Integer, LinkedList<Step>> stepMap) {
		this.stepMap = stepMap;
	}

}
