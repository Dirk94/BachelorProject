package testingstuff.algorithms.hopcroftparallel;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import testingstuff.data.DfaState;

public class Worker extends Thread {

    public int start, end;

    public Set<DfaState> x;
    
    public Set<Set<DfaState>> w, toRemove, toAdd, toRemoveW, toAddW;

    public List<Set<DfaState>> p;

    public Worker() {
        toRemove = new HashSet();
        toAdd = new HashSet();                
        toRemoveW = new HashSet();
        toAddW = new HashSet();
    }
    
    @Override
    public void run() {
        for (int i=start; i<end; i++) {
            if (i >= p.size()) { continue; }

            Set<DfaState> y = p.get(i);
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
                    toRemoveW.add(y);
                    toAddW.add(intersection);
                    toAddW.add(difference);
                } else {
                    if (intersection.size() <= difference.size()) {
                        toAddW.add(intersection);
                    } else {
                        toAddW.add(difference);
                    }
                }
            }
        }
    }
    
}
