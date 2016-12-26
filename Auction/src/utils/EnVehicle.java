package utils;

import java.util.LinkedList;
import java.util.List;

import logist.simulation.Vehicle;
import logist.topology.Topology.City;

public class EnVehicle {
	
	private int id;
	private int capacity;
	private int costPerKm;
	private City homeCity;
	
	public static List<EnVehicle> transfer(List<Vehicle> vehicles){
		List<EnVehicle> enVehicles = new LinkedList<EnVehicle> ();
		
		for(Vehicle vehicle: vehicles)
			enVehicles.add(new EnVehicle(vehicle));
		
		return enVehicles;
	}
	
	public static List<EnVehicle> create(List<City> homeCities, int capacity, int costPerKm){
		List<EnVehicle> enVehicles = new LinkedList<EnVehicle> ();
		
		for(City city: homeCities)
			enVehicles.add(new EnVehicle(capacity, costPerKm, city));
		
		return enVehicles;
	}
	
	public EnVehicle(Vehicle vehicle){
		this.id = vehicle.id();
		this.capacity = vehicle.capacity();
		this.costPerKm = vehicle.costPerKm();
		this.homeCity = vehicle.homeCity();
	}
	
	public EnVehicle(int capacity, int costPerKm, City homeCity){
		this.id = homeCity.id;
		this.capacity = capacity;
		this.costPerKm = costPerKm;
		this.homeCity = homeCity;
	}

	public int getId() {
		return id;
	}

	public int getCapacity() {
		return capacity;
	}

	public int getCostPerKm() {
		return costPerKm;
	}

	public City getHomeCity() {
		return homeCity;
	}

}
