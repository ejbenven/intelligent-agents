package template;

//the list of imports
import java.util.ArrayList;
import java.util.List;
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
        
//		System.out.println("Agent " + agent.id() + " has tasks " + tasks);
        Plan planVehicle1 = naivePlan(vehicles.get(0), tasks);

        List<Plan> plans = new ArrayList<Plan>();
        plans.add(planVehicle1);
        while (plans.size() < vehicles.size()) {
            plans.add(Plan.EMPTY);
        }
        
        long time_end = System.currentTimeMillis();
        long duration = time_end - time_start;
        System.out.println("The plan was generated in "+duration+" milliseconds.");
        
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
        
        State state = new State(vehicles.get(0).getCurrentCity(), tasks, vehicles.get(0));
        states.add(state);
        for (Vehicle vehicle : vehicles.subList(1, vehicles.size())){
            states.add(new State(vehicle.getCurrentCity(), new ArrayList<Task>(), vehicle));
        }
        
        double bestCost = computeCost(states);
        double newCost;
        Random rand = new Random();
        int ind1;
        int ind2;
        State state1;
        State state2;
        Task task;
        List<State> oldStates = new ArrayList<State>();

        for(int i = 0; i < 1000; i++){
            //We have 2 way of generating a new neighbourhood
            oldStates.clear();
            for (State state_ : states){
                oldStates.add(state_);
            }

            if (rand.nextInt(2) == 0){
                //Shuffle
                while(true) {
                    ind1 = rand.nextInt(states.size());
                    state1 = states.get(ind1);
                    if (state1.shuffle()){
                        states.add(ind1, state1);
                        break;
                    }
                }
                
            } else {
                //transfer a task
                while(true) {
                    ind1 = rand.nextInt(states.size());
                    ind2 = rand.nextInt(states.size());
                    if(ind1 == ind2)
                        continue;

                    state1 = states.get(ind1);
                    state2 = states.get(ind2);
                    if(state1.getTasks() == null)
                        continue;
                    
                    ind1 = rand.nextInt(state1.getTasks().size());
                    task = state1.getTasks().get(ind1);

                    if (state1.removeTask(task)){
                        if (state2.addTask(task)){
                            states.add(ind1,state1);
                            states.add(ind2,state2);
                            break;
                        }
                    }
                }
            }
            newCost = computeCost(states);

            //We take the new states only if it's a better one
            if(newCost > bestCost){
                states.clear();
                for (State state_: oldStates){
                    states.add(state_);
                }
            }
                

        }

       return states; 
    }

    private double computeCost (List<State> states){
        double cost = 0;

        for (State state : states)
            cost += state.getCost();

        return cost;
    }

    //TODO
    private Plan stateToPlan(State state) {
        Plan plan = new Plan(state.getCurrentCity());
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
