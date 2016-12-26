import java.awt.Color;
import java.util.ArrayList;


import uchicago.src.sim.engine.BasicAction;
import uchicago.src.sim.engine.Schedule;
import uchicago.src.sim.engine.SimInit;
import uchicago.src.sim.engine.SimModelImpl;
import uchicago.src.sim.gui.ColorMap;
import uchicago.src.sim.gui.DisplaySurface;
import uchicago.src.sim.gui.Object2DDisplay;
import uchicago.src.sim.gui.Value2DDisplay;
import uchicago.src.sim.util.SimUtilities;
import uchicago.src.sim.analysis.OpenSequenceGraph;
import uchicago.src.reflector.RangePropertyDescriptor;
import uchicago.src.sim.analysis.DataSource;
import uchicago.src.sim.analysis.Sequence;

/**
 * Class that implements the simulation model for the rabbits grass simulation.
 * This is the first class which needs to be setup in order to run Repast
 * simulation. It manages the entire RePast environment and the simulation.
 *
 * @author Jinbu Liu & Mengjie Zhao
 */

public class RabbitsGrassSimulationModel extends SimModelImpl {

	/*
	 * default values
	 */
	private static final int INITIAL_RABBITS = 150;
	private static final int SPACEXSIZE = 20;
	private static final int SPACEYSIZE = 20;
	private static final int INITIAL_GRASS = 100;
	private static final int GRASS_GROWTH_RATE = 15;
	private static final int RABBIT_MAX_ENERGY = 15;
	private static final int STEP_ENERGY_LOSS = 1;
	private static final int GRASS_ENERGY = 5;
	
	/*
	 * model parameters
	 */
	private int numRabbit = INITIAL_RABBITS;
	private int numGrass = INITIAL_GRASS;
	private int spaceXSize = SPACEXSIZE;
	private int spaceYSize = SPACEYSIZE;
	private int rabbitMaxEnergy = RABBIT_MAX_ENERGY;
	private int energyLoss = STEP_ENERGY_LOSS;
	private int growthRate = GRASS_GROWTH_RATE;
	private int grassEnergy = GRASS_ENERGY;
	
	/*
	 * modules of the Model
	 */
	private Schedule schedule;
	private RabbitsGrassSimulationSpace rgSpace;
	private DisplaySurface displaySurf;
	private ArrayList<RabbitsGrassSimulationAgent> rabbitsList;
	private OpenSequenceGraph amountOfGrassInSpace;
		
	class grassInSpace implements DataSource, Sequence{
		public Object execute(){
			return new Double(getSValue());
		}
		public double getSValue(){
			return (double) rgSpace.getTotalGrass();
		}
	}
	class rabbitsInSpace implements DataSource, Sequence{
		public Object execute(){
			return new Double(getSValue());
		}
		public double getSValue(){
			return (double) getTotalRabbits();
		}
	}
	
	public static void main(String[] args) {

		/*
		 * load the model
		 */
		SimInit init = new SimInit();
		RabbitsGrassSimulationModel model = new RabbitsGrassSimulationModel();
		init.loadModel(model, "", false);

	}

	public void setup() {
		//System.out.println("Running Setup");
		
		this.rgSpace = null;
		this.rabbitsList = new ArrayList<RabbitsGrassSimulationAgent>();
		this.schedule = new Schedule();
		
		/*
		 * add slider bar
		 */
		addParameterSlider();
		
		/*
		 * Initialize and register the display surface, if it already exists, dispose it
		 */
		if(this.displaySurf != null)
			this.displaySurf.dispose();	
		this.displaySurf = null;
		
		if(this.amountOfGrassInSpace!=null)
			this.amountOfGrassInSpace.dispose();		
		this.amountOfGrassInSpace = null;
			
		this.displaySurf = new DisplaySurface(this, "Rabbits and Grass Window");
		this.amountOfGrassInSpace = new OpenSequenceGraph("Amout of Grass and Living Rabbits in Space", this);
		
		this.registerDisplaySurface("Rabbits and Grass Window", this.displaySurf);
		this.registerMediaProducer("Plot", this.amountOfGrassInSpace);

	}

	@SuppressWarnings("unchecked")
	private void addParameterSlider() {
		RangePropertyDescriptor numRabbitSlider = new RangePropertyDescriptor("NumRabbit", 0, 1000, 200);
		this.descriptors.put("NumRabbit", numRabbitSlider);
		RangePropertyDescriptor numGrassSlider = new RangePropertyDescriptor("NumGrass", 0, 1000, 200);
		this.descriptors.put("NumGrass", numGrassSlider);
		
		RangePropertyDescriptor xSizeSlider = new RangePropertyDescriptor("SpaceXSize", 0, 500, 100);
		this.descriptors.put("SpaceXSize", xSizeSlider);
		RangePropertyDescriptor ySizeSlider = new RangePropertyDescriptor("SpaceYSize", 0, 500, 100);
		this.descriptors.put("SpaceYSize", ySizeSlider);
		
		RangePropertyDescriptor grassEnergySlider = new RangePropertyDescriptor("GrassEnergy", 0, 50, 10);
		this.descriptors.put("GrassEnergy", grassEnergySlider);
		RangePropertyDescriptor lossEnergySlider = new RangePropertyDescriptor("EnergyLoss", 0, 50, 10);
		this.descriptors.put("EnergyLoss", lossEnergySlider);
		RangePropertyDescriptor maxEnergySlider = new RangePropertyDescriptor("RabbitMaxEnergy", 0, 50, 10);
		this.descriptors.put("RabbitMaxEnergy", maxEnergySlider);
		
		RangePropertyDescriptor growthRateSlider = new RangePropertyDescriptor("GrowthRate", 0, 300, 50);
		this.descriptors.put("GrowthRate", growthRateSlider);
	}

	public Schedule getSchedule() {
		return this.schedule;
	}

	public void begin() {
		buildModel();
		buildSchedule();
		buildDisplay();
		
		this.displaySurf.display();
		this.amountOfGrassInSpace.display();
	}

	private void buildSchedule() {
	
		class RabbitGrassStep extends BasicAction{

			@Override
			public void execute() {
				
				growGrass();
				
				SimUtilities.shuffle(rabbitsList);
				for(RabbitsGrassSimulationAgent rabbit: rabbitsList){
					rabbit.step();
				}
				
				reapDeadRabbit();
				reProduceRabbit();
				
				displaySurf.updateDisplay();

			}
		}
		
		schedule.scheduleActionBeginning(0, new RabbitGrassStep());
		
		class RabbitGrassCountLiving extends BasicAction{
			@Override
			public void execute() {
				countLivingRabbits();			
			}			
		}
		schedule.scheduleActionAtInterval(3, new RabbitGrassCountLiving());
		
		class RabbitGrassUpdateGrassInSpace extends BasicAction{		
			@Override
			public void execute(){
				amountOfGrassInSpace.step();
			}
		}
		schedule.scheduleActionBeginning(0, new RabbitGrassUpdateGrassInSpace());
	}

	public void growGrass() {
		this.rgSpace.spreadGrass(this.growthRate);
	}

	public void reProduceRabbit() {
		int currRabbit = rabbitsList.size();
		for(int i=0; i<currRabbit; i++){
			RabbitsGrassSimulationAgent rabbit = rabbitsList.get(i);
			if(rabbit.getEnergy()>this.rabbitMaxEnergy){
				int childEnergy = rabbit.getEnergy()-this.rabbitMaxEnergy;
				rabbit.setEnergy(this.rabbitMaxEnergy);
				this.addNewRabbit(childEnergy);
			}
		}
	}

	public int reapDeadRabbit() {
		int count = 0;
		for(int i=(rabbitsList.size()-1); i>=0; i--){
			RabbitsGrassSimulationAgent rabbit = rabbitsList.get(i);
			if(rabbit.getEnergy()<1){
				rgSpace.removeRabbitAt(rabbit.getX(), rabbit.getY());
				rabbitsList.remove(i);
				
				count += 1;
			}
		}
		
		return count;
	}

	public int countLivingRabbits() {
		int livingRabbits = 0;
		for(RabbitsGrassSimulationAgent rabbit: this.rabbitsList){
			if(rabbit.getEnergy()>0)
				livingRabbits += 1;
		}
		
		return livingRabbits;
	}

	private void buildDisplay() {
		
		/*
		 * setup the grass color map: black for no grass, green for grass
		 */
		ColorMap map = new ColorMap();
		map.mapColor(1, Color.green);
		map.mapColor(0, Color.black);
		Value2DDisplay displayGrass = new Value2DDisplay(this.rgSpace.getCurrentGrassSpace(), map);
		
		/*
		 * setup the display of rabbit agents
		 */
		Object2DDisplay displayRabbits = new Object2DDisplay(rgSpace.getCurrentRabbitSpace());
		displayRabbits.setObjectList(rabbitsList);
		
		this.displaySurf.addDisplayable(displayGrass, "Grass");
		this.displaySurf.addDisplayable(displayRabbits, "Rabbit");
		
		this.amountOfGrassInSpace.addSequence("Grass In Space", new grassInSpace());
		this.amountOfGrassInSpace.addSequence("Living rabbits In Space", new rabbitsInSpace());
	}

	private void buildModel() {
		
		this.rgSpace = new RabbitsGrassSimulationSpace(spaceXSize, spaceYSize);
		this.rgSpace.spreadGrass(numGrass);
		this.rgSpace.setGrassEnergy(grassEnergy);
				
		for(int i=0; i<this.numRabbit; i++){
			//addNewRabbit((int)(Math.random()*this.rabbitMaxEnergy));
			addNewRabbit(10);
		}

	}

	private void addNewRabbit(int initialEnergy) {
		RabbitsGrassSimulationAgent rabbit = new RabbitsGrassSimulationAgent(
				initialEnergy, this.energyLoss);

		/*
		 * different from repast to save space.
		 */
		if(this.rgSpace.addRabbit(rabbit))
			this.rabbitsList.add(rabbit);

	}
	public int getTotalRabbits(){
		int totalAlivingRabbits=0;
		for(RabbitsGrassSimulationAgent rabbit : this.rabbitsList){
			if (rabbit.getEnergy()>0){
				totalAlivingRabbits++;
			}
		}
		return totalAlivingRabbits;
	}

	public String[] getInitParam() {
		String[] initParams = { "NumRabbit", "SpaceXSize", "SpaceYSize", "NumGrass", "RabbitMaxEnergy", "EnergyLoss",
				"GrowthRate", "GrassEnergy"};
		return initParams;
	}

	public String getName() {
		return "Rabbits and Grass Simulation";
	}

	public int getNumRabbit() {
		return numRabbit;
	}

	public void setNumRabbit(int numRabbit) {
		this.numRabbit = numRabbit;
	}

	public int getSpaceXSize() {
		return spaceXSize;
	}

	public void setSpaceXSize(int spaceXSize) {
		this.spaceXSize = spaceXSize;
	}

	public int getSpaceYSize() {
		return spaceYSize;
	}

	public void setSpaceYSize(int spaceYSize) {
		this.spaceYSize = spaceYSize;
	}

	public int getNumGrass() {
		return numGrass;
	}

	public void setNumGrass(int numGrass) {
		this.numGrass = numGrass;
	}

	public int getRabbitMaxEnergy() {
		return rabbitMaxEnergy;
	}

	public void setRabbitMaxEnergy(int rabbitMaxEnergy) {
		this.rabbitMaxEnergy = rabbitMaxEnergy;
	}

	public int getEnergyLoss() {
		return energyLoss;
	}

	public void setEnergyLoss(int energyLoss) {
		this.energyLoss = energyLoss;
	}

	public int getGrowthRate() {
		return growthRate;
	}

	public void setGrowthRate(int growthRate) {
		this.growthRate = growthRate;
	}

	public int getGrassEnergy() {
		return grassEnergy;
	}

	public void setGrassEnergy(int grassEnergy) {
		this.grassEnergy = grassEnergy;
	}
	
	
}
