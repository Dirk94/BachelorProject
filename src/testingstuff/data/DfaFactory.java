package testingstuff.data;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Random;
import java.util.Set;

public class DfaFactory {
    
    public Random random;
    
    public DfaFactory(long seed) {
        random = new Random(seed);
    }
    
    /** @deprecated */
    public Dfa specialDfa2() {
        Set<DfaState> states = new HashSet();
        DfaState state3 = new DfaState(3);
        DfaState state1 = new DfaState(1);
        DfaState state0 = new DfaState(0);
        DfaState state9 = new DfaState(9);
        DfaState state8 = new DfaState(8);
        DfaState state6 = new DfaState(6);
        DfaState state5 = new DfaState(5);
        
        states.add(state3);
        states.add(state1);
        states.add(state0);
        states.add(state9);
        states.add(state8);
        states.add(state6);
        states.add(state5);
        
        state3.edges.add(new Edge(state3, state5, 1));
        state3.edges.add(new Edge(state3, state6, 0));
        
        state1.edges.add(new Edge(state1, state6, 0));
        state1.edges.add(new Edge(state1, state1, 1));
        
        state0.edges.add(new Edge(state0, state9, 1));
        state0.edges.add(new Edge(state0, state3, 0));
        
        state9.edges.add(new Edge(state9, state5, 0));
        state9.edges.add(new Edge(state9, state9, 1));
        
        state8.edges.add(new Edge(state8, state6, 1));
        state8.edges.add(new Edge(state8, state1, 0));
        
        state6.edges.add(new Edge(state6, state9, 1));
        state6.edges.add(new Edge(state6, state0, 0));
        
        state5.edges.add(new Edge(state5, state0, 0));
        state5.edges.add(new Edge(state5, state3, 1));
        
        state1.finalState = true;
        state8.finalState = true;
        
        return new Dfa(state8, states, 2);
    }
    
    /** @deprecated */
    public Dfa specialDFA() {
        Set<DfaState> states = new HashSet();
        DfaState state1 = new DfaState(1);
        DfaState state2 = new DfaState(2);
        DfaState state3 = new DfaState(3);
        
        Edge edge1 = new Edge(state1, state2, 0);
        Edge edge2 = new Edge(state2, state3, 0);
        Edge edge3 = new Edge(state3, state2, 0);
        
        state1.edges.add(edge1);
        state2.edges.add(edge2);
        state3.edges.add(edge3);
        
        state3.finalState = true;        
        
        states.add(state1);
        states.add(state2);
        states.add(state3);
        
        Dfa dfa = new Dfa(state1, states, 1);
        return dfa;
    }
    
    public Dfa randomDfa(int numberOfStates, int alphabetSize) {
        Set<DfaState> states = generateStates(numberOfStates);
        
        addRandomEdges(states, alphabetSize);
        setFinalStates(states);
        
        DfaState startingState = getRandomState(states);
        Dfa dfa = new Dfa(startingState, states, alphabetSize);
        removeUnreachableStates(dfa);
        
        if (dfa.getFinalStates().isEmpty()) {
            return randomDfa(numberOfStates, alphabetSize);
        }
        
        return dfa;
    }
    
    private void removeUnreachableStates(Dfa dfa) {
        Set<DfaState> reachableStates = new HashSet();
        Set<DfaState> newStates = new HashSet();

        reachableStates.add(dfa.startingState);
        newStates.add(dfa.startingState);

        do {
            Set<DfaState> temp = new HashSet();

            for (DfaState state : newStates) {
                for (Edge edge : state.edges) {
                    DfaState stateTo = edge.stateTo;
                    if (!temp.contains(stateTo)) {
                        temp.add(stateTo);
                    }
                }
            }

            for (DfaState reachableState : reachableStates) {
                temp.remove(reachableState);
            }

            newStates = temp;
            for (DfaState newState : newStates) {
                if (!reachableStates.contains(newState)) {
                    reachableStates.add(newState);
                }
            }
        } while (!newStates.isEmpty());

        dfa.setStates(reachableStates);
    }
    
    private void setFinalStates(Set<DfaState> states) {
        int numberOfFinalStates = random.nextInt(states.size() - 1) + 1;

        for (int i = 0; i < numberOfFinalStates; i++) {
            getRandomState(states).finalState = true;
        }
    }
    
    private void addRandomEdges(Set<DfaState> states, int alphabetSize) {
        Iterator<DfaState> it = states.iterator();
        while(it.hasNext()) {
            DfaState stateFrom = it.next();
            
            for (int i=0; i<alphabetSize; i++) {
                DfaState stateTo = getRandomState(states);
                Edge edge = new Edge(stateFrom, stateTo, i);
                stateFrom.edges.add(edge);
                stateTo.incomingEdges.add(edge);
            }
        }
    }
    
    private Set<DfaState> generateStates(int numberOfStates) {
        Set<DfaState> states = new HashSet();
        for (int i = 0; i < numberOfStates; i++) {
            states.add(new DfaState(i));
        }
        return states;
    }
    
    private DfaState getRandomState(Set<DfaState> states) {
        int index = random.nextInt(states.size());
        Iterator<DfaState> it = states.iterator();
        int i = 0;
        while(it.hasNext()) {
            DfaState state = it.next();
            if (i == index) {
                return state;
            }
            i++;
        }
        return null;
    }
    
}
