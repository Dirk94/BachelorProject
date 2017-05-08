package testingstuff.algorithms.hopcroftparallel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import testingstuff.data.Dfa;
import testingstuff.data.DfaState;
import testingstuff.data.Edge;

public class HopcroftParallel {
    
    public Set<Set<DfaState>> run(Dfa dfa) {
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
                Set<DfaState> x = getX(a, i);
                if (x.isEmpty()) { continue; }
                
                List<Thread> threads = new ArrayList();
                for (Set<DfaState> y : p) {
                    if (y.size() == 1) { continue; }
                    
                    Worker worker = new Worker(x, y, w);
                    worker.start();
                    threads.add(worker);
                }
                
                for (Thread worker : threads) {
                    try {
                        worker.join();
                    } catch(InterruptedException ex) {
                        ex.printStackTrace();
                    }
                }
                
                for (Thread thread : threads) {
                    Worker worker = (Worker) thread;                    
                    if (!worker.somethingChanged) { continue; }
                    
                    for (Set<DfaState> state : worker.toRemove) {
                        p.remove(state);
                    }
                    for (Set<DfaState> state : worker.toAdd) {
                        p.add(state);
                    }
                    for (Set<DfaState> state : worker.toRemoveW) {
                        w.remove(state);
                    }
                    for (Set<DfaState> state : worker.toAddW) {
                        w.add(state);
                    }
                }
            }
        }
        
        return p;
    }
    
    private Set<DfaState> getX(Set<DfaState> a, int i) {
        Set<DfaState> x = new HashSet();
        
        for (DfaState state : a) {
            for (Edge edge : state.incomingEdges) {
                if (edge.label == i) {
                    x.add(edge.stateFrom);
                }
            }
        }

        return x;
    }
    
}
