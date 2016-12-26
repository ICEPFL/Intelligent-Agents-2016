package utils;

import java.util.HashMap;
import java.util.LinkedList;

import helpers.Step;

public class Result {
	
	private double cost;
	private HashMap<Integer, LinkedList<Step>> stepMap;
	
	public Result(double cost, HashMap<Integer, LinkedList<Step>> stepMap){
		this.cost = cost;
		this.stepMap = stepMap;
	}

	public double getCost() {
		return cost;
	}

	public HashMap<Integer, LinkedList<Step>> getStepMap() {
		return stepMap;
	}

}
