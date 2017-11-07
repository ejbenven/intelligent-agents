package template;

//the list of imports
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.HashSet;
import java.util.Random;
import logist.LogistSettings;

import logist.Measures;
import logist.behavior.AuctionBehavior;
import logist.behavior.CentralizedBehavior;
import logist.agent.Agent;
import logist.config.Parsers;
import logist.simulation.Vehicle;
import logist.plan.Plan;
import logist.task.Task;
import logist.task.TaskDistribution;
import logist.task.TaskSet;
import logist.topology.Topology;
import logist.topology.Topology.City;
import java.lang.Math;

/**
 * A very simple auction agent that assigns all tasks to its first vehicle and
 * handles them sequentially.
 *
 */
@SuppressWarnings("unused")
public class CentralizedTemplate implements CentralizedBehavior {

    private Topology topology;
    private TaskDistribution distribution;
    private Agent agent;
    private long timeout_setup;
    private long timeout_plan;
    private double p;
    private int nbTasks;

    
    @Override
    public void setup(Topology topology, TaskDistribution distribution,
            Agent agent) {
        
        // this code is used to get the timeouts
        LogistSettings ls = null;
        try {
            ls = Parsers.parseSettings("config/settings_default.xml");
        }
        catch (Exception exc) {
            System.out.println("There was a problem loading the configuration file.");
        }
        p = agent.readProperty("p",Double.class, 0.3);
        // the setup method cannot last more than timeout_setup milliseconds
        timeout_setup = ls.get(LogistSettings.TimeoutKey.SETUP);
        // the plan method cannot execute more than timeout_plan milliseconds
        timeout_plan = ls.get(LogistSettings.TimeoutKey.PLAN);
        
        this.topology = topology;
        this.distribution = distribution;
        this.agent = agent;
    }

    @Override
    public List<Plan> plan(List<Vehicle> vehicles, TaskSet tasks) {
        long time_start = System.currentTimeMillis();
        
        List<State> states = COP(vehicles, tasks);
        List<Plan> plans = new ArrayList<Plan>();

        for (State state : states)
            plans.add(stateToPlan(state));

        long time_end = System.currentTimeMillis();
        long duration = time_end - time_start;
        double cost=  computeCost(states);
        System.out.println("The plan was generated in "+duration+" milliseconds.");
        System.out.println("The cost is "+cost);

        
        return plans;
    }

    private List<State> COP (List<Vehicle> vehicles, TaskSet ts) {
        //Initialisation
        List<State> states = new ArrayList<State>();        
        List<Task> tasks = new ArrayList<Task>();
        for (Task task : ts) {
            tasks.add(task);
            tasks.add(task);
        }
        nbTasks = tasks.size();
        //Find biggest truck
        int ind = 0;
        int bestCapa = 0;
        for (Vehicle vehicle : vehicles){
            if (vehicle.capacity()>bestCapa){
                bestCapa = vehicle.capacity();
                ind = vehicles.indexOf(vehicle);
            }
        }

        for (Vehicle vehicle : vehicles){
            if(vehicles.indexOf(vehicle) != ind)
                states.add(new State(vehicle.getCurrentCity(), new ArrayList<Task>(), vehicle));
            else
                states.add(new State(vehicle.getCurrentCity(), tasks, vehicle));
        }
        
        
        //List<State> oldStates = new ArrayList<State>();

        for(int i = 0; i < java.lang.Math.pow(10,6); i++){
            //We have 2 way of generating a new neighbourhood
            //oldStates.clear();
            //for (State state_ : states){
            //    oldStates.add(state_);
            //}
            
            states = chooseNeighboors(states);
            
            //newCost = computeCost(states);

        }

       return states; 
    }

    private List<State> chooseNeighboors(List<State> states)
    {
        List<State> stateShuffle = new ArrayList<State>();
        List<State> stateSwap = new ArrayList<State>();
        int ind1, ind2, indt;
        State state1, state2;
        Task task;
        double oldCost, shuffleCost, swapCost;
        Random rand = new Random();

        for (State state : states){
            stateShuffle.add(state);
            stateSwap.add(state);
        }

        //Shuffle
        while(true) {
            ind1 = rand.nextInt(states.size());
            state1 = states.get(ind1);
            if (state1.shuffle()){
                stateShuffle.remove(ind1);
                stateShuffle.add(ind1, state1);
                break;
            }
        }
        
        //transfer a task
        while(true) {
            ind1 = rand.nextInt(states.size());
            ind2 = rand.nextInt(states.size());
            if(ind1 == ind2)
                continue;

            state1 = states.get(ind1);
            state2 = states.get(ind2);
            if(state1.getTasks().isEmpty())
                continue;
            
            //If the vehicle carries few tasks, we will sometime go look for 
            //another vehicle to remove tasks from

            if(state1.getTasks().size()<(nbTasks/states.size()) && rand.nextDouble() < 0.5 ){
                continue;
            }

            indt = rand.nextInt(state1.getTasks().size());
            task = state1.getTasks().get(indt);

            if (state2.addTask(task)){
                if (state1.removeTask(task)){
                    stateSwap.remove(ind1);
                    stateSwap.add(ind1,state1);
                    stateSwap.remove(ind2);
                    stateSwap.add(ind2,state2);
                    break;
                } else {
                    System.out.println("Error: Added but not removed");
                }
            } 
        }

        shuffleCost = computeCost(stateShuffle);
        swapCost = computeCost(stateSwap);
        oldCost = computeCost(states);
        double draw = rand.nextDouble();
        if (draw < p){
            if (shuffleCost < swapCost && shuffleCost < oldCost)
                return stateShuffle;
            else if (swapCost < shuffleCost && swapCost < oldCost)
                return stateSwap;
            else if (oldCost < shuffleCost && oldCost < swapCost)
                return states;
            else if (oldCost == shuffleCost && oldCost != swapCost) {
                if (rand.nextDouble() < 0.5)
                    return states;
                else
                    return stateShuffle;
            } else if (oldCost == swapCost && oldCost != shuffleCost) {
                if (rand.nextDouble() < 0.5)
                    return states;
                else
                    return stateSwap;
            } else if (swapCost == shuffleCost && swapCost != oldCost) {
                if (rand.nextDouble() < 0.5)
                    return stateSwap;
                else
                    return stateShuffle;
            } else {
                if (rand.nextDouble() < 0.3)
                    return states;
                else if (rand.nextDouble() <0.5)
                    return stateShuffle;
                else
                    return stateSwap;
            }
        } else if (p < draw && draw < 2* p){
            if (rand.nextDouble() < 0.3)
                return states;
            else if (rand.nextDouble() < 0.5)
                return stateShuffle;
            else
                return stateSwap;
        } else {
            return states;
        }

    }

    private double computeCost (List<State> states){
        double cost = 0;

        for (State state : states)
            cost += state.getCost();

        return cost;
    }

    private Plan stateToPlan(State state) {
        Plan plan = new Plan(state.getCurrentCity());
        City currentCity = state.getCurrentCity();
        City destCity;
        boolean pickup;
        Set<Task> pickedTasks = new HashSet<Task>();

        for (Task task : state.getTasks()){
            if (pickedTasks.add(task)){
                destCity = task.pickupCity;
                pickup = true;
            } else {
                destCity = task.deliveryCity;
                pickup = false;
            }

            for (City city : currentCity.pathTo(destCity))
                plan.appendMove(city);

            if (pickup)
                plan.appendPickup(task);
            else
                plan.appendDelivery(task);

            currentCity = destCity;
        }
            
        return plan;
    }

    private Plan naivePlan(Vehicle vehicle, TaskSet tasks) {
        City current = vehicle.getCurrentCity();
        Plan plan = new Plan(current);

        for (Task task : tasks) {
            // move: current city => pickup location
            for (City city : current.pathTo(task.pickupCity)) {
                plan.appendMove(city);
            }

            plan.appendPickup(task);

            // move: pickup location => delivery location
            for (City city : task.path()) {
                plan.appendMove(city);
            }

            plan.appendDelivery(task);

            // set current city
            current = task.deliveryCity;
        }
        return plan;
    }
}
