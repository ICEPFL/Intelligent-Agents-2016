package mains;

import logist.task.Task;

public class Step {
	
	private Task task;
	
	public Step(Task task){
		this.task = task;
	}

	public Task getTask() {
		return task;
	}

	public void setTask(Task task) {
		this.task = task;
	}

}
