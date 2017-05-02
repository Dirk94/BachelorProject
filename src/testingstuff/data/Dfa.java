package testingstuff.data;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class Dfa {

    public DfaState startingState;

    public Set<DfaState> states;
    
    public List<DfaState> statesAsList;

    public int alphabetSize;

    public Dfa(DfaState startingState, Set<DfaState> states, int alphabetSize) {
        this.startingState = startingState;        
        this.alphabetSize = alphabetSize;
        this.statesAsList = new ArrayList();
        
        setStates(states);
    }

    public void setStates(Set<DfaState> states) {
        this.states = states;
        for (DfaState state : states) {
            statesAsList.add(state);
        }
    }
    
    public Set<DfaState> getFinalStates() {
        Set<DfaState> finalStates = new HashSet();

        Iterator<DfaState> it = states.iterator();
        while(it.hasNext()) {
            DfaState state = it.next();
            if (state.finalState) {
                finalStates.add(state);
            }
        }
        
        return finalStates;     
    }
    
    public Set<DfaState> getNonFinalStates() {
        Set<DfaState> nonFinalStates = new HashSet();

        Iterator<DfaState> it = states.iterator();
        while(it.hasNext()) {
            DfaState state = it.next();
            if (!state.finalState) {
                nonFinalStates.add(state);
            }
        }
        
        return nonFinalStates;     
    }

    public void print() {
        System.out.println("Starting State: " + startingState.id);
        
        Iterator<DfaState> it = states.iterator();
        while(it.hasNext()) {
            DfaState state = it.next();
            System.out.println("id: " + state);
            
            Iterator<Edge> edgeIterator = state.edges.iterator();
            while(edgeIterator.hasNext()) {
                Edge edge = edgeIterator.next();
                System.out.println("\t{ " + state.id + " } --" + (char) (97 + edge.label) + "--> { " + edge.stateTo.id + " }");
            }
        }
    }
}
