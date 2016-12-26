package helpers;

import java.util.LinkedList;
import java.util.List;

import logist.task.Task;
import logist.topology.Topology.City;

public class CityEstimater {
	
	private List<City> upperCities;
	private List<City> lowerCities;
	
	private List<City> cities;
	
	public CityEstimater(List<City> cities){
		this.cities = cities;
		this.upperCities = new LinkedList<City> ();
		this.lowerCities = new LinkedList<City> ();
	}
	
	public City estimateInitialCity(Long value, Task task, double costPerKm) {
		
		this.upperCities.clear();
		this.lowerCities.clear();
		
		if(value==null)
			return null;
		
		City target = null;
		double costDiff = Double.MAX_VALUE;
		
		for(City city: cities){
			double currCost = (city.distanceTo(task.pickupCity)+task.pathLength())*costPerKm;
			
			if(currCost>=value)
				this.upperCities.add(city);
			else
				this.lowerCities.add(city);
			
			double currCostDiff = Math.abs(currCost-value);
			
			if(currCostDiff<costDiff){
				costDiff = currCostDiff;
				target = city;
			}
		}
		
		return target;
		
	}

	public List<City> getUpperCities() {
		return upperCities;
	}

	public List<City> getLowerCities() {
		return lowerCities;
	}

}
