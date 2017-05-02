package testingstuff.algorithms.hopcroftparallel;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import testingstuff.data.Dfa;
import testingstuff.data.DfaState;
import testingstuff.data.Edge;

public class HopcroftParallel {
    
    public Set<Set<DfaState>> run(Dfa dfa) {
        Set<Set<DfaState>> wHasContained = new HashSet();
        
        Set<Set<DfaState>> p = new HashSet();
        Set<Set<DfaState>> w = new HashSet();
        
        p.add(dfa.getFinalStates());
        p.add(dfa.getNonFinalStates());
        
        w.add(dfa.getFinalStates());
        
        while (!w.isEmpty()) {
            Iterator<Set<DfaState>> wIt = w.iterator();
            Set<DfaState> a = wIt.next();
            wIt.remove();
            
            for (int i=0; i<dfa.alphabetSize; i++) {
                Set<DfaState> x = getX(dfa, a, i);
                
                // Start a thread for each element in p.
                int nThreads = p.size();
                Worker[] workers = new Worker[nThreads];
                int index = 0;
                for (Set<DfaState> states : p) {
                    workers[index] = new Worker(states, x, w);
                    workers[index].start();
                    index++;
                }
                
                // Wait for all threads to finish.
                for (int j=0; j<nThreads; j++) {
                    try {
                        workers[j].join();
                    } catch(InterruptedException ex) {
                        ex.printStackTrace();
                    }
                }
                
                // Update the data according to the Thread results.
                Set<Set<DfaState>> before = p;
                for (int j=0; j<nThreads; j++) {
                    
                    if (workers[j].toRemoveFromP != null) {
                        p.remove(workers[j].toRemoveFromP);
                    }
                    if (workers[j].toRemoveFromW != null) {
                        w.remove(workers[j].toRemoveFromW);
                    }
                    
                    for (Set<DfaState> s : workers[j].toAddToP) {
                        if (!p.contains(s)) {
                            p.add(s);
                        }
                    }
                    for (Set<DfaState> s : workers[j].toAddToW) {
                        if (!w.contains(s) && !wHasContained.contains(s)) {
                            w.add(s);
                            wHasContained.add(s);
                        }
                    }                    
                }
            }
        }
        
        return p;
    }
    
    private Set<DfaState> getX(Dfa dfa, Set<DfaState> a, int i) {
        Set<DfaState> x = new HashSet();
        
        for (DfaState state : dfa.states) {
            for (Edge edge : state.edges) {
                if (edge.label == i && a.contains(edge.stateTo)) {
                    if (!x.contains(edge.stateFrom)) {
                        x.add(edge.stateFrom);
                    }
                }
            }
        }

        return x;
    }
}
