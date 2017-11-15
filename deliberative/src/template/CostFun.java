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
        double dist1 = 0;
        double dist2 = 0;
        double d;
        City curr1 = state1.getCurrentCity();
        City curr2 = state2.getCurrentCity();


        for (Task task : state1.getAgentTaskList() ) {
            d = curr1.distanceTo(task.deliveryCity);
            if (d > dist1)
                dist1 = d;
        }
        
        for (Task task : state1.getCityTasksList() ) {
            d = curr1.distanceTo(task.pickupCity);
            if (d > dist1)
                dist1 = d;
        }

        for (Task task : state2.getAgentTaskList() ) {
            d = curr2.distanceTo(task.deliveryCity);
            if (d>dist2)
                dist2 = d;
        }

        for (Task task : state2.getCityTasksList() ) {
            d = curr2.distanceTo(task.pickupCity);
            if (d>dist2)
                dist2 = d;

        }

        return Double.compare(state1.getCost() + dist1*costPerKm , state2.getCost() + dist2*costPerKm );
    }

}
