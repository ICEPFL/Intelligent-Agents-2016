package template;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import helpers.VActionType;
import helpers.VTriplet;
import helpers.VTuple;
import logist.agent.Agent;
import logist.simulation.Vehicle;
import logist.task.TaskDistribution;
import logist.topology.Topology;
import logist.topology.Topology.City;

public class VManager {
	
	/*
	 * all possible states and actions
	 */
	private List<VState> states;
	private List<VAction> moveActions;
	private List<VAction> pickActions;
	
	/*
	 * auxiliary map: city ID -> all states related this city
	 */
	private Map<Integer, ArrayList<VState>> refStateMap; 
	
	/*
	 * the environment parameters
	 */
	private Topology topology;
	private TaskDistribution taskDistribution;
	private Agent agent;
	
	/*
	 * reward tables and transition table
	 */
	private Map<Vehicle, HashMap<VTuple, Double>> rewardMaps;
	private Map<VTriplet, Double> stateTransitionMap;
	
	
	public VManager(Topology topology, TaskDistribution taskDistribution, Agent agent){
		this.topology = topology;
		this.taskDistribution = taskDistribution;
		this.agent = agent;
	}
	
	public void initialize(){
		
		List<Vehicle> vehicles = agent.vehicles();
		rewardMaps = new HashMap<Vehicle, HashMap<VTuple, Double>>();
		
		for(Vehicle vehicle: vehicles)
			rewardMaps.put(vehicle, new HashMap<VTuple, Double>());
		
		stateTransitionMap = new HashMap<VTriplet, Double>();
		
		refStateMap = new HashMap<Integer, ArrayList<VState>>();
		
		List<City> cities = topology.cities();
		for(City city: cities)
			refStateMap.put(city.id, new ArrayList<VState>());
		
		states = new ArrayList<VState>();
		moveActions = new ArrayList<VAction>();
		pickActions = new ArrayList<VAction>();
		
		this.initialStates();
		this.initialActions();
		
		this.initializeRewardMaps();
		this.initializeTransitionMap();
		
	}

	/**
	 * initial all possible actions
	 */
	private void initialActions() {
		List<City> cities = topology.cities();
		
		for(City dstCity: cities){
			
			//action: move to destination city
			VAction moveToDst = new VAction(VActionType.Move, dstCity);
			
			//action: pickup the task in current city and delivery it to destination
			VAction pickToDst = new VAction(VActionType.Pickup, dstCity);
			
			this.moveActions.add(moveToDst);
			this.pickActions.add(pickToDst);
			
		}
		
	}

	/**
	 * initial all possible states
	 */
	private void initialStates() {
		List<City> cities = topology.cities();
		
		for(City currCity: cities){
			
			ArrayList<VState> refStates = this.refStateMap.get(currCity.id);
			
			//if the current city doesn't generate task
			VState nullState = new VState(currCity, null, false);
			
			this.states.add(nullState);
			refStates.add(nullState);
			
			for(City dstCity: cities){
				
				//if the current city generate a task to destination city
				VState withState = new VState(currCity, dstCity, true);
				
				this.states.add(withState);
				refStates.add(withState);
			}
		}
	}

	/**
	 * initialize Reward Tables
	 */
	private void initializeRewardMaps(){
		
		List<Vehicle> vehicles = this.agent.vehicles();
		
		//every vehicle has its own reward map
		for(Vehicle vehicle: vehicles){
			
			Map<VTuple, Double> map = this.rewardMaps.get(vehicle);
			
			for(VState state: this.states){
				
				//if move action is carried out
				for(VAction move: movementToNeighborCity(state.getCity())){
					VTuple tuple = new VTuple(state, move);
					
					double cost = -vehicle.costPerKm()*state.getCity().distanceTo(move.getDstCity());
					map.put(tuple, cost);
				}
				
				//if pickup action is carried out
				if(state.isHasTask()==true){
					VAction pickup = pickupToDeliveryCity(state.getTaskDestination());
					VTuple tuple = new VTuple(state, pickup);
					
					double reward = this.taskDistribution.reward(state.getCity(), state.getTaskDestination())
							- vehicle.costPerKm()*state.getCity().distanceTo(state.getTaskDestination());
					map.put(tuple, reward);
				}
				
			}
			
		}
		
	}

	/**
	 * initialize Transition Tables
	 */
	private void initializeTransitionMap(){
		
		for(VState state: this.states){
			
			/*
			 * if the move action is carried out
			 */
			for(VAction move: movementToNeighborCity(state.getCity())){
				
				//get all possible state after movement
				List<VState> statesAfterMove = this.refStateMap.get(move.getDstCity().id);
				
				for(VState resState: statesAfterMove){
					VTriplet triplet = new VTriplet(state, resState, move);
					
					double probablity = this.taskDistribution.probability(resState.getCity(), resState.getTaskDestination());
					this.stateTransitionMap.put(triplet, probablity);
					
				}
				
			}
			
			/*
			 * if the pickup action is carried out
			 */
			if(state.isHasTask()==true){
				VAction pickup = this.pickupToDeliveryCity(state.getTaskDestination());
				
				List<VState> statesAfterPick = this.refStateMap.get(pickup.getDstCity().id);
				
				for(VState resState: statesAfterPick){
					VTriplet triplet = new VTriplet(state, resState, pickup);
					
					double probablity = this.taskDistribution.probability(resState.getCity(), resState.getTaskDestination());
					this.stateTransitionMap.put(triplet, probablity);
				}
			}
		}
	}

	/**
	 * get all the move action to neighbor cities of the current city
	 * @param city
	 * @return
	 */
	public ArrayList<VAction> movementToNeighborCity(City city) {
		ArrayList<VAction> movements = new ArrayList<VAction>();
		
		for(VAction move: this.moveActions){
			if(city.neighbors().contains(move.getDstCity()))
				movements.add(move);
		}
		
		return movements;
	}

	/**
	 * get the pickup action to the destination city
	 * @param taskDestination
	 * @return
	 */
	public VAction pickupToDeliveryCity(City taskDestination) {
		for(VAction pickup: this.pickActions){
			if(pickup.getDstCity().equals(taskDestination))
				return pickup;
		}
		
		return null;
	}

	/**
	 * get the transition: (state -> nextState through action) 
	 * @param state
	 * @param nextState
	 * @param action
	 * @return
	 */
	public VTriplet searchTransition(VState state, VState nextState, VAction action) {
		VTriplet triplet = new VTriplet(state, nextState, action);
		
		for(VTriplet currTriplet: this.stateTransitionMap.keySet())
			if(currTriplet.equals(triplet))
				return currTriplet;
		
		return null;
	}
	
	public void setTopology(Topology topology) {
		this.topology = topology;
	}

	public void setTaskDistribution(TaskDistribution taskDistribution) {
		this.taskDistribution = taskDistribution;
	}

	public void setAgent(Agent agent) {
		this.agent = agent;
	}

	public List<VState> states() {
		return states;
	}

	public List<VAction> moveActions() {
		return moveActions;
	}

	public List<VAction> pickActions() {
		return pickActions;
	}

	public Map<Integer, ArrayList<VState>> refStateMap() {
		return refStateMap;
	}

	public Map<Vehicle, HashMap<VTuple, Double>> rewardMaps() {
		return rewardMaps;
	}

	public Map<VTriplet, Double> stateTransitionMap() {
		return stateTransitionMap;
	}

	public Agent agent() {
		return agent;
	}
	
}
