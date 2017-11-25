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

        private long timeout_setup;
        private long timeout_plan;
        private long timeout_bid;
        private double p;
	private double temperature;

        private double currentCost;
        private List<State> currentStates;
        private List<State> newStates;
        private Set<Task> ownedTasks;
        private double greed;
        private List<Long> ourBids;
        
        //We keep track of our oponents moves
        private List<Long> oppBids;
        private Set<Task> oppTasks;
        private double oppCurrCost;
        private double oppNewCost;
        private long oppMinBid;
        private long oppExpBid;
        private long oppTotBid;

        private long error;

	@Override
	public void setup(Topology topology, TaskDistribution distribution,
			Agent agent) {

		this.topology = topology;
		this.distribution = distribution;
		this.agent = agent;
                
                temperature = agent.readProperty("temperature",Double.class,3000.);
        	p = agent.readProperty("p",Double.class, 0.001);
                greed = 0.8;//agent.readProperty("greed",Double.class,0.5);
                LogistSettings ls = null;
                try {
                    ls = Parsers.parseSettings("config/settings_auction.xml");
                }
                catch (Exception exc) {
                    System.out.println("There was a problem loading the configuration file.");
                }
                timeout_setup = ls.get(LogistSettings.TimeoutKey.SETUP);
                timeout_plan = ls.get(LogistSettings.TimeoutKey.PLAN);
                timeout_bid = ls.get(LogistSettings.TimeoutKey.BID);
                if (timeout_bid < timeout_plan)
                    timeout_plan = timeout_bid;
                
                currentCost = 0;
                currentStates = new ArrayList<State>();
                newStates = new ArrayList<State>();
                ownedTasks = new HashSet<Task>();
                ourBids = new ArrayList<Long>();

                oppCurrCost = 0;
                oppTotBid = 0;
                oppExpBid = 0;
                oppMinBid = 0;
                oppMinBid = Long.MAX_VALUE;
                oppBids = new ArrayList<Long>();
                oppTasks = new HashSet<Task>();

                error = 0;

		long seed = 123456;
		this.random = new Random(seed);
	}

	@Override
	public void auctionResult(Task previous, int winner, Long[] bids) {
	    long oBid = bids[1-agent.id()];
            oppBids.add(oBid);
            ourBids.add(bids[agent.id()]);
            oppTotBid += oBid;
            if (oppMinBid > oBid)
                oppMinBid = oBid;
            if (oppBids.isEmpty()){
                oppMinBid = oBid;
                error = oppExpBid - oBid;
            } 
            if (oBid < oppMinBid)
                    oppMinBid = oBid;
            
            
            if (winner == agent.id()) {
	        ownedTasks.add(previous);
                currentStates.clear();
                for (State state : newStates)
                    currentStates.add(state.clone());
                currentCost = computeCost(currentStates);
                greed *= 1.1;
	    } else {
                oppTasks.add(previous);
                oppCurrCost = oppNewCost;
                greed *= 0.8;
                if (greed < 0.1)
                    greed = 0.1;
                if (greed > 1)
                    greed = 1;
            }
            newStates.clear();
	}
	
	// @Override
 //    public Long askPrice(Task task) {
 //            double newCost, bid, spread, oppMargin;
 //            long t = System.currentTimeMillis();
 //            Set<Task> newTasks = new HashSet<Task>();

 //            for (Task task_ : ownedTasks)
 //                newTasks.add(task_);
 //            newTasks.add(task);
 //            newStates.clear();
 //            //newStates = COP(agent.vehicles(), newTasks, t);
 //            newStates = greedy(agent.vehicles(), newTasks);
 //            newCost = computeCost(newStates);

            
 //            double ourMargin = newCost-currentCost;
           
 //            if(oppBids.isEmpty() || oppBids.size()<4){
 //                bid = 10;
 //            } else {
 //                newTasks.clear();
 //                for (Task task_ : oppTasks)
 //                    newTasks.add(task_);
 //                newTasks.add(task);
 //                oppNewCost = computeCost(greedy(agent.vehicles(), newTasks));
 //                oppMargin = oppNewCost - oppCurrCost;
 //                spread = ourMargin - oppMargin;

 //                if (spread > 0)
 //                    bid = ourMargin;
 //                else
 //                    bid = ourMargin + greed*spread;
 //                //if(bid < oppMinBid)
 //                //    bid = oppMinBid-1;
 //            }
            
            
            
 //        return (long) Math.round(bid);
 //    }
    @Override
    public Long askPrice(Task task) {
            double newCost, bid, spread, oppMargin, marginalCost;
            long distanceTask = task.pickupCity.distanceUnitsTo(task.deliveryCity);
            System.out.println("Distance: " + distanceTask);
            int minCostPerKm= (int) Double.POSITIVE_INFINITY;
            for (Vehicle vehicle : agent.vehicles()){
                if(vehicle.costPerKm()<minCostPerKm){
                    minCostPerKm = vehicle.costPerKm();
                }
            }
            System.out.println("cost Distance: " + minCostPerKm);
            marginalCost = Measures.unitsToKM(minCostPerKm * distanceTask);
            if(oppBids.isEmpty() || oppBids.size()<4){
                bid = greed * marginalCost;
            }else{
                bid = greed * marginalCost;
            }
                
            
            
        return (long) Math.round(bid);
    }
	

    
        @Override
        public List<Plan> plan(List<Vehicle> vehicles, TaskSet tasks) {
            long time_start = System.currentTimeMillis();
            long tc;
            Set<Task> tasks_ = tasksetToTasklist(tasks);
            List<State> states = COP(vehicles,tasks_,time_start);
            List<Plan> plans = new ArrayList<Plan>();

            
            for (State state : states)
                plans.add(stateToPlan(state));

            long time_end = System.currentTimeMillis();
            long duration = time_end - time_start;
            double cost=  computeCost(states);
            System.out.println("Our bids: " + ourBids.toString());
            System.out.println("Their bids: " + oppBids.toString());
            
            return plans;
        }

        private List<State> greedy (List<Vehicle> vehicles, Set<Task> ts){
            double bCost = Double.POSITIVE_INFINITY;
            double cost;
            int id;
            Vehicle best = vehicles.get(0);
            
            State state = new State(best.getCurrentCity(),new ArrayList<Task>(), best);
            State bestState = new State(best.getCurrentCity(),new ArrayList<Task>(), best);
            List<State> bestStates = new ArrayList<State>();
            List<State> tmpStates = new ArrayList<State>();
            
            for (Vehicle vehicle : vehicles){
                bestStates.add(new State(vehicle.getCurrentCity(),new ArrayList<Task>(), vehicle));
                tmpStates.add(new State(vehicle.getCurrentCity(),new ArrayList<Task>(), vehicle));
            }
            
            for (Task task : ts){
                bCost = Double.POSITIVE_INFINITY;
                for (Vehicle vehicle : vehicles){
                    if (task.weight > vehicle.capacity())
                        continue;
                    id = vehicles.indexOf(vehicle);
                    state = tmpStates.get(id).clone();
                    state.addTask(task);
                    state.reOrder();
                    cost = state.getCost();
                    if (cost < bCost){
                        bCost = cost;
                        bestState = state.clone();
                    }
                }
                id = vehicles.indexOf(bestState.getVehicle());
                bestStates.remove(id);
                bestStates.add(id,bestState);
                tmpStates.clear();
                for (State state_ : bestStates)
                    tmpStates.add(state_.clone());
            }

            return bestStates;
        }

        private List<State> COP (List<Vehicle> vehicles, Set<Task> ts, long time_start) {
            //Initialisation
            long duration;
            double t = temperature;
            List<State> states = new ArrayList<State>(); 
            if (ts.isEmpty()){
                for (Vehicle vehicle : vehicles)
                    states.add(new State(vehicle.getCurrentCity(), new ArrayList<Task>(), vehicle));
                return states;
            }
            
            states = greedy(vehicles, ts);            
            
            List<State> bestStates = new ArrayList<State>();
            for (State state : states)
                bestStates.add(state.clone());
            double bestCost = computeCost(bestStates);
            List<State> bestStatesOverall = new ArrayList<State>();
            for (State state : states)
                bestStatesOverall.add(state.clone());
            double bestCostOverall = bestCost;

            double newCost;
            
            while(temperature > 1){
                duration = System.currentTimeMillis() - time_start;
                if (duration >= 0.45*timeout_plan)
                    break;

                states = chooseNeighboors(states);
                temperature *= (1-p);
                newCost = computeCost(states);
                if (newCost < bestCost){
                    bestStates.clear();
                    for (State state : states)
                        bestStates.add(state.clone());
                    bestCost = newCost;
                }
                duration = System.currentTimeMillis() - time_start;
                if (duration >= 0.45*timeout_plan)
                    break;

            
            }
            if (bestCost < bestCostOverall){
                bestCostOverall = bestCost;
                bestStatesOverall.clear();
                for (State state: bestStates)
                    bestStatesOverall.add(state.clone());
            }
            
            temperature = t;
            return bestStatesOverall; 
        }

        private List<State> chooseNeighboors(List<State> states)
        {
            List<State> stateSwap = new ArrayList<State>();
            int ind1, ind2, indt;
            State state1, state2;
            Task task;
            double oldCost, shuffleCost, swapCost;
            Random rand = new Random();

            for (State state : states){
                stateSwap.add(state);
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
                            state1.reOrderAnnealing();
                            state2.reOrderAnnealing();
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
                return states;
            }

            swapCost = computeCost(stateSwap);
            oldCost = computeCost(states);
            
            if (acceptanceProbability(oldCost,swapCost) > rand.nextDouble())
                return stateSwap;
            else
                return states;
        }

	private double acceptanceProbability(double oldCost, double newCost){
            if (newCost < oldCost)
                return 1.0;
            else
                return Math.exp((oldCost-newCost) / temperature);
        }

        private double computeCost (List<State> states){
            double cost = 0;
            if (states.isEmpty())
                return cost;
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
