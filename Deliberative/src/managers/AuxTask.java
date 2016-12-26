package managers;

import java.util.LinkedList;

import logist.task.Task;
import logist.topology.Topology.City;

public class AuxTask {
	
	public Task task;
	public String state;
	
	public AuxTask(Task task, String state){
		this.task = task;
		this.state = state;
	}

	public static double distanceFollowOrder(City startCity, LinkedList<AuxTask> taskOrder) {
		
		double distance = 0;
		
		for(AuxTask auxTask: taskOrder){
			if(auxTask.state.equals("pickuped")){
				distance += startCity.distanceTo(auxTask.task.deliveryCity);
				startCity = auxTask.task.deliveryCity;
			} else {
				distance += startCity.distanceTo(auxTask.task.pickupCity);
				startCity = auxTask.task.pickupCity;
			}
		}
		
		return distance;
	}

	public static int searchPickIndex(LinkedList<AuxTask> taskOrder, AuxTask auxTask) {
		
		int length = taskOrder.size();
		for(int i=0; i<length; i++){
			if(taskOrder.get(i).equals(auxTask.task))
				return i;
		}
		
		return 0;
	}
	
	

}
