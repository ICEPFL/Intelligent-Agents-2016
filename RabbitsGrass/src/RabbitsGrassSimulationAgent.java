import java.awt.Color;

import uchicago.src.sim.gui.Drawable;
import uchicago.src.sim.gui.SimGraphics;
import uchicago.src.sim.space.Object2DGrid;


/**
 * Class that implements the simulation agent for the rabbits grass simulation.

 * @author Jinbu Liu & Mengjie Zhao
 */

public class RabbitsGrassSimulationAgent implements Drawable {

	private int x;
	private int y;
	private int vX;
	private int vY;
	private int energy;
	private int stepEnergyLoss;
	private int ID;
	private static int IDNumber = 0;
	
	private RabbitsGrassSimulationSpace rgSpace;
	
	public RabbitsGrassSimulationAgent(int energy, int energyLoss){
		this.x = -1;
		this.y = -1;
		this.energy = energy;
		this.stepEnergyLoss = energyLoss;
		this.setDirection();
		IDNumber++;
		this.ID = IDNumber;
	}
	
	private void setDirection() {
		vX = ((int)(Math.random()*3))-1;
		if(vX != 0)
			vY = 0;
		else{
			vY = ((int)(Math.random()*2))*2-1;
		}
	}
	
	public String getID(){
		return "Rabbit-" + this.ID;
	}
	public void setRabbitGrassSpace(RabbitsGrassSimulationSpace space){
		this.rgSpace = space;
	}
	
	public void draw(SimGraphics g) {
		g.drawOval(Color.white);
	}

	public int getX() {
		return this.x;
	}

	public int getY() {
		return this.y;
	}

	public void setXY(int newX, int newY){
		this.x = newX;
		this.y = newY;
	}

	public void step() {
		
		int newX = x + vX;
		int newY = y + vY;
		
		Object2DGrid grid = rgSpace.getCurrentRabbitSpace();
		
		/*
		 * different from original code
		 */
		newX = (newX + grid.getSizeX()) % grid.getSizeX();
		newY = (newY + grid.getSizeY()) % grid.getSizeY();
		
		if(tryMove(newX, newY)){
			this.energy += rgSpace.eatEnergyAt(x, y);
		} 
		
		/*
		 * different from original code
		 */
		this.setDirection();
		
		/*
		 * Need to be commented in report. We consider trying 
		 * also consumes energy.
		 */
		this.energy -= stepEnergyLoss; 
		
	}

	private boolean tryMove(int newX, int newY) {
		return rgSpace.moveRabbitAt(x, y, newX, newY);
	}

	public int getEnergy() {
		return this.energy;
	}

	public void setEnergy(int energy) {
		this.energy = energy;
	}

}
