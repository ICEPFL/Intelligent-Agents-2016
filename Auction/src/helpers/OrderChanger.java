package helpers;

import utils.EnVehicle;

public class OrderChanger {
	
	private EnVehicle vehicle;
	
	private int pickupFrom;
	private int deliveryFrom;
	
	private int pickupTo;
	private int deliveryTo;
	
	private int cost;
	
	public OrderChanger(EnVehicle vehicle, int pickupFrom, int deliveryFrom, 
			int pickupTo, int deliveryTo, int cost){
		this.vehicle = vehicle;
		
		this.pickupFrom = pickupFrom;
		this.deliveryFrom = deliveryFrom;
		this.pickupTo = pickupTo;
		this.deliveryTo = deliveryTo;
		
		this.cost = cost;
	}

	public EnVehicle getVehicle() {
		return vehicle;
	}

	public void setVehicle(EnVehicle vehicle) {
		this.vehicle = vehicle;
	}

	public int getPickupFrom() {
		return pickupFrom;
	}

	public void setPickupFrom(int pickupFrom) {
		this.pickupFrom = pickupFrom;
	}

	public int getDeliveryFrom() {
		return deliveryFrom;
	}

	public void setDeliveryFrom(int deliveryFrom) {
		this.deliveryFrom = deliveryFrom;
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
