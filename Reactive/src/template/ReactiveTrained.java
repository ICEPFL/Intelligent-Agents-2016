package template;

import logist.simulation.Vehicle;
import helpers.VActionType;
import logist.agent.Agent;
import logist.behavior.ReactiveBehavior;
import logist.plan.Action;
import logist.plan.Action.Move;
import logist.plan.Action.Pickup;
import logist.task.Task;
import logist.task.TaskDistribution;
import logist.topology.Topology;

public class ReactiveTrained implements ReactiveBehavior {

	private VController controller = null;

	@Override
	public void setup(Topology topology, TaskDistribution td, Agent agent) {
		/* 
		 * Reads the discount factor from the agents.xml file.
		 * If the property is not present it defaults to 0.95 
		 */
		double discount = agent.readProperty("discount-factor", Double.class,
				0.95);
		
		VManager manager = new VManager(topology, td, agent);
		manager.initialize();
		
		controller = new VController(manager);
		
		controller.setGamma(discount);
		controller.trainPolicy();
	}

	@Override
	public Action act(Vehicle vehicle, Task availableTask) {
		Action action;
		
		VAction reactive = this.controller.reactiveToState(vehicle, availableTask);
		
		if(reactive.getActionType()==VActionType.Pickup)
			action = new Pickup(availableTask);
		else
			action = new Move(reactive.getDstCity());
		
		return action;
	}
}
