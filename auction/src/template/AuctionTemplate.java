package template;

//the list of imports
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.HashSet;

import logist.LogistSettings;
import logist.config.Parsers;
import logist.Measures;
import logist.behavior.AuctionBehavior;
import logist.agent.Agent;
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
public class AuctionTemplate implements AuctionBehavior {

	private Topology topology;
	private TaskDistribution distribution;
	private Agent agent;
	private Random random;
	private Vehicle vehicle;
	private City currentCity;

        private long timeout_setup;
        private long timeout_plan;
        private long timeout_bid;
        private double p;

        private double currentCost;
        private List<State> currentStates;
        private Set<Task> ownedTasks;

	@Override
	public void setup(Topology topology, TaskDistribution distribution,
			Agent agent) {

		this.topology = topology;
		this.distribution = distribution;
		this.agent = agent;
		this.vehicle = agent.vehicles().get(0);
		this.currentCity = vehicle.homeCity();
                
                p = agent.readProperty("p", Double.class, 0.3);
                // the plan method cannot execute more than timeout_plan milliseconds
                LogistSettings ls = null;
                try {
                    ls = Parsers.parseSettings("config/settings_default.xml");
                }
                catch (Exception exc) {
                    System.out.println("There was a problem loading the configuration file.");
                }
                timeout_setup = ls.get(LogistSettings.TimeoutKey.SETUP);
                timeout_plan = ls.get(LogistSettings.TimeoutKey.PLAN);
                timeout_bid = ls.get(LogistSettings.TimeoutKey.BID);

                currentCost = 0;
                currentStates = new ArrayList<State>();
                ownedTasks = new HashSet<Task>();

		long seed = -9019554669489983951L * currentCity.hashCode() * agent.id();
		this.random = new Random(seed);
	}

        //TODO
	@Override
	public void auctionResult(Task previous, int winner, Long[] bids) {
		if (winner == agent.id()) {
			currentCity = previous.deliveryCity;
		}
	}
	
        //TODO
	@Override
	public Long askPrice(Task task) {

		if (vehicle.capacity() < task.weight)
			return null;

		long distanceTask = task.pickupCity.distanceUnitsTo(task.deliveryCity);
		long distanceSum = distanceTask
				+ currentCity.distanceUnitsTo(task.pickupCity);
		double marginalCost = Measures.unitsToKM(distanceSum
				* vehicle.costPerKm());

		double ratio = 1.0 + (random.nextDouble() * 0.05 * task.id);
		double bid = ratio * marginalCost;

		return (long) Math.round(bid);
	}

    
        @Override
        public List<Plan> plan(List<Vehicle> vehicles, TaskSet tasks) {
            long time_start = System.currentTimeMillis();
            Set<Task> tasks_ = tasksetToTasklist(tasks);
            List<State> states = COP(vehicles, tasks_, time_start);
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

        private List<State> COP (List<Vehicle> vehicles, Set<Task> ts, long time_start) {
            //Initialisation
            long duration;
            List<State> states = new ArrayList<State>();        
            List<Task> tasks = new ArrayList<Task>();
            for (Task task : ts) {
                tasks.add(task);
                tasks.add(task);
            }
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
            
            
            List<State> bestStates = new ArrayList<State>();
            for (State state : states)
                bestStates.add(state);
            double bestCost = computeCost(bestStates);
            double newCost;
            for(int i = 0; i < 1000000; i++){
                duration = System.currentTimeMillis() - time_start;
                if (duration >= 0.9*timeout_plan)
                    break;

                states = chooseNeighboors(states);
                newCost = computeCost(states);
                if (newCost < bestCost){
                    bestStates.clear();
                    for (State state : states)
                        bestStates.add(state);
                }

            }
            return bestStates; 
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
            if (states.size() > 1){
                while(true) {
                    ind1 = rand.nextInt(states.size());
                    ind2 = rand.nextInt(states.size());
                    if(ind1 == ind2)
                        continue;

                    state1 = states.get(ind1);
                    state2 = states.get(ind2);
                    if(state1.getTasks().isEmpty())
                        continue;
                    
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
            } else {
                stateSwap = stateShuffle;
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
            } else if (rand.nextDouble() < 0.5){
                return states;
            } else {
                if (rand.nextDouble() < 0.3)
                    return states;
                else if (rand.nextDouble() < 0.5)
                    return stateShuffle;
                else
                    return stateSwap;
            }

        }

        private double computeCost (List<State> states){
            double cost = 0;

            for (State state : states)
                cost += state.getCost();

            return cost;
        }

        private Set<Task> tasksetToTasklist (TaskSet ts){
            Set<Task> tasksList = new HashSet<Task>();
            for (Task task : ts){
                tasksList.add(task);
            }

            return tasksList;
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

	
}
