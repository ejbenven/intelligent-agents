package template;

import logist.task.Task;
import logist.topology.Topology.City;
import java.util.Comparator;


class CostFun implements Comparator<State> {
    /* Implement an heuristic to rank the node from the one that we expect
     * will have the smallest cost to the most expensive one.
     * To do so we compute the sum of the distance to every delivery city
     * and to every pickup city
     */
    
    
    double costPerKm;

    public CostFun (double costPerKm) {
        this.costPerKm = costPerKm;
    }

    @Override
    public int compare (State state1, State state2) {
        int dist1 = 0;
        int dist2 = 0;
        City curr1 = state1.getCurrentCity();
        City curr2 = state2.getCurrentCity();


        for (Task task : state1.getAgentTaskList() ) {
            dist1 += curr1.distanceTo(task.deliveryCity);
        }
        
        for (Task task : state1.getCityTasksList() ) {
            dist1 += curr1.distanceTo(task.pickupCity);
        }

        for (Task task : state2.getAgentTaskList() ) {
            dist2 += curr2.distanceTo(task.deliveryCity);
        }

        for (Task task : state2.getCityTasksList() ) {
            dist2 += curr2.distanceTo(task.pickupCity);

        }

        return Double.compare(state1.getCost() , state2.getCost()  );
    }

}
