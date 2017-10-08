package template;

import logist.topology.Topology.City;

public class State{

    //Stores the current city, the destination city of the task(if any), and if
    //there is a task in the city. If not, endCity = null.
    private City startCity;
    private City endCity;
    private boolean task;

    public State(City startCity, City endCity, boolean task){
        this.startCity = startCity;
        this.endCity = endCity;
        this.task = task;
    }

    public City getStartCity(){
        return startCity;
    }

    public City getEndCity(){
        return endCity;
    }

    public boolean getTask(){
        return task;
    }

    public void setStartCity(City startCity){
        this.startCity = startCity;
    }

    public void setEndCity(City endCity){
        this.endCity = endCity;
    }

    public void setTask (boolean task){
        this.task = task;
    }

    //We use the State and Move classes as key in a hashmap so we need to
    //override their hashCode and equals methods

    @Override
    public int hashCode(){
        final int prime = 31;
        int result = 1;
        result = prime * result + ((startCity == null) ? 0 : startCity.hashCode());
        result = prime * result + ((endCity == null) ? 0 : endCity.hashCode());
        result = prime * result + (task ? 1231 : 1237);

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
        
        State other = (State) obj;
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
       
        if (task != other.task)
            return false;
                
        return true;
        }
}
