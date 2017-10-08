package template;

import logist.topology.Topology.City;

public class Move{

    //Stores the starting city, the destination city, and if a pickup has been 
    //picked up.
    private City startCity;
    private City endCity;
    private boolean pickup;

    public Move(City startCity, City endCity, boolean pickup){
        this.startCity = startCity;
        this.endCity = endCity;
        this.pickup = pickup;
    }

    public City getStartCity(){
        return startCity;
    }

    public City getEndCity(){
        return endCity;
    }

    public boolean getpickup(){
        return pickup;
    }

    public void setStartCity(City startCity){
        this.startCity = startCity;
    }

    public void setEndCity(City endCity){
        this.endCity = endCity;
    }

    public void setpickup (boolean pickup){
        this.pickup = pickup;
    }

    //We use the Move and Move classes as key in a hashmap so we need to
    //override their hashCode and equals methods

    @Override
    public int hashCode(){
        final int prime = 31;
        int result = 1;
        result = prime * result + ((startCity == null) ? 0 : startCity.hashCode());
        result = prime * result + ((endCity == null) ? 0 : endCity.hashCode());
        result = prime * result + (pickup ? 1231 : 1237);

        return result;
    }

    @Override
    public boolean equals(Object obj){
        if (this == obj)
            return true;
        
        if (obj == null)
            return false;
        
        if (getClass() != obj.getClass())
            return false;
        
        Move other = (Move) obj;
        if (startCity == null) {
            if (other.startCity != null) {
                return false;
            }
        } else if (!startCity.equals(other.startCity))
            return false;
        
        if (endCity == null) {
            if (other.endCity != null) {
                return false;
            }
        } else if (!endCity.equals(other.endCity))
            return false;
       
        if (pickup != other.pickup)
            return false;
                
        return true;
        }
}

