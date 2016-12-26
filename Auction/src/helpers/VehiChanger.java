package helpers;

import utils.EnVehicle;

public class VehiChanger {
	
	private EnVehicle vehicleFrom;
	private EnVehicle vehicleTo;
	
	int pickupFrom;
	int pickupTo;
	int deliveryTo;
	
	private int cost;
	
	public VehiChanger(EnVehicle from, EnVehicle to, int pickupFrom, int pickupTo,
			int deliveryTo, int cost){
		
		this.vehicleFrom = from;
		this.vehicleTo = to;
		
		this.pickupFrom = pickupFrom;
		this.pickupTo = pickupTo;
		this.deliveryTo = deliveryTo;
		
		this.cost = cost;
	}

	public EnVehicle getVehicleFrom() {
		return vehicleFrom;
	}

	public void setVehicleFrom(EnVehicle vehicleFrom) {
		this.vehicleFrom = vehicleFrom;
	}

	public EnVehicle getVehicleTo() {
		return vehicleTo;
	}

	public void setVehicleTo(EnVehicle vehicleTo) {
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
