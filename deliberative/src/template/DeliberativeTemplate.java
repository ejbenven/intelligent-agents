package template;

/* import table */
import logist.simulation.Vehicle;
import logist.agent.Agent;
import logist.behavior.DeliberativeBehavior;
import logist.plan.Plan;
import logist.task.Task;
import logist.task.TaskDistribution;
import logist.task.TaskSet;
import logist.topology.Topology;
import logist.topology.Topology.City;
import java.util.Set;
import java.util.HashSet;
import java.util.PriorityQueue;
import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;

/**
 * An optimal planner for one vehicle.
 */
@SuppressWarnings("unused")
public class DeliberativeTemplate implements DeliberativeBehavior {

    enum Algorithm { BFS, ASTAR }

    /* Environment */
    Topology topology;
    TaskDistribution td;

    /* the properties of the agent */
    Agent agent;
    int capacity;

    /* the planning class */
    Algorithm algorithm;

    @Override
    public void setup(Topology topology, TaskDistribution td, Agent agent) {
        this.topology = topology;
        this.td = td;
        this.agent = agent;

        // initialize the planner
        int capacity = agent.vehicles().get(0).capacity();
        String algorithmName = agent.readProperty("algorithm", String.class, "ASTAR");

        // Throws IllegalArgumentException if algorithm is unknown
        algorithm = Algorithm.valueOf(algorithmName.toUpperCase());

        // ...
    }

    @Override
    public Plan plan(Vehicle vehicle, TaskSet tasks) {
        Plan plan;

        // Compute the plan with the selected algorithm.
        switch (algorithm) {
        case ASTAR:
            // ...
            plan = AStarPlan(vehicle, tasks);
            break;
        case BFS:
            plan = BfsPlan(vehicle, tasks);
            // plan = naivePlan(vehicle, tasks);
            break;
        default:
            throw new AssertionError("Should not happen.");
        }
        return plan;
    }
    private Plan BfsPlan (Vehicle vehicle, TaskSet tasks) {
        City currentCity = vehicle.getCurrentCity();
        Plan plan = new Plan(currentCity);
        
        TaskSet carriedTasksts = vehicle.getCurrentTasks();
        TaskSet cityTasksts = TaskSet.intersectComplement(tasks,carriedTasksts);
        
        
        State endState = null;

        //We initialise the starting state
        Set<Task> carriedTasks = new HashSet<Task>();
        for (Task task : carriedTasksts ) {
            carriedTasks.add(task);
        }

        Set<Task> cityTasks = new HashSet<Task>();
        for (Task task : cityTasksts ) {
             cityTasks.add(task);
        }
        
        List<AgentAction> agentActionList = new ArrayList<AgentAction>();
        State startState = new State(currentCity, carriedTasks, cityTasks, 
                carriedTasksts.weightSum(), 0., agentActionList);
        // create a linkedList to go througt all states
        LinkedList<State> Q = new LinkedList<State>();

        //Set of all the states that have already been visited
        HashSet<State> C = new HashSet<State>();

        State sbc = null;
        int it = 0;
        Q.addFirst(startState);

        do {
            //Break if Q is empty (means that we looked at all the paths)
            if (Q.isEmpty())
                break;
            
            it++;

            //Get first node in Q
            State currState = Q.poll();
            // System.out.println("Hello World");
            //We check that we didn't already evaluate this state and, if yes
            //if our new evaluation of current state has a lower cost
            if (C.contains(currState))
                continue;
            else
                C.add(currState);

            sbc = currState.clone();
            // sbc.setAgentActionList(null);


            //Check if current state is endState
            if (currState.getCityTasksList().isEmpty() && currState.getAgentTaskList().isEmpty()){
                endState = currState;
                continue;
            }

            //We loop over all the actions possible, ie all the pickup 
            //and deliver available in this current state
            State nextState = null;
            // double nCost;
            //Set<Task> newCity = new HashSet<Task>();
            //Set<Task> newAgent = new HashSet<Task>();
            for (Task task : currState.getAgentTaskList() ) {
                //Compute next state
                nextState = currState.clone();
                //Action of delivering the task
                AgentAction aAction = new AgentAction(false, true, task.deliveryCity, task.pickupCity, task);
                //Cost of performing the actoin
                // nCost = currState.getCost() + currState.getCurrentCity().distanceTo(task.deliveryCity) * costPerKm;
                //Remove the task since it's delivered
                nextState.removeAgentTask(task);
                //update cost
                // nextState.setCost(nCost);
                //Add the action to the list
                nextState.addAgentAction(aAction);
                nextState.setCurrentCity(task.deliveryCity);
                nextState.setWeight(nextState.getWeight() - task.weight);

                //Enqueue it
                Q.add(nextState);
            }
            for (Task task : currState.getCityTasksList() ) {
                //Compute next state
                if(vehicle.capacity() < currState.getWeight() + task.weight)
                    continue;

                nextState = currState.clone();

                AgentAction aAction = new AgentAction(true, false, task.deliveryCity, task.pickupCity, task);

                // nCost = currState.getCost() + currState.getCurrentCity().distanceTo(task.pickupCity) * costPerKm;

                nextState.removeCityTask(task);
                nextState.addAgentTask(task);

                // nextState.setCost(nCost);
                nextState.addAgentAction(aAction);
                nextState.setCurrentCity(task.pickupCity);
                nextState.setWeight(nextState.getWeight() + task.weight);

                //Enqueue it
                Q.add(nextState);
            }

        }while(true);
        System.out.println(it);
        plan = stateToPlan(vehicle, endState, plan);
        return plan;

    }
    private Plan AStarPlan (Vehicle vehicle, TaskSet tasks) {
        City currentCity = vehicle.getCurrentCity();
        Plan plan = new Plan(currentCity);
        
        TaskSet carriedTasksts = vehicle.getCurrentTasks();
        TaskSet cityTasksts = TaskSet.intersectComplement(tasks,carriedTasksts);
        
        double costPerKm = vehicle.costPerKm();
        double minCost = Double.POSITIVE_INFINITY;
        State endState = null;

        //We initialise the starting state
        Set<Task> carriedTasks = new HashSet<Task>();
        for (Task task : carriedTasksts ) {
            carriedTasks.add(task);
        }

        Set<Task> cityTasks = new HashSet<Task>();
        for (Task task : cityTasksts ) {
             cityTasks.add(task);
        }
        
        List<AgentAction> agentActionList = new ArrayList<AgentAction>();
        State startState = new State(currentCity, carriedTasks, cityTasks, 
                carriedTasksts.weightSum(), 0., agentActionList);


        //Create a priority queue that order the element according to costFun
        CostFun costFun = new CostFun(costPerKm);
        PriorityQueue<State> Q = new PriorityQueue<State>(20000, costFun);

        //Set of all the states that have already been visited
        HashSet<State> C = new HashSet<State>();
        //We store the best cost for each state
        HashMap<State,Double> stateBestCost = new HashMap<State,Double>();
        State sbc = null;
        int it = 0;
        double costMemory;
        Q.add(startState);
        do {
            //Break if Q is empty (means that we looked at all the paths)
            if (Q.isEmpty())
                break;
            
            it++;

            //Get first node in Q
            State currState = Q.poll();

            //We check that we didn't already evaluate this state and, if yes
            //if our new evaluation of current state has a lower cost
            if (C.contains(currState))
                continue;
            else
                C.add(currState);

            sbc = currState.clone();
            sbc.setAgentActionList(null);
            costMemory = sbc.getCost();
            sbc.setCost(0.);
            if (stateBestCost.containsKey(sbc))
                if (stateBestCost.get(sbc) < costMemory)
                    continue;
                else
                    stateBestCost.replace(sbc,costMemory);
            else
                stateBestCost.put(sbc,costMemory);

            //Check if current state is endState
            if (currState.getCityTasksList().isEmpty() && currState.getAgentTaskList().isEmpty()){
                //check if this endState is better
                if (currState.getCost() < minCost) {
                    minCost = currState.getCost();
                    endState = currState;
                }
                continue;
            }

            //We loop over all the actions possible, ie all the pickup 
            //and deliver available in this current state
            State nextState = null;
            double nCost;
            //Set<Task> newCity = new HashSet<Task>();
            //Set<Task> newAgent = new HashSet<Task>();
            for (Task task : currState.getAgentTaskList() ) {
                //Compute next state
                nextState = currState.clone();
                //Action of delivering the task
                AgentAction aAction = new AgentAction(false, true, task.deliveryCity, task.pickupCity, task);
                //Cost of performing the actoin
                nCost = currState.getCost() + currState.getCurrentCity().distanceTo(task.deliveryCity) * costPerKm;
                //Remove the task since it's delivered
                nextState.removeAgentTask(task);
                //update cost
                nextState.setCost(nCost);
                //Add the action to the list
                nextState.addAgentAction(aAction);
                nextState.setCurrentCity(task.deliveryCity);
                nextState.setWeight(nextState.getWeight() - task.weight);

                //Enqueue it
                Q.add(nextState);
            }
            for (Task task : currState.getCityTasksList() ) {
                //Compute next state
                if(vehicle.capacity() < currState.getWeight() + task.weight)
                    continue;

                nextState = currState.clone();

                AgentAction aAction = new AgentAction(true, false, task.deliveryCity, task.pickupCity, task);

                nCost = currState.getCost() + currState.getCurrentCity().distanceTo(task.pickupCity) * costPerKm;

                nextState.removeCityTask(task);
                nextState.addAgentTask(task);

                nextState.setCost(nCost);
                nextState.addAgentAction(aAction);
                nextState.setCurrentCity(task.pickupCity);
                nextState.setWeight(nextState.getWeight() + task.weight);

                //Enqueue it
                Q.add(nextState);
            }

        }while(true);
        System.out.println(it);
        plan = stateToPlan(vehicle, endState, plan);
        return plan;
    }

    private Plan stateToPlan(Vehicle vehicle, State endState, Plan plan) {
        City currCity = vehicle.getCurrentCity();
        for (AgentAction act : endState.getAgentActionList()) {
            //Translate them for logist
            if(act.getPickup()) {
                if (currCity.equals(act.getHomeCity())) {
                    plan.appendPickup(act.getTask());
                } else {
                    for (City city : currCity.pathTo(act.getHomeCity())){
                        plan.appendMove(city);
                    }
                    plan.appendPickup(act.getTask());
                    currCity = act.getHomeCity();
                }
            } else {
                if (currCity.equals(act.getDestCity())) {
                    plan.appendDelivery(act.getTask());
                } else {
                    for (City city : currCity.pathTo(act.getDestCity())){
                        plan.appendMove(city);
                    }
                    plan.appendDelivery(act.getTask());
                    currCity = act.getDestCity();
                }

            }
        }

        return plan;
    }

    private Plan naivePlan(Vehicle vehicle, TaskSet tasks) {
        City current = vehicle.getCurrentCity();
        Plan plan = new Plan(current);

        for (Task task : tasks) {
            // move: current city => pickup location
            for (City city : current.pathTo(task.pickupCity))
                plan.appendMove(city);

            plan.appendPickup(task);

            // move: pickup location => delivery location
            for (City city : task.path())
                plan.appendMove(city);

            plan.appendDelivery(task);

            // set current city
            current = task.deliveryCity;
        }
        return plan;
    }

    private boolean stateCompare (State state1, State state2) {
        if (!state1.getCurrentCity().equals(state2.getCurrentCity()))
            return false;
        else if (!state1.getAgentTaskList().equals(state2.getAgentTaskList()))
            return false;
        else if (state1.getCityTasksList().equals(state2.getCityTasksList()))
            return false;
        else
            return true;
    }

    @Override
    public void planCancelled(TaskSet carriedTasks) {

        if (!carriedTasks.isEmpty()) {
            // This cannot happen for this simple agent, but typically
            // you will need to consider the carriedTasks when the next
            // plan is computed.
        }
    }
}
