package managers;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import logist.agent.Agent;
import logist.plan.Plan;
import logist.task.TaskDistribution;
import logist.task.TaskSet;
import logist.topology.Topology;
import logist.topology.Topology.City;

public class Manager {

	/*
	 * initialization parameters
	 */
	Topology topology;
	Agent agent;
	TaskDistribution distribution;

	private State currState;
	private String vehicleID;

	public Manager(Topology topology, TaskDistribution distribution, Agent agent) {
		this.topology = topology;
		this.agent = agent;
		this.distribution = distribution;

		this.vehicleID = agent.vehicles().get(0).name();
	}

	/**
	 * initialize the state
	 * @param currCity
	 * @param tasks
	 * @param capacity
	 */
	public void initialState(City currCity, TaskSet tasks, int capacity) {
		if (this.currState == null)
			this.currState = new State(currCity, tasks, capacity);

		this.currState.initialState(currCity, tasks);
	}

	/**
	 * when the original plan is canceled, save the carried tasks
	 * @param carriedTasks
	 */
	public void saveCarriedTasks(TaskSet carriedTasks) {
		this.currState.saveCarriedTasks(carriedTasks);
	}

	/**
	 * according to the algorithm, make a plan
	 * @param algorithm
	 * @return
	 * 		Plan for the future actions
	 */
	public Plan makePlan(Algorithm algorithm) {
		Plan plan = new Plan(this.currState.getCurrCity());
		LinkedList<State> path = null;

		switch(algorithm){
		case BFS:
			path = this.findPathToGoalStateByBFS();
			break;
		case ASTAR:
			path = this.findPathToGoalStateByAStar();
			break;
		default:
			throw new AssertionError("Unsupported search method.");
		}
	
		City currCity = currState.getCurrCity();
		for (State state : path) {
			TransferInfo info = state.getTransferInfo();
			
			if(!state.getCurrCity().equals(currCity)){
				plan.appendMove(state.getCurrCity());
			}
			
			currCity = state.getCurrCity();
			
			if (info.getType() == ActionType.Pickup) {

				plan.appendPickup(info.getTask());

				System.out.println(this.vehicleID + " Pickup a task at " + state.getCurrCity().name + ": go to " + state.getCurrCity().name);

			} else if (info.getType() == ActionType.Delivery) {

				plan.appendDelivery(info.getTask());

				System.out.println(this.vehicleID + " Delivery a task to " + state.getCurrCity().name + ": go to " + state.getCurrCity().name);

			} else {

				System.out.println(this.vehicleID + " Move to " + state.getCurrCity().name);

			}
		}

		return plan;
	}

	/**
	 * find a path to the goal state using A* algorithm
	 * @return
	 * 		array of State representing the states path
	 */
	private LinkedList<State> findPathToGoalStateByAStar() {
		LinkedList<State> queue = new LinkedList<State>();
		queue.addLast(currState);

		LinkedList<State> checkedSet = new LinkedList<State>();

		State goalState = null;
		
		StateComparator comparator = new StateComparator();

		while (!queue.isEmpty()) {
			
			State state = queue.poll();
			if (state.isGoalState()) {
				goalState = state;
				break;
			}

			if (!checkedSet.contains(state)) {
				checkedSet.add(state);
				ArrayList<State> nextStates = state.getNextStatesByCity();

				queue.addAll(nextStates);

			} else {
				State clone = checkedSet.get(checkedSet.indexOf(state));
				if (state.getCost() < clone.getCost()) {
					
					//checkedSet.add(state);
					clone.setCost(state.getCost());
					ArrayList<State> nextStates = state.getNextStatesByCity();

					queue.addAll(nextStates);

				}

			}

			comparator.quickSortState(queue, 0, queue.size()-1);
			
		}

		if (goalState == null)
			return null;

		LinkedList<State> path = new LinkedList<State>();
		State state = goalState;

		// backtrack the state path  
		while (!state.equals(this.currState)) {
			path.push(state);
			state = state.getTransferInfo().getParent();
		}
		
		return path;
	}

	/**
	 * find a path to the goal state using A* algorithm
	 * @return
	 * 		array of State representing the states path
	 */
	public LinkedList<State> findPathToGoalStateByBFS() {

		LinkedList<State> queue = new LinkedList<State>();
		queue.addLast(currState);

		HashSet<State> checkedSet = new HashSet<State>();

		State goalState = null;

		while (!queue.isEmpty()) {
			
			State state = queue.poll();
			if (state.isGoalState()) {
				goalState = state;
				break;
			}

			if (!checkedSet.contains(state)) {
				checkedSet.add(state);
				ArrayList<State> nextStates = state.getNextStatesByCity();

				for(State nextState: nextStates){
					if(!queue.contains(nextState))
						queue.add(nextState);
				}
				
				//queue.addAll(nextStates);
				
			} 
			
		}

		if (goalState == null)
			return null;

		LinkedList<State> path = new LinkedList<State>();
		State state = goalState;

		// backtrack the state path
		while (!state.equals(this.currState)) {
			path.push(state);
			state = state.getTransferInfo().getParent();
		}

		return path;
	}

}
