package template;

import logist.agent.Agent;
import logist.behavior.DeliberativeBehavior;
import logist.plan.Plan;
import logist.simulation.Vehicle;
import logist.task.TaskDistribution;
import logist.task.TaskSet;
import logist.topology.Topology;
import managers.Algorithm;
import managers.Manager;

public class DeliberativeMain implements DeliberativeBehavior{

	private Manager manager;
	
	private Algorithm algorithm;
	
	@Override
	public void setup(Topology topology, TaskDistribution distribution, Agent agent) {
		
		this.manager = new Manager(topology, distribution, agent);
		String algorithmName = agent.readProperty("algorithm", String.class, "ASTAR");
		
		this.algorithm = Algorithm.valueOf(algorithmName.toUpperCase());
	}

	@Override
	public Plan plan(Vehicle vehicle, TaskSet tasks) {
		
		manager.initialState(vehicle.getCurrentCity(), tasks, vehicle.capacity());
		
		long start = System.currentTimeMillis();
		
		Plan plan = manager.makePlan(algorithm);
		
		long duration = System.currentTimeMillis()-start;
		
		System.out.println("Plan time: "+duration);
		
		return plan;
	}

	@Override
	public void planCancelled(TaskSet carriedTasks) {
		
		manager.saveCarriedTasks(carriedTasks);
	}

}
