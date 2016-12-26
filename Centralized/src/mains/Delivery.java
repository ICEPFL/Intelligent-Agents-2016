package mains;

import logist.task.Task;
import mains.Step;

public class Delivery extends Step {

	private Pickup sibling;
	
	public Delivery(Task task) {
		super(task);
		
	}

	public Pickup getSibling() {
		return sibling;
	}

	public void setSibling(Pickup sibling) {
		this.sibling = sibling;
	}

}
