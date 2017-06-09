package testingstuff.algorithms;

import java.util.*;

import testingstuff.data.Dfa;
import testingstuff.data.DfaState;
import testingstuff.data.Edge;

public class HopcroftSequential {

    public List<Set<DfaState>> run(Dfa dfa) {
        Thread.currentThread().setPriority(Thread.MAX_PRIORITY);
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
                if (x.isEmpty()) { continue; }

                Set<Set<DfaState>> toRemove = new HashSet();
                Set<Set<DfaState>> toAdd = new HashSet();

                for (Set<DfaState> y : p) {
                    if (y.size() == 1) { continue; }

                    Set<DfaState> intersection = new HashSet(y);
                    intersection.retainAll(x);

                    Set<DfaState> difference = new HashSet(y);
                    difference.removeAll(x);

                    if (!intersection.isEmpty() && !difference.isEmpty()) {
                        toRemove.add(y);
                        toAdd.add(intersection);
                        toAdd.add(difference);

                        if (w.contains(y)) {
                            w.remove(y);
                            w.add(intersection);
                            w.add(difference);
                        } else {
                            if (intersection.size() <= difference.size()) {
                                w.add(intersection);
                            } else {
                                w.add(difference);
                            }
                        }
                    }
                }

                for (Set<DfaState> state : toRemove) {
                    p.remove(state);
                }
                for (Set<DfaState> state : toAdd) {
                    p.add(state);
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