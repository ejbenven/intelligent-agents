

/**
 * Class that implements the simulation model for the rabbits grass
 * simulation.  This is the first class which needs to be setup in
 * order to run Repast simulation. It manages the entire RePast
 * environment and the simulation.
 *
 * @author
 */


//public class RabbitsGrassSimulationModel extends SimModelImpl {
import java.awt.Color;
import java.util.ArrayList;

import uchicago.src.sim.analysis.BinDataSource;
import uchicago.src.sim.analysis.DataSource;
import uchicago.src.sim.analysis.OpenHistogram;
import uchicago.src.sim.analysis.OpenSequenceGraph;
import uchicago.src.sim.analysis.Sequence;
import uchicago.src.sim.engine.BasicAction;
import uchicago.src.sim.engine.Schedule;
import uchicago.src.sim.engine.SimInit;
import uchicago.src.sim.engine.SimModelImpl;
import uchicago.src.sim.gui.DisplaySurface;
import uchicago.src.sim.gui.ColorMap;
import uchicago.src.sim.gui.Object2DDisplay;
import uchicago.src.sim.gui.Value2DDisplay;
import uchicago.src.sim.util.SimUtilities;

public class RabbitsGrassSimulationModel extends SimModelImpl {
  // Default Values
  private static final int NUMAGENTS = 100;
  private static final int WORLDXSIZE = 20;
  private static final int WORLDYSIZE = 20;
  private static final int GRASSGROWTH = 1000;
	private static final int AGENT_REPRODUCTION_COST = 10;
	private static final int AGENT_REPRODUCTION_THRESHOLD = 50;
	private static final int AGENT_ENERGY_AT_BIRTH = 20;

	private int numAgents = NUMAGENTS;
	private int worldXSize = WORLDXSIZE;
	private int worldYSize = WORLDYSIZE;
	private int grassGrowth = GRASSGROWTH;
	private int grass = 0;
	private int agentReproductionCost = AGENT_REPRODUCTION_COST;
	private int agentReproductionThreshold = AGENT_REPRODUCTION_THRESHOLD;
	private int agentEnergyAtBirth = AGENT_ENERGY_AT_BIRTH;

  private Schedule schedule;

  private RabbitsGrassSimulationSpace cdSpace;

  private ArrayList agentList;

  private DisplaySurface displaySurf;

  private OpenSequenceGraph amountOfgrassInSpace;
  private OpenHistogram agentWealthDistribution;

  class grassInSpace implements DataSource, Sequence {

    public Object execute() {
      return new Double(getSValue());
    }

    public double getSValue() {
      return (double)cdSpace.getTotalgrass();
    }
  }

  class agentgrass implements BinDataSource{
    public double getBinValue(Object o) {
      RabbitsGrassSimulationAgent cda = (RabbitsGrassSimulationAgent)o;
      return (double)cda.getgrass();
    }
  }

  public String getName(){
    return "Carry And Drop";
  }

  public void setup(){
    System.out.println("Running setup");
    cdSpace = null;
    agentList = new ArrayList();
    schedule = new Schedule(1);

    // Tear down Displays
    if (displaySurf != null){
      displaySurf.dispose();
    }
    displaySurf = null;

    if (amountOfgrassInSpace != null){
      amountOfgrassInSpace.dispose();
    }
    amountOfgrassInSpace = null;

    if (agentWealthDistribution != null){
      agentWealthDistribution.dispose();
    }
    agentWealthDistribution = null;

    // Create Displays
    displaySurf = new DisplaySurface(this, "Carry Drop Model Window 1");
    amountOfgrassInSpace = new OpenSequenceGraph("Amount Of grass In Space",this);
    agentWealthDistribution = new OpenHistogram("Agent Wealth", 8, 0);

    // Register Displays
    registerDisplaySurface("Carry Drop Model Window 1", displaySurf);
    this.registerMediaProducer("Plot", amountOfgrassInSpace);
  }

  public void begin(){
    buildModel();
    buildSchedule();
    buildDisplay();

    displaySurf.display();
    amountOfgrassInSpace.display();
    agentWealthDistribution.display();
  }

  public void buildModel(){
    System.out.println("Running BuildModel");
    cdSpace = new RabbitsGrassSimulationSpace(worldXSize, worldYSize);
    cdSpace.spreadgrass(grassGrowth);

    for(int i = 0; i < numAgents; i++){
      addNewAgent();
    }
    for(int i = 0; i < agentList.size(); i++){
      RabbitsGrassSimulationAgent cda = (RabbitsGrassSimulationAgent)agentList.get(i);
      cda.report();
    }
  }

  public void buildSchedule(){
    System.out.println("Running BuildSchedule");

    class RabbitsGrassSimulationStep extends BasicAction {
      public void execute() {
				cdSpace.spreadgrass(grassGrowth);
        SimUtilities.shuffle(agentList);
        for(int i =0; i < agentList.size(); i++){
          RabbitsGrassSimulationAgent cda = (RabbitsGrassSimulationAgent)agentList.get(i);
          cda.step();
        }
				reapDeadAgents();
        int birthAgents = reproductionAgents();
        for(int i =0; i < birthAgents; i++){
          addNewAgent();
        }

        displaySurf.updateDisplay();
      }
    }

    schedule.scheduleActionBeginning(0, new RabbitsGrassSimulationStep());

    class RabbitsGrassSimulationCountLiving extends BasicAction {
      public void execute(){
        countLivingAgents();
      }
    }

    schedule.scheduleActionAtInterval(10, new RabbitsGrassSimulationCountLiving());

    class RabbitsGrassSimulationUpdategrassInSpace extends BasicAction {
      public void execute(){
        amountOfgrassInSpace.step();
      }
    }

    schedule.scheduleActionAtInterval(10, new RabbitsGrassSimulationUpdategrassInSpace());

    class RabbitsGrassSimulationUpdateAgentWealth extends BasicAction {
      public void execute(){
        agentWealthDistribution.step();
      }
    }

    schedule.scheduleActionAtInterval(10, new RabbitsGrassSimulationUpdateAgentWealth());
  }

  public void buildDisplay(){
    System.out.println("Running BuildDisplay");

    ColorMap map = new ColorMap();

    for(int i = 1; i<16; i++){
      map.mapColor(i, new Color((int)(i * 8 + 127), 0, 0));
    }
    map.mapColor(0, Color.white);

    Value2DDisplay displaygrass =
        new Value2DDisplay(cdSpace.getCurrentgrassSpace(), map);

    Object2DDisplay displayAgents = new Object2DDisplay(cdSpace.getCurrentAgentSpace());
    displayAgents.setObjectList(agentList);

    displaySurf.addDisplayableProbeable(displaygrass, "grass");
    displaySurf.addDisplayableProbeable(displayAgents, "Agents");

    amountOfgrassInSpace.addSequence("grass In Space", new grassInSpace());
    agentWealthDistribution.createHistogramItem("Agent Wealth",agentList,new agentgrass());

  }

  private void addNewAgent(){
    RabbitsGrassSimulationAgent a = new RabbitsGrassSimulationAgent(agentReproductionCost, agentReproductionThreshold, agentEnergyAtBirth);
    agentList.add(a);
    cdSpace.addAgent(a);
  }

  private void reapDeadAgents(){
    for(int i = (agentList.size() - 1); i >= 0 ; i--){
      RabbitsGrassSimulationAgent cda = (RabbitsGrassSimulationAgent)agentList.get(i);
      if(cda.getgrass() < 1){
        cdSpace.removeAgentAt(cda.getX(), cda.getY());
        agentList.remove(i);
      }
    }
  }

	private int reproductionAgents(){
		int count = 0;
		for(int i = (agentList.size() - 1); i >= 0 ; i--){
			RabbitsGrassSimulationAgent cda = (RabbitsGrassSimulationAgent)agentList.get(i);
			if(cda.getgrass() >= agentReproductionThreshold){
				cda.givebirth(agentReproductionCost);
				count++;
			}
		}
		return count;
	}

  private int countLivingAgents(){
    int livingAgents = 0;
    for(int i = 0; i < agentList.size(); i++){
      RabbitsGrassSimulationAgent cda = (RabbitsGrassSimulationAgent)agentList.get(i);
      if(cda.getgrass() > 0) livingAgents++;
    }
    System.out.println("Number of living agents is: " + livingAgents);

    return livingAgents;
  }

  public Schedule getSchedule(){
    return schedule;
  }

  public String[] getInitParam(){
    String[] initParams = { "NumAgents", "WorldXSize", "WorldYSize", "grass Growth", "agentReproductionCost", "agentReproductionThreshold"};
    return initParams;
  }

  public int getNumAgents(){
    return numAgents;
  }

  public void setNumAgents(int na){
    numAgents = na;
  }

  public int getWorldXSize(){
    return worldXSize;
  }

  public void setWorldXSize(int wxs){
    worldXSize = wxs;
  }

  public int getWorldYSize(){
    return worldYSize;
  }

  public void setWorldYSize(int wys){
    worldYSize = wys;
  }

  public int getgrassGrowth() {
    return grassGrowth;
  }

  public void setgrassGrowth(int i) {
    grassGrowth = i;
  }

  public int getagentReproductionThreshold() {
    return agentReproductionThreshold;
  }

  public int getagentReproductionCost() {
    return agentReproductionCost;
  }

  public void setagentReproductionThreshold(int i) {
    agentReproductionThreshold = i;
  }

  public void setagentReproductionCost(int i) {
    agentReproductionCost = i;
  }

  public static void main(String[] args) {
    SimInit init = new SimInit();
    RabbitsGrassSimulationModel model = new RabbitsGrassSimulationModel();
    init.loadModel(model, "", false);
  }

}
