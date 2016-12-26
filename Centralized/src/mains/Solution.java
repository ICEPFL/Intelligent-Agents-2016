package mains;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import logist.simulation.Vehicle;
import logist.task.Task;
import logist.task.TaskSet;
import logist.topology.Topology.City;
import mains.Step;

public class Solution {
	
	private List<Vehicle> vehicles;
	private TaskSet tasks;
	 
	private HashMap<Integer, LinkedList<Step>> stepMap;
	private LinkedList<Vehicle> vehiclesWithTasks;
	
	private int solutionCost;
	
	public Solution(List<Vehicle> vehicles, TaskSet tasks){
		this.vehicles = vehicles;
		this.tasks = tasks;
		
		this.stepMap = new HashMap<Integer, LinkedList<Step>>();
		this.vehiclesWithTasks = new LinkedList<Vehicle> ();
		
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
		
		Vehicle vehicle = this.vehiclesWithTasks.get((int)(Math.random()*this.vehiclesWithTasks.size()));
		LinkedList<Step> steps = this.stepMap.get(vehicle.id());
		
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
		
		Vehicle vehicleFrom = this.vehiclesWithTasks.get((int)(Math.random()*this.vehiclesWithTasks.size()));
		Vehicle vehicleTo = this.vehicles.get((int)(Math.random()*this.vehicles.size()));
		
		LinkedList<Step> stepFrom = this.stepMap.get(vehicleFrom.id());
		LinkedList<Step> stepTo = this.stepMap.get(vehicleTo.id());
		
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
		
		LinkedList<Step> steps = this.stepMap.get(changer.getVehicle().id());
		
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
		
		LinkedList<Step> fromSteps = this.stepMap.get(changer.getVehicleFrom().id());
		LinkedList<Step> toSteps = this.stepMap.get(changer.getVehicleTo().id());
		
		Step pickupFrom = fromSteps.get(changer.getPickupFrom());
		Step deliveryFrom = ((Pickup)pickupFrom).getSibling();
		
		fromSteps.remove(pickupFrom);
		fromSteps.remove(deliveryFrom);
		
		toSteps.add(changer.getPickupTo(), pickupFrom);
		toSteps.add(changer.getDeliveryTo(), deliveryFrom);
		
		this.solutionCost = changer.getCost();
		
		// update the vehicles which have tasks
		if(this.stepMap.get(changer.getVehicleFrom().id()).size()==0)
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
		
		// choose a vehicle randomly
		Vehicle selectedVehicle = this.vehiclesWithTasks.get((int)(Math.random()*this.vehiclesWithTasks.size()));
		
		LinkedList<Step> steps = this.stepMap.get(selectedVehicle.id());
		
		LinkedList<Integer> pickIndices = new LinkedList<Integer> ();
		for(int i=0; i<steps.size(); i++){
			if(steps.get(i) instanceof Pickup)
				pickIndices.add(i);
		}
		
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
			
			if(!this.isValidStepSequence(selectedVehicle, steps, Math.max(deliveryIndex, deliveryToIndex))){
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
	
		return new OrderChanger(selectedVehicle, pickupIndex, deliveryIndex, optPickupTo, optDeliveryTo, currCost);
	}

	/**
	 * find a better solution by assigning a task to another vehicle
	 * @return
	 */
	private VehiChanger updateSolutionByChangeVehicle() {
		int currCost = this.solutionCost;
		
		// choose a vehicle randomly
		Vehicle selectedVehicle = this.vehiclesWithTasks.get((int)(Math.random()*this.vehiclesWithTasks.size()));
		
		LinkedList<Step> steps = this.stepMap.get(selectedVehicle.id());
		
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
		
		Vehicle optVehicle = null;
		int optPickIndex = 1;
		int optDeliIndex = 1;
		
		for(Vehicle vehicle: this.vehicles){
		
			
			LinkedList<Step> vSteps = this.stepMap.get(vehicle.id());
			
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
						optVehicle = vehicle;
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
		
		if(optVehicle==null)
			return null;
		
		return new VehiChanger(selectedVehicle, optVehicle, pickupIndex, optPickIndex, optDeliIndex, currCost);
		
	}

	/**
	 * test whether 1 ~ end steps is valid, which means during these steps, the
	 * vehicle can load corresponding tasks
	 * @param vehicle
	 * @param vSteps
	 * @param end
	 * @return
	 */
	private boolean isValidStepSequence(Vehicle vehicle, LinkedList<Step> vSteps, int end) {
		int capacity = vehicle.capacity();
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
		List<Vehicle> vehicles = this.getVehicles();
		int cost = 0;
		
		// for each vehicle, compute the traveled distance
		for(Vehicle vehicle: vehicles){
			City currCity = vehicle.getCurrentCity();
			LinkedList<Step> steps = this.getStepMap().get(vehicle.id());
			
			int costPerKm = vehicle.costPerKm();
			
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
		
		for(Vehicle vehicle: this.vehicles)
			stepMap.put(vehicle.id(), new LinkedList<Step>());
		
		int vLen = this.vehicles.size();
		
		for(Task task: this.tasks){
			
			int vIndex = task.id % vLen;
			
			/* in current configuration, the weight of task is a constant 3,
			 * and the capacity of vehicle is 30, so the following statement 
			 * always return true
			 */
			// initialize the pickup step
			Vehicle vehicle = this.vehicles.get(vIndex);
			
			Pickup pickup = new Pickup(task);
				
			// initialize the delivery step
			Delivery delivery = new Delivery(task);
				
			pickup.setSibling(delivery);
			delivery.setSibling(pickup);
				
			stepMap.get(vehicle.id()).add(pickup);
			stepMap.get(vehicle.id()).add(delivery);
			
		}
		
		
		this.vehiclesWithTasks.addAll(this.vehicles);
		
	}

	public int getSolutionCost() {
		return solutionCost;
	}

	public void setSolutionCost(int solutionCost) {
		this.solutionCost = solutionCost;
	}

	public List<Vehicle> getVehicles() {
		return vehicles;
	}

	public void setVehicles(List<Vehicle> vehicles) {
		this.vehicles = vehicles;
	}

	public HashMap<Integer, LinkedList<Step>> getStepMap() {
		return stepMap;
	}

	public void setStepMap(HashMap<Integer, LinkedList<Step>> stepMap) {
		this.stepMap = stepMap;
	}

}
