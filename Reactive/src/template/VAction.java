package template;

import helpers.VActionType;
import logist.topology.Topology.City;

/*
 * action contains two part: (1)the action type, move or pickup
 * 	                         (2)destination city
 */
public class VAction{
	
	/*
	 * 0 for move, 1 for pickup
	 */
	private VActionType actionType;
	private City dstCity;
	
	public VAction(VActionType actionType, City city){
		this.actionType = actionType;
		this.dstCity = city;
	}

	@Override
	public boolean equals(Object obj){
		VAction action = (VAction)obj;
		
		if((this.actionType==action.getActionType())&&(this.dstCity.id==action.getDstCity().id))
			return true;
		
		return false;
		
	}

	public VActionType getActionType() {
		return actionType;
	}

	public void setActionType(VActionType actionType) {
		this.actionType = actionType;
	}

	public City getDstCity() {
		return dstCity;
	}

	public void setDstCity(City deliveryCity) {
		this.dstCity = deliveryCity;
	}
	
	
	
	
}
