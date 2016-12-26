package helpers;

import template.VAction;
import template.VState;

public class VTriplet{
	
	private VState startState;
	private VState endState;
	private VAction action;
	
	public VTriplet(VState left, VState middle, VAction right){
		this.startState = left;
		this.endState = middle;
		this.action = right;
	}

	@Override
	public boolean equals(Object obj){
		VTriplet triplet = (VTriplet)obj;
		
		return (startState.equals(triplet.getStartState())) && (endState.equals(triplet.getEndState()))
				&&(action.equals(triplet.getAction()));
		
	}
	
	public VState getStartState() {
		return this.startState;
	}

	public VState getEndState() {
		return this.endState;
	}

	public VAction getAction() {
		return this.action;
	}
	
	

}
