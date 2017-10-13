
package template;

import logist.topology.Topology.City;

public class AgentTask {

    //Encode the informations about a task:
    //weight: weight of the load
    //destCity: destination of the package
    private int weight;
    private City destCity;

    public AgentTask(int weight, City destCity) {
        this.weight = weight;
        this.destCity = destCity;
    }

    public int getWeight() {
        return weight;
    }

    public City getDestCity() {
        return destCity;
    }

    public void setWeight (int weight) {
        this.weight = weight;
    }

    public void setDestCity(City destCity) {
        this.destCity = destCity;
    }


    //Override of hashCode and equals so that we can use the class in a hashMap
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((destCity == null) ? 0 : destCity.hashCode());
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

        AgentTask other = (AgentTask) obj;
        if (destCity == null) {
            if (other.destCity != null) {
                return false;
            }
        } else if (!destCity.equals(other.destCity))
            return false;

        if (weight != other.weight)
            return false;

        return true;
    }

    @Override
    public String toString() {
        return "Weight: " + Integer.toString(weight) + System.lineSeparator() +
               "to: " + destCity.toString();
    }
