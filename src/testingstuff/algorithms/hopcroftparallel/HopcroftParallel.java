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

    public static int THREADS = 1;

    public List<Set<DfaState>> run(Dfa dfa) {
        Worker[] workers = new Worker[THREADS];
        for (int i=0; i<THREADS; i++) {
            workers[i] = new Worker();
        }

        List<Set<DfaState>> p = new ArrayList();
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
                if (x.isEmpty()) {
                    continue;
                }

                int activeThreads = THREADS;
                int blockSize;

                if (p.size() < THREADS) {
                    activeThreads = p.size();
                    blockSize = 1;
                } else {
                    blockSize = (int)(Math.ceil(p.size() / (double)activeThreads));
                }

                for (int j=0; j<activeThreads; j++) {
                    workers[j] = new Worker();
                    workers[j].setPriority(Thread.MAX_PRIORITY);
                    workers[j].x = x;
                    workers[j].w = w;
                    workers[j].p = p;
                    workers[j].start = j * blockSize;
                    workers[j].end = (j * blockSize) + blockSize;
                    workers[j].start();
                }

                for (int j=0; j<activeThreads; j++) {
                    try {
                        workers[j].join();
                    } catch(InterruptedException ex) {
                        ex.printStackTrace();
                    }
                }

                p.clear();

                for (int j=0; j<activeThreads; j++) {
                    p.addAll(workers[j].toAdd);
                    w.removeAll(workers[j].toRemoveW);
                    w.addAll(workers[j].toAddW);
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
