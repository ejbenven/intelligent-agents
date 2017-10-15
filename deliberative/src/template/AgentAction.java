
package template;

import logist.topology.Topology.City;

public class AgentAction {

    //Encode the different type of action an agent can do:
    //pickup: pickup the task to be delivered at destCity
    //deliver: deliver the task for the city he's currently in
    //move: move to destCity
    //idle: do nothing
    private boolean pickup;
    private boolean deliver;
    private boolean move;
    private boolean idle;
    private City destCity;

    public AgentAction(boolean pickup, boolean deliver, boolean move, boolean idle,
                     City destCity) {
        this.pickup = pickup;
        this.deliver = deliver;
        this.move = move;
        this.idle = idle;
        this.destCity = destCity;
    }

    public boolean getPickup() {
        return pickup;
    }

    public boolean getDeliver() {
        return deliver;
    }

    public boolean getMove() {
        return move;
    }

    public boolean getIdle() {
        return idle;
    }

    public City getDestCity() {
        return destCity;
    }

    public void setpickup (boolean pickup) {
        this.pickup = pickup;
    }

    public void setDeliver (boolean deliver) {
        this.deliver = deliver;
    }

    public void setMove (boolean move) {
        this.move = move;
    }

    public void setIdle(boolean idle) {
        this.idle = idle;
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
        result = prime * result + (pickup ? 1231 : 1237);
        result = prime * result + (deliver ? 1231 : 1237);
        result = prime * result + (move ? 1231 : 1237);
        result = prime * result + (idle ? 1231 : 1237);

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

        if (pickup != other.pickup)
            return false;

        if (deliver != other.deliver)
            return false;

        if (move != other.move)
            return false;

        if (idle != other.idle)
            return false;

        return true;
    }

    @Override
    public String toString() {
        String s;

        if (idle)
            s = "Idle";
        else if (pickup)
            s = "Pickup package for " + destCity.toString();
        else if (deliver)
            s = "Deliver package";
        else 
            s = "Move to " + destCity.toString();

        return s;
    }
}
