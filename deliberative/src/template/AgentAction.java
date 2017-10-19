
package template;

import logist.topology.Topology.City;

public class AgentAction {

    //Encode the different type of action an agent can do:
    //pickup: pickup the task in homeCity to be delivered at destCity
    //deliver: deliver the task at destCity
    
    private boolean pickup;
    private boolean deliver;
    private City destCity;
    private City homeCity;

    public AgentAction(boolean pickup, boolean deliver, City destCity, City homeCity) {
        this.pickup = pickup;
        this.deliver = deliver;
        this.destCity = destCity;
        this.homeCity = homeCity;
    }

    public boolean getPickup() {
        return pickup;
    }

    public boolean getDeliver() {
        return deliver;
    }

    public City getDestCity() {
        return destCity;
    }

    public City getHomeCity() {
        return homeCity;
    }

    public void setpickup (boolean pickup) {
        this.pickup = pickup;
    }

    public void setDeliver (boolean deliver) {
        this.deliver = deliver;
    }

    public void setDestCity(City destCity) {
        this.destCity = destCity;
    }
    
    public void setHomeCity(City homeCity) {
        this.homeCity = homeCity;
    }

    //Override of hashCode and equals so that we can use the class in a hashMap
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((destCity == null) ? 0 : destCity.hashCode());
        result = prime * result + ((homeCity == null) ? 0 : homeCity.hashCode());
        result = prime * result + (pickup ? 1231 : 1237);
        result = prime * result + (deliver ? 1231 : 1237);

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

        AgentAction other = (AgentAction) obj;
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

        if (pickup != other.pickup)
            return false;

        if (deliver != other.deliver)
            return false;

        return true;
    }

    @Override
    public String toString() {
        String s;

        if (pickup)
            s = "Pickup package at " + destCity.toString();
        else
            s = "Deliver package to " + destCity.toString();
        return s;
    }
}
