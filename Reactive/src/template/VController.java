package template;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import helpers.VTriplet;
import helpers.VTuple;
import logist.simulation.Vehicle;
import logist.task.Task;
import logist.topology.Topology.City;

public class VController {
	
	/*
	 * every vehicle has its own policy which is needed to be trained,
	 */
	private Map<Vehicle, HashMap<VState, VPolicyEntry>> policy;
	
	private VManager manager;
	
	/*
	 * parameters used for training 
	 */
	private double trainThreshold = 1;
	private double gamma = 0.8;
	
	public VController(VManager manager){
		
		this.manager = manager;
	
		this.policy = new HashMap<Vehicle, HashMap<VState, VPolicyEntry>>();
		initialPolicy();
	}

	/**
	 * train policies for every vehicle
	 */
	public void trainPolicy(){
		
		List<Vehicle> vehicles = manager.agent().vehicles();
		
		for(Vehicle vehicle: vehicles){
			
			// the current policy of the vehicle
			HashMap<VState, VPolicyEntry> currPolicy = this.policy.get(vehicle);
			
			// the current reward map of the vehicle
			HashMap<VTuple, Double> currRewardMap = manager.rewardMaps().get(vehicle);
			
			ArrayList<Double> improvements = new ArrayList<Double>();
			double overallImprove = Integer.MAX_VALUE;
			
			/*
			 * continue to train if the current improvement is bigger than threshold
			 */
			while(overallImprove>trainThreshold){
				
				improvements.clear();
				/*
				 * for each state, find a optimal action
				 */
				for(VState state: manager.states()){
					
					VAction optimalAction = null;
					
					double optimalReward = Integer.MIN_VALUE;
					
					// retrieve move actions
					for(VAction move: manager.movementToNeighborCity(state.getCity())){
						
						// get the possible result states after the current move action
						List<VState> resState = manager.refStateMap().get(move.getDstCity().id);
						
						double currReward = baseReward(currRewardMap, state, move)+ gamma*MDPReward(state, move, resState, currPolicy);
						
						// if the reward improves, update the optimal reward and action 
						if(currReward>optimalReward){
							optimalAction = move;
							optimalReward = currReward;
						}
						
					}
					
					// if there exist a task in the current state, carry out pickup
					if(state.isHasTask()==true){
						VAction pickup = manager.pickupToDeliveryCity(state.getTaskDestination());
						
						// get the possible result states after the current pickup action
						List<VState> resState = manager.refStateMap().get(pickup.getDstCity().id);
						
						//Tuple tuple = new Tuple(state, pickup);
						double currReward = baseReward(currRewardMap, state, pickup) + gamma*MDPReward(state, pickup, resState, currPolicy);
 					
						if(currReward>optimalReward){
							optimalAction = pickup;
							optimalReward = currReward;
						}
					}
					
					// put the improvement of the policy of state into the list 
					improvements.add(Math.abs(optimalReward-currPolicy.get(state).getProfit()));
					
					currPolicy.get(state).setAntion(optimalAction);
					currPolicy.get(state).setProfit(optimalReward);
					
				}
				
				// update the max improvement
				overallImprove = Collections.max(improvements);
			}
			
		}
		
	}
	
	private Double baseReward(HashMap<VTuple, Double> currRewardMap, VState state, VAction move) {
		VTuple tuple = new VTuple(state, move);
		
		for(VTuple currTuple: currRewardMap.keySet()){
			if(currTuple.equals(tuple)){
				return currRewardMap.get(currTuple);
			}
		}
		
		return null;
	}

	/**
	 * carry out Markov Decision Processes
	 * @param state 
	 * 				current state
	 * @param action
	 * 				action carried out by the vehicle
	 * @param resState
	 * 				result states after action
	 * @param currPolicy
	 * 				current policy of the vehicle
	 * @return
	 * 				the expected rewards of all possible result states 
	 */
	private double MDPReward(VState state, VAction action, List<VState> resState, HashMap<VState, VPolicyEntry> currPolicy) {
		double reward = 0;
		
		for(VState nextState: resState){
			
			VTriplet triplet = manager.searchTransition(state, nextState, action);	
			
			double probablity = manager.stateTransitionMap().get(triplet);
			double nextReward = currPolicy.get(nextState).getProfit();
			
			reward += probablity * nextReward;
		}
		
		return reward;
	}

	private void initialPolicy() {
		List<Vehicle> vehicles = manager.agent().vehicles();
		
		/*
		 * initial a policy map for every vehicle
		 */
		for(Vehicle vehicle: vehicles){
			HashMap<VState, VPolicyEntry> vPolicy = new HashMap<VState, VPolicyEntry>();
			
			for(VState state: manager.states())
				vPolicy.put(state, new VPolicyEntry(null, 0));
			
			this.policy.put(vehicle, vPolicy);
		}
	}
	
	public VAction reactiveToState(Vehicle vehicle, Task task){
		
		HashMap<VState, VPolicyEntry> vPolicy = this.policy.get(vehicle);
		
		City currCity = vehicle.getCurrentCity();
		
		VState currState = null;
		if(task!=null)
			currState = new VState(currCity, task.deliveryCity, true);
		else
			currState = new VState(currCity, null, false);
		
		for(VState state: vPolicy.keySet()){
			if(state.equals(currState))
				return vPolicy.get(state).getAntion();
		}
		
		return null;
		
	}
	
	public void setGamma(double gamma) {
		this.gamma = gamma;
	}
	
}
