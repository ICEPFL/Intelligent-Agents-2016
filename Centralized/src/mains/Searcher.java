package mains;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import logist.simulation.Vehicle;
import logist.task.TaskSet;

public class Searcher {

	private Solution currSolution;
	
	// used to save the optimal solution
	private HashMap<Integer, LinkedList<Step>> bestStepMap;
	private int bestCost;
	
	private double lowThreshold = 0.5;
	private double highThreshold = 1;
	
	private int retryNum;
	private int maxIteration = 2000;
	
	public Searcher(List<Vehicle> vehicles, TaskSet tasks){
		
		this.bestStepMap = new HashMap<Integer, LinkedList<Step>>();
		this.currSolution = new Solution(vehicles, tasks);
		
		this.updateBestSolutionTillNow(this.currSolution);
		
		this.retryNum = vehicles.size();
	}

	public void searchSolution(){
		
		int lastCost = 0;
		int currCost = 0;
		int counter = 0;
		
		// used to remark whether the algorithm falls into the local minimum
		int updateType = 1;
		
		for(int iteration=0; iteration<maxIteration; iteration++){
		
			switch(updateType){
			/*
			 * if falls into the local minimum, choose a random neighbor solution
			 */
			case 0:
				this.currSolution.updateSolutionByRandomChoice();
				updateType = 1;
				break;
			/*
			 * otherwise, normal procedure
			 */
			case 1:
				double probability = Math.random();
				
				// if number is bigger than high threshold, choose a random neighbor
				if(probability > this.highThreshold)
					this.currSolution.updateSolutionByRandomChoice();
			
				// if the number smaller than low threshold, choose the best neighbor
				else if(probability < this.lowThreshold)
					this.currSolution.updateSolutionBylocalChoice();
				
				else
					continue;
				
				break;
			default:
				System.out.println("Unexpected situtation");
			}
			
			currCost = this.currSolution.getSolutionCost();
			
			/* 
			 * if the current solution is better than the current optimal solution, 
			 * update the optimal solution
			 */
			if(currCost<this.bestCost){
				this.updateBestSolutionTillNow(currSolution);
				
				lastCost = currCost;
				counter = 0;
				
				System.out.println("Current best cost is " + this.bestCost);
				
				continue;
			}
			
			if(currCost==lastCost){
				counter += 1;
				
				// if the counter reach to the maximal retry number, falls into the local minimum
				if(counter==this.retryNum){
					updateType = 0;
					counter = 0;
				}
				
			}else{
				lastCost = currCost;
				counter = 0;
			}
			
		}
		
	}
	
	private void updateBestSolutionTillNow(Solution solution) {
		this.bestStepMap.clear();
		
		HashMap<Integer, LinkedList<Step>> stepMap = solution.getStepMap();
		for(Integer key: stepMap.keySet()){
			
			LinkedList<Step> steps = new LinkedList<Step>();
			for(Step step: stepMap.get(key)){
				steps.add(step);
			}
			
			this.bestStepMap.put(key, steps);
		}
		
		this.bestCost = solution.getSolutionCost();
		
	}

	public HashMap<Integer, LinkedList<Step>> getBestStepMap() {
		return bestStepMap;
	}

	public void setBestStepMap(HashMap<Integer, LinkedList<Step>> bestStepMap) {
		this.bestStepMap = bestStepMap;
	}

	public int getBestCost() {
		return bestCost;
	}

	public void setBestCost(int bestCost) {
		this.bestCost = bestCost;
	}
	
}
