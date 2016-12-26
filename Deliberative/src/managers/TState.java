package managers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import logist.task.Task;
import logist.task.TaskSet;
import logist.topology.Topology.City;

public class TState implements Cloneable{
	
	private ArrayList<Task> unpickuped;
	private ArrayList<Task> pickuped;
	private ArrayList<Task> deliveried;
	
	public TState(){
		
		this.unpickuped = new ArrayList<Task> ();
		this.pickuped = new ArrayList<Task> ();
		this.deliveried = new ArrayList<Task> ();
		
	}
	
	public void addUnpickupedTasks(TaskSet tasks){
		for(Task task: tasks){
			if(!this.pickuped.contains(task))
				this.unpickuped.add(task);
		}
	}

	public void renewCarriedTasks(TaskSet carriedTasks) {
		this.pickuped.clear();
		this.pickuped.addAll(carriedTasks);
		
		this.unpickuped.clear();
		this.deliveried.clear();
	}
	
	public void renewStateByDelivery(Task task) {
		this.deliveried.add(task);
		this.pickuped.remove(task);
	}
	
	public void renewStateByPickup(Task task) {
		this.unpickuped.remove(task);
		this.pickuped.add(task);
	}
	
	public ArrayList<Task> getHoldedTasks(){
		return this.pickuped;
	}
	
	public ArrayList<Task> getUnholdedTask(){
		return this.unpickuped;
	}

	public ArrayList<Task> getDeliveriedTask(){
		return this.deliveried;
	}

	/**
	 * guess the distance from current tasks' state to the goal state, using 
	 * the random method
	 * @param City: current city
	 * @return distance
	 */
	public double distanceToDeliveryTasks(City currCity) {
		
		double distance = 0;
		double disOptimal = Double.MAX_VALUE;
		/*
		 * create an auxiliary to save all the works needed to do in the 
		 * following steps 
		 */
		LinkedList<AuxTask> taskOrder = new LinkedList<AuxTask> ();
		LinkedList<AuxTask> deliOrder = new LinkedList<AuxTask> ();
		
		for(Task task: this.unpickuped){
			taskOrder.add(new AuxTask(task, "unpickuped"));
			deliOrder.add(new AuxTask(task, "pickuped"));
		}
			
		
		for(Task task: this.pickuped)
			taskOrder.add(new AuxTask(task, "pickuped"));
	
		int trailNum = 100;
		
		for(int i=0; i<trailNum; i++){
			
			// shuffle the order of works, 
			Collections.shuffle(taskOrder);
			
			for(AuxTask auxTask: deliOrder){
				// auxTask must occur after the follow index
				int pickIndex = AuxTask.searchPickIndex(taskOrder, auxTask);
				int deliIndex = (int)(Math.random()*(taskOrder.size()-pickIndex))+1;
				
				taskOrder.add(deliIndex, auxTask);
			}
			
			// get the distance to the goal state if follow the above working order
			distance = AuxTask.distanceFollowOrder(currCity, taskOrder);
			
			if(disOptimal>distance)
				disOptimal = distance;
		}
		
		// use the expected distance to represent the real distance to the goal state
		
		return 0.5 * disOptimal;
	}
	
	@Override 
	public int hashCode(){
		
		int code = 5;
		
		code = 89 * code;
		for(Task task: this.unpickuped)
			code +=  task.hashCode();
		
		code = 89 * code;
		for(Task task: this.pickuped)
			code +=  task.hashCode();
		
		code = 89 * code;
		for(Task task: this.deliveried)
			code +=  task.hashCode();
		
		return code;
		
	}

	@Override
	public boolean equals(Object obj){
		TState state = (TState)obj;
		for(Task task: this.unpickuped){
			if(!state.getUnholdedTask().contains(task))
				return false;
		}
		
		for(Task task: this.pickuped){
			if(!state.getHoldedTasks().contains(task))
				return false;
		}
		
		for(Task task: this.deliveried){
			if(!state.getDeliveriedTask().contains(task))
				return false;
		}
		
		return true;
		
	}
	
	@Override
	public Object clone(){
		TState clone = new TState();
		for(Task task: this.unpickuped)
			clone.getUnholdedTask().add(task);
		for(Task task: this.pickuped)
			clone.getHoldedTasks().add(task);
		for(Task task: this.deliveried)
			clone.getDeliveriedTask().add(task);
		
		return clone;
		
	}

}
