package mains;

import logist.simulation.Vehicle;

public class VehiChanger {
	
	private Vehicle vehicleFrom;
	private Vehicle vehicleTo;
	
	int pickupFrom;
	int pickupTo;
	int deliveryTo;
	
	private int cost;
	
	public VehiChanger(Vehicle from, Vehicle to, int pickupFrom, int pickupTo,
			int deliveryTo, int cost){
		
		this.vehicleFrom = from;
		this.vehicleTo = to;
		
		this.pickupFrom = pickupFrom;
		this.pickupTo = pickupTo;
		this.deliveryTo = deliveryTo;
		
		this.cost = cost;
	}

	public Vehicle getVehicleFrom() {
		return vehicleFrom;
	}

	public void setVehicleFrom(Vehicle vehicleFrom) {
		this.vehicleFrom = vehicleFrom;
	}

	public Vehicle getVehicleTo() {
		return vehicleTo;
	}

	public void setVehicleTo(Vehicle vehicleTo) {
		this.vehicleTo = vehicleTo;
	}

	public int getPickupFrom() {
		return pickupFrom;
	}

	public void setPickupFrom(int pickupFrom) {
		this.pickupFrom = pickupFrom;
	}

	public int getPickupTo() {
		return pickupTo;
	}

	public void setPickupTo(int pickupTo) {
		this.pickupTo = pickupTo;
	}

	public int getDeliveryTo() {
		return deliveryTo;
	}

	public void setDeliveryTo(int deliveryTo) {
		this.deliveryTo = deliveryTo;
	}

	public int getCost() {
		return cost;
	}

	public void setCost(int cost) {
		this.cost = cost;
	}

}
