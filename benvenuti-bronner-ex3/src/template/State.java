package template;

import logist.topology.Topology.City;
import java.util.Set;
import java.util.HashSet;
import java.util.ArrayList;
import java.util.List;
import logist.task.Task;

public class State {

    //A state is defined by currentCity: the city in which the agent is in
    //agentTasks: the set of tasks currently carried by the agent
    //cityTasks: the set of tasks available in all the cities
    //weight: the total weight of the tasks carried by the agent
    //cost: cost to reach the state
    //agentActionList: list of all the action taken to reach this state.
    private City currentCity;
    private Set<Task> agentTasks;
    private Set<Task> cityTasks;
    private int weight;
    private double cost;
    private List<AgentAction> agentActionList;

    public State(City currentCity, Set<Task> agentTasks, 
            Set<Task> cityTasks, int weight, double cost,
            List<AgentAction> agentActionList) {
        this.currentCity = currentCity;
        this.agentTasks = agentTasks;
        this.cityTasks = cityTasks;
        this.weight = weight;
        this.cost = cost;
        this.agentActionList = agentActionList;
    }

    public City getCurrentCity() {
        return currentCity;
    }

    public Set<Task> getAgentTaskList() {
        return agentTasks;
    }

    public Set<Task> getCityTasksList() {
        return cityTasks;
    }

    public int getWeight() {
        return weight;
    }

    public double getCost() {
        return cost;
    }

    public List<AgentAction> getAgentActionList() {
        return agentActionList;
    }

    public void setCurrentCity (City currentCity) {
        this.currentCity = currentCity;
    }

    public void setAgentTasksList (Set<Task> agentTasks) {
        this.agentTasks = agentTasks;
    }

    public void setCityTasksList (Set<Task> cityTasks) {
        this.cityTasks = cityTasks;
    }

    public void setAgentActionList (List<AgentAction> agentActionList) {
        this.agentActionList = agentActionList;
    }

    public boolean removeAgentTask (Task task) {
        return agentTasks.remove(task);
    }

    public boolean addAgentTask (Task task) {
        return agentTasks.add(task);
    }

    public boolean removeCityTask (Task task) {
        return cityTasks.remove(task);
    }

    public boolean addCityTask (Task task) {
        return cityTasks.add(task);
    }

    public boolean addAgentAction (AgentAction action) {
        return agentActionList.add(action);
    }

    public void setWeight (int weight) {
        this.weight = weight;
    }

    public void setCost (double cost) {
        this.cost = cost;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((currentCity == null) ? 0 : currentCity.hashCode());
        result = prime * result + ((agentTasks == null) ? 0 : agentTasks.hashCode());
        result = prime * result + ((cityTasks == null) ? 0 : cityTasks.hashCode());
        result = prime * result + weight;
        result = prime * result + Double.hashCode(cost);
        result = prime * result + ((agentActionList == null) ? 0 : agentActionList.hashCode());

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

        if (agentTasks == null) {
            if (other.agentTasks != null) {
                return false;
            }
        } else if (!agentTasks.equals(other.agentTasks))
            return false;

        if (cityTasks == null) {
            if (other.cityTasks != null) {
                return false;
            }
        } else if (!cityTasks.equals(other.cityTasks))
            return false;

        if (weight != other.weight)
            return false;
       
        if (cost != other.cost)
            return false;

        if (agentActionList == null) {
            if (other.agentActionList != null) {
                return false;
            }
        } else if (!agentActionList.equals(other.agentActionList))
            return false;
        
        return true;
    }

    @Override
    public State clone() {
        Set<Task> nAgentTasks = new HashSet<Task>();
        Set<Task> nCityTasks = new HashSet<Task>();
        List<AgentAction> nAgentActionList = new ArrayList<AgentAction>();

        for (Task task : agentTasks) {
            nAgentTasks.add(task);
        }
        for (Task task : cityTasks) {
            nCityTasks.add(task);
        }
        for (AgentAction act : agentActionList) {
            nAgentActionList.add(act);
        }

        State state = new State(currentCity, nAgentTasks, nCityTasks, weight, 
                cost, nAgentActionList);
        return state;
    }

    @Override
    public String toString() {
        return "State: " + System.lineSeparator() +
            "In: " + currentCity.toString() + System.lineSeparator() +
            "Carrying: " + agentTasks.toString() + System.lineSeparator() +
            "Available: " + cityTasks.toString() + System.lineSeparator() +
            "Total weight: " + Integer.toString(weight) + System.lineSeparator() +
            "Action list: " + agentActionList.toString() + System.lineSeparator() +
            "Cost: " + Double.toString(cost);


    }
}
