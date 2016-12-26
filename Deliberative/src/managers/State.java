package managers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import logist.task.Task;
import logist.task.TaskSet;
import logist.topology.Topology.City;

public class State implements Cloneable{

	private City currCity;
	private TaskSet tasks;
	
	/*
	 * the state of tasks
	 */
	private TState taskStates;
	
	/*
	 * the capacity of the vehicle
	 */
	private int maxWeight;
	/*
	 * the weight of the current load of the vehicle
	 */
	private int taskWeight = 0;
	
	/*
	 * parameter for backward trace, contains parent information 
	 */
	private TransferInfo fromParent;
	
	/*
	 * parameters for ASTAR 
	 */
	// the cost of current state, f(n)=g(n)+h(n)
	private double cost = 0;
	// the distance from current state to the initial state, that is g(n)
	private double disToInit = 0;
	// the distance from current state to the goal state, that is h(n)
	private double disToGoal = 0;
	
	public State(City currCity, TaskSet tasks, int capacity){
		this.currCity = currCity;
		this.tasks = tasks;
		this.maxWeight = capacity;
		this.taskStates = new TState();
		
		this.fromParent = null;
		
	}
	
	public void initialState(City city, TaskSet tasks) {
		this.currCity = city;
		
		this.cost = 0;
		this.disToInit = 0;
		this.disToGoal = 0;
		
		this.tasks.addAll(tasks);
		this.taskStates.addUnpickupedTasks(tasks);
	}
	
	/**
	 * clear the tasks state and renew it by saving carried tasks,
	 * also update the weight of current load
	 * @param carriedTasks
	 */
	public void saveCarriedTasks(TaskSet carriedTasks) {
		this.tasks.clear();
		this.tasks.addAll(carriedTasks);
		
		this.taskWeight = carriedTasks.weightSum();
		this.taskStates.renewCarriedTasks(carriedTasks);
	}
	
	/**
	 * get the neighbor states of current state, by moving to the
	 * neighbor cities from current city
	 * @return
	 * 		all possible neighbor states
	 */
	public ArrayList<State> getNextStatesByCity(){
		
		ArrayList<State> nextStates = new ArrayList<State>();
		List<City> neighbors = this.currCity.neighbors();
		
		/*
		 * delivery a task to a neighbor city or current city
		 */
		for(Task task: this.taskStates.getHoldedTasks()){
			if(this.currCity.equals(task.deliveryCity)||neighbors.contains(task.deliveryCity)){
				State nextState = (State) this.clone();
				
				// setup the parent information
				nextState.renowTansferInfo(this, ActionType.Delivery, task);
				nextState.renewStateByDelivery(task);
				
				// used for A*, update the cost
				nextState.renewCost(this.currCity, this.disToInit);
				
				nextStates.add(nextState);
			}
		}
		
		/*
		 * pickup a task at a neighbor city or current city
		 */
		for(Task task: this.taskStates.getUnholdedTask()){
			if((this.currCity.equals(task.pickupCity)||neighbors.contains(task.pickupCity))&&canLoadMoreTask(task)){
				State nextState = (State) this.clone();
				
				// setup the parent information
				nextState.renowTansferInfo(this, ActionType.Pickup, task);
				nextState.renewStateByPickup(task);
				
				// used for A*, update the cost
				nextState.renewCost(this.currCity, this.disToInit);
				
				nextStates.add(nextState);
			}
		}
		
		/*
		 * just move to a neighbor city
		 */
		for(City city: neighbors){
			State nextState = (State) this.clone();
			
			// setup the parent information
			nextState.renowTansferInfo(this, ActionType.Move, null);
			nextState.renewStateByMove(city);
			
			// used for A*, update the cost
			nextState.renewCost(this.currCity, this.disToInit);
			
			nextStates.add(nextState);
		}
		
		/*
		 * to avoid deterministic result
		 */
		Collections.shuffle(nextStates);
		
		return nextStates;
		
	}

	private void renowTansferInfo(State state, ActionType action, Task task) {
		this.getTransferInfo().setTansferInfo(state, action, task);
	}

	
	private void renewCost(City city, double distance) {
		this.setDistanceToInit(distance+city.distanceTo(this.currCity));
		this.setDistanceToGoal(this.taskStates.distanceToDeliveryTasks(this.currCity));
		
		this.cost = this.disToInit + this.disToGoal;
	}

	/*
	 * test whether the vehicle can take more tasks
	 */
	private boolean canLoadMoreTask(Task task) {
		if(this.taskWeight+task.weight>this.maxWeight)
			return false;
		
		return true;
	}

	/**
	 * update the state after moving to a neighbor city: change the current city
	 * @param nextCity
	 */
	private void renewStateByMove(City nextCity) {
		this.currCity = nextCity;
	}
	
	/**
	 * update the state after delivering a task: change the current city
	 * and decrease the weight of load
	 * @param task
	 */
	private void renewStateByDelivery(Task task) {
		this.currCity = task.deliveryCity;
		this.taskWeight -= task.weight;
		
		this.taskStates.renewStateByDelivery(task);
	}
	
	/**
	 * update the state after picking up a task: change the current city
	 * and increase the weight of load
	 * @param task
	 */
	private void renewStateByPickup(Task task) {
		this.currCity = task.pickupCity;
		this.taskWeight += task.weight;
		
		this.taskStates.renewStateByPickup(task);
	}
	
	/**
	 * check whether the current state is the goal state
	 * @return
	 * 		true for current state is the goal state, otherwise, false
	 */
	public boolean isGoalState(){
		
		// check whether all the tasks have been delivered
		if(this.tasks.size()==this.taskStates.getDeliveriedTask().size())
			return true;
		
		return false;
	}

	@Override 
	public int hashCode(){
		
		int code = 17;
		code = 31 * code + this.currCity.hashCode();
		code = 31 * code + this.taskStates.hashCode();
		
		return code;
	}
	
	@Override
	public boolean equals(Object obj){
		
		State state = (State)obj;
		if(!this.currCity.equals(state.getCurrCity()))
			return false;
		if(!this.taskStates.equals(state.getTState()))
			return false;
		
		return true;
	}
	
	@Override
	public Object clone(){
		
		State clone = new State(this.currCity, this.tasks, this.maxWeight);
		
		clone.setTState((TState)this.taskStates.clone());
		clone.setTaskWeight(this.taskWeight);
		
		return clone;
	}
	
	public TransferInfo getTransferInfo() {
		if(this.fromParent==null)
			this.fromParent = new TransferInfo();
		
		return this.fromParent;
	}

	public City getCurrCity() {
		return currCity;
	}
	
	public TState getTState() {
		return taskStates;
	}

	public void setTState(TState taskStates) {
		this.taskStates = taskStates;
	}
	
	public int getTaskWeight() {
		return taskWeight;
	}

	public void setTaskWeight(int taskWeight) {
		this.taskWeight = taskWeight;
	}

	public double getCost() {
		return cost;
	}

	public void setCost(double cost) {
		this.cost = cost;
	}

	public double getDistanceToGoal() {
		return disToGoal;
	}

	public void setDistanceToGoal(double disToGoal) {
		this.disToGoal = disToGoal;
	}

	public double getDistanceToInit() {
		return disToInit;
	}

	public void setDistanceToInit(double disToInit) {
		this.disToInit = disToInit;
	}
}
