package helpers;

import logist.task.Task;

public class Pickup extends Step {

	private Delivery sibling;
	
	public Pickup(Task task) {
		super(task);
	}

	public Step getSibling() {
		return sibling;
	}

	public void setSibling(Delivery sibling) {
		this.sibling = sibling;
	}

}
