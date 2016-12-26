package managers;

import logist.task.Task;

public class TransferInfo {
	
	private State parent;
	private ActionType type;
	private Task task;
	
	public TransferInfo(){
		
	}

	public State getParent() {
		return parent;
	}

	public void setParent(State parent) {
		this.parent = parent;
	}

	public ActionType getType() {
		return type;
	}

	public void setType(ActionType type) {
		this.type = type;
	}

	public Task getTask() {
		return task;
	}

	public void setTask(Task task) {
		this.task = task;
	}

	public void setTansferInfo(State state, ActionType action, Task task) {
		this.parent = state;
		this.type = action;
		this.task = task;
	}
	
	

}
