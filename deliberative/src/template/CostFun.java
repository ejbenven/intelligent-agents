package template;

import logist.task.Task;
import logist.topology.Topology.City;
import java.util.Comparator;


class CostFun implements Comparator<State> {
    /* Implement an heuristic to rank the node from the one that we expect
     * will have the smallest cost to the most expensive one.
     * To do so we compute the smallest distance to either delivery or pickup
     */
    
    
    double costPerKm;

    public CostFun (double costPerKm) {
        this.costPerKm = costPerKm;
    }

    @Override
    public int compare (State state1, State state2) {
        int dist1 = 0;
        int dist2 = 0;


        for (Task task : state1.getAgentTaskList() ) {

        }
        
        for (Task task : state1.getCityTasksList() ) {

        }

        for (Task task : state2.getAgentTaskList() ) {

        }

        for (Task task : state2.getCityTasksList() ) {

        }

        return Double.compare(state1.getCost() + dist1*costPerKm, state2.getCost() + dist2*costPerKm);
    }

}
