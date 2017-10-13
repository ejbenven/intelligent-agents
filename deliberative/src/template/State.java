package template;

import logist.topology.Topology.City;

public class State {

    //Stores the current city, the destination city of the task(if any), and if
    //there is a task in the city. If not, endCity = null.
    private City currentCity;
    private List<AgentTask> tasksList;
    private int weight;

    public State(City currentCity, List<AgentTask> tasksList, int weight) {
        this.currentCity = currentCity;
        this.taskslist = tasksList;
        this.weight = weight;
    }

    public City getCurrentCity() {
        return currentCity;
    }

    public List<AgentTask> getTaskList() {
        return tasksList;
    }

    public int getWeight() {
        return weight;
    }

    public void setCurrentCity (City currentCity) {
        this.currentCity = currentCity;
    }

    public void setTasksList (List<AgentTask> tasksList) {
        this.tasksList = tasksList;
    }

    //TODO
    public void removeTask (AgentTask task) {

    }

    //TODO
    public void addTask (AgentTask task) {

    }

    public void setWeight (int weight) {
        this.weight = weight;
    }

    //We use the State and Move classes as key in a hashmap so we need to
    //override their hashCode and equals methods

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((currentCity == null) ? 0 : currentCity.hashCode());
        result = prime * result + ((endCity == null) ? 0 : endCity.hashCode());
        result = prime * result + (task ? 1231 : 1237);

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

        if (endCity == null) {
            if (other.endCity != null) {
                return false;
            }
        } else if (!endCity.equals(other.endCity))
            return false;

        if (task != other.task)
            return false;

        return true;
    }

    //TODO
    @Override
    public String toString() {

    }
}
