package template;

import java.util.Random;
import java.util.ArrayList;
import java.util.List;
import logist.topology.Topology.City;
import logist.task.Task;
import logist.simulation.Vehicle;
import java.util.Set;
import java.util.HashSet;

public class State {

    private City currentCity;
    private List<Task> tasks;
    private Vehicle vehicle;
    private double cost;

    public State (City currentCity, List<Task> tasks, Vehicle vehicle) {
        this.currentCity = currentCity;
        this.tasks = tasks;
        this.vehicle = vehicle;
        cost = computeCost();
    }

    private double computeCost(){
        City presentCity = currentCity;
        double cost = 0;
        Set<Task> carriedTasks = new HashSet<Task>();

        for(Task task : tasks) {
            //if the task is not already carried
            if (carriedTasks.add(task)){
                if (presentCity.equals(task.pickupCity))
                    cost += 0;
                else
                    cost += vehicle.costPerKm() * presentCity.distanceTo(task.pickupCity);
                presentCity = task.pickupCity;
            } else {
                //delivers it
                if (presentCity.equals(task.deliveryCity))
                    cost += 0;
                else
                    cost += vehicle.costPerKm() * presentCity.distanceTo(task.deliveryCity);
                presentCity = task.deliveryCity;
            }
        }

        return cost;
    }

    public boolean addTask (Task task) {
        //We check if the vehicle can carry the task
        if (task.weight  > vehicle.capacity())
            return false;
        int cnt = 0;
        boolean ret = tasks.add(task);
        if (ret)
            cnt++;
        ret = tasks.add(task);
        if (!ret && cnt == 1)
            System.out.println("Adding Error");
        
        cost = computeCost();
        return ret;
    }

    public boolean removeTask (Task task) {
        int cnt = 0;
        boolean ret = tasks.remove(task);
        if (ret)
            cnt++;
        ret = tasks.remove(task);
        if(!ret && cnt==1)
            System.out.println("Removing Error");

        cost = computeCost();
        return ret;
    }

    public boolean shuffle() {

        if (tasks.isEmpty() || tasks.size() < 4)
            return false;

        Random rand = new Random();
        int size = tasks.size();
        int ind1, ind2;
        
        do{
            ind1 = rand.nextInt(size);
            do{
                ind2 = rand.nextInt(size);
            }while(ind1 == ind2 || tasks.get(ind1).id == tasks.get(ind2).id);
        }while(tooHeavy(ind1, ind2));

        Task task1 = tasks.get(ind1);
        Task task2 = tasks.get(ind2);

        tasks.remove(ind2);
        tasks.add(ind2, task1);
        tasks.remove(ind1);
        tasks.add(ind1, task2);
        cost = computeCost();

        return true;
    }

    private boolean tooHeavy(int ind1, int ind2){
        List<Task> tmpTasks = new ArrayList<Task>();

        for (Task task : tasks){
            tmpTasks.add(task);
        }
        Task task1 = tasks.get(ind1);
        Task task2 = tasks.get(ind2);

        tmpTasks.remove(ind2);
        tmpTasks.add(ind2, task1);
        tmpTasks.remove(ind1);
        tmpTasks.add(ind1, task2);

        Set<Task> carriedTasks = new HashSet<Task>();
        int weight = 0;

        for (Task task : tmpTasks){
            if (carriedTasks.add(task)){
                weight += task.weight;
                if (weight > vehicle.capacity())
                    return true;
            } else {
                weight -= task.weight;
            }
        }
        
        return false;
    }

    public void reOrder() {
        City cCity = currentCity;
        Task cTask = null;
        double sCost = Double.POSITIVE_INFINITY;
        double tmpCost;
        int carriedWeight = 0;

        List<Task> tmpTasks = new ArrayList<Task>();
        for (Task task : tasks)
            tmpTasks.add(task);

        Set<Task> carriedTasks = new HashSet<Task>();
        tasks.clear();
        while(!tmpTasks.isEmpty()){
            sCost = Double.POSITIVE_INFINITY;
            for (Task task : tmpTasks){
                if(carriedTasks.contains(task))
                    tmpCost = cCity.distanceTo(task.deliveryCity)*vehicle.costPerKm();
                else if (carriedWeight + task.weight < vehicle.capacity())
                    tmpCost = cCity.distanceTo(task.pickupCity)*vehicle.costPerKm();
                else
                    continue;
                if (tmpCost < sCost){
                    sCost = tmpCost;
                    cTask = task;
                }
            }
            tasks.add(cTask);
            if(carriedTasks.add(cTask)){
                cCity = cTask.pickupCity;
                carriedWeight += cTask.weight;
            }
            else{
                cCity = cTask.deliveryCity;
                carriedWeight -= cTask.weight;
            }
            tmpTasks.remove(cTask);
        }
        cost = computeCost();
    }


    public void reOrderAnnealing(){
        //We initialize with the greedy algorithm
        reOrder();
        double t = 3000.;
        double p = 0.001;

        if (tasks.isEmpty() || tasks.size() < 4)
            return;

        List<Task> bestTasks = new ArrayList<Task>();
        for (Task task : tasks)
            bestTasks.add(task);
        double bestCost = cost;

        List<Task> oldTasks = new ArrayList<Task>();
        for (Task task : tasks)
            oldTasks.add(task);
        double oldCost = cost;

        Random rand = new Random();
        int size = tasks.size();
        int ind1, ind2;
      
        while(t > 1){
            do{
                ind1 = rand.nextInt(size);
                do{
                    ind2 = rand.nextInt(size);
                }while(ind1 == ind2);
            }while(tooHeavy(ind1, ind2));

            Task task1 = tasks.get(ind1);
            Task task2 = tasks.get(ind2);

            tasks.remove(ind2);
            tasks.add(ind2, task1);
            tasks.remove(ind1);
            tasks.add(ind1, task2);
            cost = computeCost();

            if (acceptanceProbability(oldCost,cost,t) > rand.nextDouble()){
                oldTasks.clear();
                for (Task task : tasks)
                    oldTasks.add(task);
                oldCost = cost;
            } else {
                tasks.clear();
                for (Task task : oldTasks)
                    tasks.add(task);
                cost = oldCost;
            }

            if (cost < bestCost){
                bestTasks.clear();
                for (Task task : tasks)
                    bestTasks.add(task);
                bestCost = cost;
            }

            t *= (1-p);
        }
        
        tasks.clear();
        for (Task task : bestTasks)
            tasks.add(task);

        cost = computeCost();
    }

    private double acceptanceProbability(double oldCost, double newCost, double t){
            if (newCost < oldCost)
                return 1.0;
            else
                return Math.exp((oldCost-newCost) / t);
        }    
   
    public Vehicle getVehicle(){
        return vehicle;
    }

    public City getCurrentCity(){
        return currentCity;
    }

    public List<Task> getTasks(){
        return tasks;
    }

    public void setCurrentCity(City currentCity){
        this.currentCity = currentCity;
    }

    public void setTasks (List<Task> tasks){
        this.tasks = tasks;
    }

    public double getCost(){
        return cost;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((currentCity == null) ? 0 : currentCity.hashCode());
        result = prime * result + ((tasks == null) ? 0 : tasks.hashCode());

        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;

        if (obj == null)
            return false;

        if (getClass() != obj.getClass())
            return false;

        State other = (State) obj;
        if (currentCity == null) {
            if (other.currentCity != null) {
                return false;
            }
        } else if (!currentCity.equals(other.currentCity))
            return false;

        if (tasks == null) {
            if (other.getTasks() != null) {
                return false;
            }
        } else if (!tasks.equals(other.getTasks()))
            return false;
        
        return true;
    }

    @Override
    public State clone() {
        City nCurrentCity = currentCity;
        Vehicle nVehicle = vehicle;
        List<Task> nTasks = new ArrayList<Task>();

        for (Task task : tasks) {
            nTasks.add(task);
        }

        State state = new State(nCurrentCity, nTasks, nVehicle);
        return state;
    }

    @Override
    public String toString() {
        return "State" + System.lineSeparator() +
            "City: " + currentCity.toString() + System.lineSeparator() +
            "Tasks: " + tasks.toString() + System.lineSeparator(); 


    }

}





