package template;

import logist.topology.Topology.City;
import java.util.Set;

public class State {

    //A state is defined by currentCity: the city in which the agent is in
    //agentTasks: the set of tasks currently carried by the agent
    //cityTasks: the set of tasks available in the city
    //weight: the total weight of the tasks carried by the agent
    private City currentCity;
    private Set<AgentTask> agentTasks;
    private Set<AgentTask> cityTasks;
    private int weight;

    public State(City currentCity, Set<AgentTask> agentTasks, 
            Set<AgentTask> cityTasks, int weight) {
        this.currentCity = currentCity;
        this.agentTasks = agentTasks;
        this.cityTasks = cityTasks;
        this.weight = weight;
    }

    public City getCurrentCity() {
        return currentCity;
    }

    public Set<AgentTask> getAgentTaskList() {
        return agentTasks;
    }

    public Set<AgentTask> getCityTasksList() {
        return cityTasks;
    }

    public int getWeight() {
        return weight;
    }

    public void setCurrentCity (City currentCity) {
        this.currentCity = currentCity;
    }

    public void setAgentTasksList (Set<AgentTask> agentTasks) {
        this.agentTasks = agentTasks;
    }

    public void setCityTasksList (Set<AgentTask> cityTasks) {
        this.cityTasks = cityTasks;
    }

    public boolean removeAgentTask (AgentTask task) {
        return agentTasks.remove(task);
    }

    public boolean addAgentTask (AgentTask task) {
        return agentTasks.add(task);
    }

    public boolean removeCityTask (AgentTask task) {
        return cityTasks.remove(task);
    }

    public boolean addCityTask (AgentTask task) {
        return cityTasks.add(task);
    }

    public void setWeight (int weight) {
        this.weight = weight;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((currentCity == null) ? 0 : currentCity.hashCode());
        result = prime * result + ((agentTasks == null) ? 0 : agentTasks.hashCode());
        result = prime * result + ((cityTasks == null) ? 0 : cityTasks.hashCode());
        result = prime * result + weight;

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

        return true;
    }

    //TODO
    @Override
    public String toString() {
        return "State :" + System.lineSeparator() +
            "In : " + currentCity.toString() + System.lineSeparator() +
            "Carrying : " + agentTasks.toString() + System.lineSeparator() +
            "Available : " + cityTasks.toString() + System.lineSeparator() +
            "Total weight : " + Integer.toString(weight);


    }
}
