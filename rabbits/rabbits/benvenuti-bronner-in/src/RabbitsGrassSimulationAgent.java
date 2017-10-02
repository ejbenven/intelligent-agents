import uchicago.src.sim.gui.Drawable;
import uchicago.src.sim.gui.SimGraphics;


/**
 * Class that implements the simulation agent for the rabbits grass simulation.

 * @benvenuti, bronner
 */

//public class RabbitsGrassSimulationAgent implements Drawable {
import java.awt.Color;

import uchicago.src.sim.gui.Drawable;
import uchicago.src.sim.gui.SimGraphics;
import uchicago.src.sim.space.Object2DGrid;

//public class RabbitsGrassSimulationAgent implements Drawable{
public class RabbitsGrassSimulationAgent implements Drawable {
  private int x;
  private int y;
  private int vX;
  private int vY;
  private int grass;
  private static int IDNumber = 0;
  private int ID;
  private RabbitsGrassSimulationSpace cdSpace;

  //public RabbitsGrassSimulationAgent(int agentReproductionCost, int agentReproductionThreshold){
	public RabbitsGrassSimulationAgent(int agentReproductionCost, int agentReproductionThreshold, int agentEnergyAtBirth){
    x = -1;
    y = -1;
    grass = agentEnergyAtBirth;
    setVxVy();
    IDNumber++;
    ID = IDNumber;
  }

  private void setVxVy(){
    vX = 0;
    vY = 0;
		int direction = (int)Math.floor(Math.random() * 4);
		switch (direction){
			case 0: vX =-1; vY = 0;
				break;
			case 1: vX =0; vY = 1;
				break;
			case 2: vX = 1; vY = 0;
				break;
			case 3: vX =0 ; vY = -1;
				break;

		}
		//System.out.println("Speed Vx " + vX + "Vy" + vY);
  }

  public void setXY(int newX, int newY){
    x = newX;
    y = newY;
  }

  public void setRabbitsGrassSimulationSpace(RabbitsGrassSimulationSpace cds){
    cdSpace = cds;
  }

  public String getID(){
    return "A-" + ID;
  }

  public int getgrass(){
    return grass;
  }
	public void givebirth(int cost){
    grass -= cost;
  }


  public void report(){
    System.out.println(getID() +
                       " at " +
                       x + ", " + y +
                       " has " +
                       getgrass() + " energy");
  }

  public int getX(){
    return x;
  }

  public int getY(){
    return y;
  }

  public void draw(SimGraphics G){
      G.drawFastRoundRect(Color.green);
  }

  public void step(){
		setVxVy();
    int newX = x + vX;
    int newY = y + vY;

    Object2DGrid grid = cdSpace.getCurrentAgentSpace();
    newX = (newX + grid.getSizeX()) % grid.getSizeX();
    newY = (newY + grid.getSizeY()) % grid.getSizeY();

    if(tryMove(newX, newY)){
        x = newX;
        y = newY;
      grass += cdSpace.takegrassAt(x, y);
    }
    else{
      setVxVy();
    }
    grass--;
  }

  private boolean tryMove(int newX, int newY){
    return cdSpace.moveAgentAt(x, y, newX, newY);
  }

}
