package helpers;

import template.VAction;
import template.VState;

public class VTuple{
	
	private VState state;
	private VAction action;
	
	public VTuple(VState state, VAction action){
		this.state = state;
		this.action = action;
	}

	public VState getState() {
		return state;
	}

	public VAction getAction() {
		return action;
	}
	
	@Override
	public boolean equals(Object obj){
		VTuple tuple = (VTuple) obj;
		
		if(this.state.equals(tuple.getState())&&this.action.equals(tuple.getAction()))
			return true;
		
		return false;
	}
	
}
