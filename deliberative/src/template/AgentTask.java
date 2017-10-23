
package template;

import logist.topology.Topology.City;

public class AgentTask {

    //Encode the informations about a task:
    //weight: weight of the load
    //destCity: destination of the package
    //id: id of the task
    //homeCity: city in which the task can be picked up
    private int weight;
    private City destCity;
    private int id;
    private City homeCity;
    private long reward;

    public AgentTask(int weight, City destCity, int id, City homeCity, long reward) {
        this.weight = weight;
        this.destCity = destCity;
        this.id = id;
        this.homeCity = homeCity;
        this.reward = reward;
    }

    public int getWeight() {
        return weight;
    }

    public int getId() {
        return id;
    }

    public City getHomeCity() {
        return homeCity;
    }
  
    public City getDestCity() {
        return destCity;
    }

    public long getReward() {
        return reward;
    }

    public void setWeight (int weight) {
        this.weight = weight;
    }

    public void setHomeCity(City homeCity) {
        this.homeCity = homeCity;
    }
  
    public void setDestCity(City destCity) {
        this.destCity = destCity;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setReward(long reward) {
        this.reward = reward;
    }

    //Override of hashCode and equals so that we can use the class in a hashMap
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((destCity == null) ? 0 : destCity.hashCode());
        result = prime * result + ((homeCity == null) ? 0 : homeCity.hashCode());
        result = prime * result + weight;
        result = prime * result + id;
        result = prime * result + Long.hashCode(reward);
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

        AgentTask other = (AgentTask) obj;
        if (destCity == null) {
            if (other.destCity != null) {
                return false;
            }
        } else if (!destCity.equals(other.destCity))
            return false;

        if (homeCity == null) {
            if (other.homeCity != null) {
                return false;
            }
        } else if (!homeCity.equals(other.homeCity))
            return false;

        if (weight != other.weight)
            return false;

        if (id != other.id)
            return false;

        if (reward != other.reward)
            return false;

        return true;
    }

    @Override
    public String toString() {
        return "Weight: " + Integer.toString(weight) + System.lineSeparator() +
               "from: " + homeCity.toString() + System.lineSeparator() +
               "to: " + destCity.toString() + System.lineSeparator() +
               "id: " + Integer.toString(id) + System.lineSeparator() +
               "reward: " + Long.toString(reward);
    }
}
