package template;

import java.util.Random;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;

import logist.simulation.Vehicle;
import logist.agent.Agent;
import logist.behavior.ReactiveBehavior;
import logist.plan.Action;
import logist.plan.Action.Move;
import logist.plan.Action.Pickup;
import logist.task.Task;
import logist.task.TaskDistribution;
import logist.topology.Topology;
import logist.topology.Topology.City;

public class ReactiveTemplate implements ReactiveBehavior {

    private Random random;
    private double pPickup;
    private int numActions;
    private Agent myAgent;

    //Q(s,a)
    private HashMap<AgentMove,Double> Q = new HashMap<AgentMove,Double>();
    //Best move
    private HashMap<State, AgentMove> bestMove = new HashMap<State,AgentMove>();
    //V(s)
    private HashMap<State,Double> V = new HashMap<State,Double>();

    @Override
    public void setup(Topology topology, TaskDistribution td, Agent agent) {

        // Reads the discount factor from the agents.xml file.
        // If the property is not present it defaults to 0.95
        Double discount = agent.readProperty("discount-factor", Double.class,
                                             0.95);

        this.random = new Random();
        this.pPickup = discount;
        this.numActions = 0;
        this.myAgent = agent;


        List<Boolean> bTask = new ArrayList();
        bTask.add(true);
        bTask.add(false);

        State state;
        AgentMove move;
        //We initialize V
        for (City sCity : topology.cities()) {
            //Case where there is no package
            state = new State(sCity,null,false);
            this.V.put(state,(double) 0);
            for (City eCity : topology.cities()) {
                if (sCity.equals(eCity)) {
                    continue;
                }

                state = new State(sCity,eCity,true);
                this.V.put(state,(double) 0);

                move = new AgentMove(sCity,eCity,false);
                bestMove.put(state,move);

            }
        }

        HashMap<State,Double> V_;
        AgentMove pmove;
        double cost;
        double reward;
        double memory;
        double gain;
        State futureState;
        int i = 0;
        do {
            i++;
            V_ = new HashMap<State,Double>(V);
            this.V.clear();

            //Loop over all the states
            for (City sCity : topology.cities()) {
                for (City eCity : topology.cities()) {
                    if (sCity.equals(eCity)) {
                        continue;
                    }
                    for (Boolean isTask : bTask) {
                        if (isTask) {
                            state = new State(sCity,eCity,isTask);
                        } else {
                            state = new State(sCity,null,isTask);
                        }

                        //Loop over all the actions
                        for (City destCity : topology.cities()) {
                            if (destCity.equals(sCity)) {
                                continue;
                            }
                            //compute cost and future expected benefits
                            cost = agent.vehicles().get(0).costPerKm();
                            cost *= sCity.distanceTo(destCity);
                            cost *= (-1);
                            //memory
                            memory = 0;
                            for (City nextDest : topology.cities()) {
                                if (nextDest.equals(destCity)) {
                                    continue;
                                }
                                futureState = new State(destCity,nextDest,true);
                                memory += td.probability(destCity,nextDest) * V_.get(futureState);
                            }
                            futureState = new State(destCity,null,false);
                            memory += td.probability(destCity,null) * V_.get(futureState);
                            memory *= discount;

                            gain = cost + memory;

                            //If there is a package for destCity we have the
                            //option to pick it up
                            if (isTask && destCity.equals(eCity)) {
                                pmove = new AgentMove(sCity,destCity,true);
                                if (sCity.hasNeighbor(destCity)){
                                    move = new AgentMove(sCity,destCity,false);
                                    Q.put(move,gain);
                                }

                                //Reward for carrying the payload
                                reward = td.reward(sCity,destCity);

                                Q.put(pmove,gain+reward);

                            } else if (!sCity.hasNeighbor(destCity)){
                                continue;
                            } else {
                                move = new AgentMove(sCity,destCity,false);

                                Q.put(move,gain);
                            }


                        }


                        AgentMove maxEntry = indMaxQ(Q);
                        this.V.put(state,Q.get(maxEntry));
                        bestMove.put(state,maxEntry);
                        Q.clear();
                    }
                }
            }
        } while(noConvergence(V,V_));
    }

    private AgentMove indMaxQ(HashMap<AgentMove,Double> Q) {
        AgentMove maxEntry = null;

        for (AgentMove entry : Q.keySet()) {
            if (maxEntry == null || Q.get(entry) - Q.get(maxEntry)>0) {
                maxEntry = entry;
            }
        }
        return maxEntry;
    }

    private boolean noConvergence(HashMap<State,Double> V,HashMap<State,Double> V_) {
        double epsilon = 0.1;
        boolean convergence = false;

        for (State entry : V.keySet()) {
            if (Math.abs(V.get(entry) - V_.get(entry)) > epsilon) {
                convergence = true;
                break;
            }
        }

        return convergence;
    }

    @Override
    public Action act(Vehicle vehicle, Task availableTask) {
        Action action;
        State state;
        
        if (availableTask == null) {
            state = new State(vehicle.getCurrentCity(),null,false);
        } else {
            state = new State(vehicle.getCurrentCity(),availableTask.deliveryCity,true);
        }

        AgentMove agentMove = bestMove.get(state);
        if (availableTask != null && agentMove.getpickup()) {
            action = new Pickup(availableTask);
        } else {
            action = new Move(agentMove.getEndCity());
        }

        if (numActions == 6000) {
            System.out.println("The total profit after "+numActions+" actions is "+myAgent.getTotalProfit()+" (average profit: "+(myAgent.getTotalProfit() / (double)numActions)+")");
        }
        numActions++;

        return action;
    }
}
