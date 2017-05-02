package testingstuff.data;

import java.util.HashSet;
import java.util.Set;

public class DfaState {
    
    public int id;
    
    public Set<Edge> edges;
    
    public Set<Edge> incomingEdges;
    
    public boolean finalState;
    
    public DfaState(int id) {
        this.id = id;
        edges = new HashSet();
        incomingEdges = new HashSet();
    }
    
    @Override
    public boolean equals(Object other) {
        if (other instanceof DfaState) {
            DfaState otherState = (DfaState) other;
            return otherState.id == id;
        }
        
        return false;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 23 * hash + this.id;
        return hash;
    }
    
    @Override
    public String toString() {
        String s = Integer.toString(id);
        if (finalState) {
            s += "F";
        }
        return s;
    }
}
