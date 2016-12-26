import java.util.ArrayList;

import uchicago.src.sim.space.Object2DGrid;

/**
 * Class that implements the simulation space of the rabbits grass simulation.
 * @author Jinbu Liu & Mengjie Zhao
 */

public class RabbitsGrassSimulationSpace {
	
	private Object2DGrid grassSpace;
	private Object2DGrid rabbitSpace;
	private int grassEnergy;
	
	public RabbitsGrassSimulationSpace(int xSize, int ySize){
		this.grassSpace = new Object2DGrid(xSize, ySize);
		this.rabbitSpace = new Object2DGrid(xSize, ySize);
		
		for(int i=0; i<xSize; i++){
			for(int j=0; j<ySize; j++){
				this.grassSpace.putObjectAt(i, j, new Integer(0));
			}
		}
	}

	public void spreadGrass(int numGrass) {
		
		int totalLoc = grassSpace.getSizeX()*grassSpace.getSizeY();
		ArrayList<Integer> location = new ArrayList<Integer>(totalLoc);
		for(int i=0; i<totalLoc; i++)
			location.add(i, i);
		
		int counterLoc = 0; 
		int counterGra = 0;
		
		/*
		 * randomly place grass in the grass space 
		 */
		while((counterLoc<totalLoc)&&(counterGra<numGrass)){
			int locOrder = (int)(Math.random()*location.size());
			int loc = location.get(locOrder);
			
			int x = loc % grassSpace.getSizeX();
			int y = loc / grassSpace.getSizeY();
			
			if((Integer)(grassSpace.getObjectAt(x, y))==0){
				grassSpace.putObjectAt(x, y, new Integer(1));
				counterGra += 1;
			} 
			location.remove((int)locOrder);
			counterLoc += 1;		
		}
		if(counterGra<numGrass){
			System.out.println("The space is full!");
		}
		
	}

	public Object2DGrid getCurrentGrassSpace() {
		return this.grassSpace;
	}
	
	public boolean isCellOccupied(int x, int y){
		if(this.rabbitSpace.getObjectAt(x, y)!=null)
			return true;
		return false;
	}
	
	public boolean addRabbit(RabbitsGrassSimulationAgent rabbit){
		boolean retValue = false;
		
		int totalLoc = rabbitSpace.getSizeX()*rabbitSpace.getSizeY();
		ArrayList<Integer> location = new ArrayList<Integer>(totalLoc);
		for(int i=0; i<totalLoc; i++)
			location.add(i, i);
		
		int count = 0;
		while((retValue==false)&&(count<totalLoc)){
			int locOrder = (int)(Math.random()*location.size());
			int loc = location.get(locOrder);
			
			int x = loc % rabbitSpace.getSizeX();
			int y = loc / rabbitSpace.getSizeY();
			
			if(this.isCellOccupied(x, y)==false){
				rabbitSpace.putObjectAt(x, y, rabbit);
				rabbit.setXY(x, y);
				rabbit.setRabbitGrassSpace(this);
				retValue = true;
			} else {
				location.remove((int)locOrder);
			}
			
			count += 1;
		}
		
		return retValue; 
	}

	public Object2DGrid getCurrentRabbitSpace() {
		return this.rabbitSpace;
	}

	public void removeRabbitAt(int x, int y) {
		this.rabbitSpace.putObjectAt(x, y, null);
	}

	public int eatEnergyAt(int x, int y) {
		int energy = this.grassEnergy * ((Integer) this.grassSpace.getObjectAt(x, y));
		this.grassSpace.putObjectAt(x, y, new Integer(0));
		
		return energy;
	}

	public void setGrassEnergy(int grassEnergy) {
		this.grassEnergy = grassEnergy;
	}

	public boolean moveRabbitAt(int x, int y, int newX, int newY) {
		boolean retValue = false;
		if(!this.isCellOccupied(newX, newY)){
			RabbitsGrassSimulationAgent rabbit = (RabbitsGrassSimulationAgent)rabbitSpace.getObjectAt(x, y);
			removeRabbitAt(x, y);
			rabbit.setXY(newX, newY);
			rabbitSpace.putObjectAt(newX, newY, rabbit);
			retValue = true;
		}
		return retValue;
	}
	public int getGrassAt(int x, int y){
		int i;
		if((Integer)grassSpace.getObjectAt(x, y)!=0){
			i = 1;
		} else {
			i = 0;
		}
		return i;	
	}
	public int getRabbitsAt(int x, int y){
		int i;
		if(rabbitSpace.getObjectAt(x, y)!=null){
			i = 1;
		} else {
			i = 0;
		}
		return i;	
	}
	
	public int getTotalGrass(){
		int totalGrass = 0;
		for(int i=0; i<grassSpace.getSizeX(); i++){
			for(int j=0;j<grassSpace.getSizeY(); j++){
				totalGrass += getGrassAt(i, j);
			}
		
		}
	return totalGrass;
	}

}
