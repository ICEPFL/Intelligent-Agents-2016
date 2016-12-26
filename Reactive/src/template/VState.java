package template;

import logist.topology.Topology.City;

/*
 * state contains three part: (1)current city
 * 							  (2)is there a task in current city
 * 							  (3)if task exists, the destination of the task
 */
public class VState{
	private City city;
	private City taskDst;
	private Integer hasTask;	
	
	public VState(City from, City taskDst, boolean hasTask){
		this.city = from;
		this.taskDst = taskDst;
		
		if(hasTask==true)
			this.hasTask = 1;
		else
			this.hasTask = 0;
	}

	@Override
	public boolean equals(Object obj){
		VState state = (VState)obj;
		
		if(this.city.id != state.getCity().id)
			return false;
		
		if(this.isHasTask() != state.isHasTask())
			return false;
		
		if(this.isHasTask()){
			if(this.taskDst.id==state.getTaskDestination().id)
				return true;
			
			return false;
		} 
		else 
			return true;
	}
	
	public City getCity() {
		return city;
	}
	
	public boolean isHasTask() {
		if(hasTask==1)
			return true;
		return false;
	}
	
	public City getTaskDestination(){
		return this.taskDst;
	}
}
