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
            plan = naivePlan(vehicle, tasks);
            break;
        case BFS:
            // ...
            plan = naivePlan(vehicle, tasks);
            break;
        default:
            throw new AssertionError("Should not happen.");
        }
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
        Set<AgentTask> carriedTasks = new HashSet<AgentTask>();
        for (Task task : carriedTasksts ) {
            AgentTask aTask = new AgentTask(task.weight, task.deliveryCity, 
                    task.id, task.pickupCity, task.reward);
            carriedTasks.add(aTask);
        }

        Set<AgentTask> cityTasks = new HashSet<AgentTask>();
        for (Task task : cityTasksts ) {
             AgentTask aTask = new AgentTask(task.weight, task.deliveryCity, 
                    task.id, task.pickupCity, task.reward);
             cityTasks.add(aTask);
        }
        
        List<AgentAction> agentActionList = new ArrayList<AgentAction>();
        State startState = new State(currentCity, carriedTasks, cityTasks, 
                carriedTasksts.weightSum(), 0., agentActionList);


        //Create a priority queue that order the element according to costFun
        CostFun costFun = new CostFun(costPerKm);
        PriorityQueue<State> Q = new PriorityQueue<State>(2000, costFun);

        //Set of all the states that have already been visited
        HashSet<State> C = new HashSet<State>();
        //We store the best cost for each state
        HashMap<State,Double> stateBestCost = new HashMap<State,Double>();
        int it = 0;
        do {
            //Break if Q is empty (means that we looked at all the paths)
            if (Q.isEmpty())
                break;
            
            it++;

            //Get first node in Q
            State currState = Q.poll();

            //We check that we didn't already evaluate this state and, if yes
            //if our new evaluation of current state has a lower cost
            //TODO: we should check the hashmap instead
            if (C.contains(currState))
                //TODO: find a way to hash so that the informations about the
                //actions and the cost are not lost 
                continue;

            //Check if current state is endState
            if (currState.getCityTasksList().isEmpty() && currState.getAgentTaskList().isEmpty()){
                //check if this endState is better
                if (currState.getCost() < minCost) {
                    minCost = currState.getCost();
                    endState = currState;
                }
                continue;
            }

            C.add(currState);
            //TODO: add it to the hashmap


            //TODO: We loop over all the actions possible, ie all the pickup 
            //and deliver available in this current state
            State nextState = null;
            for (AgentTask task : currState.getAgentTaskList() ) {
                //Compute next stata
                nextState = currState.clone();
                //Action of delivering the task
                AgentAction aAction = new AgentAction(false, true, task.getDestCity(), task.getHomeCity());
                //Cost of performing the actoin
                nCost = currState.getCost() + currState.getCurrentCity().distanceTo(task.getDestCity()) * costPerKm;
                //Remove the task since it's delivered
                nextState.getAgentTaskList().remove(task);
                //update cost
                nextState.setCost(nCost);
                //Add the action to the list
                nextState.addAgentAction(aAction);
                nextState.setCurrentCity(task.getDestCity());

                //Enqueue it
                Q.add(nextState);
            }
            for (AgentTask task : currState.getCityTasksList() ) {
                //Compute next state

                //Enqueue it
            }

        }while(true);
        
        plan = stateToPlan(endState, plan);
        return plan;
    }

    //TODO
    private Plan stateToPlan(State endState, Plan plan) {

        for (AgentAction act : endState.getAgentActionList()) {
            //Translate them for logist
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
