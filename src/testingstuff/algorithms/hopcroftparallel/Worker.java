package testingstuff.algorithms.hopcroftparallel;

import java.util.HashSet;
import java.util.Set;
import testingstuff.data.DfaState;

public class Worker extends Thread {
    
    public boolean somethingChanged;
    
    public Set<DfaState> x, y;
    
    public Set<Set<DfaState>> w, toRemove, toAdd, toRemoveW, toAddW;
    
    public Worker(Set<DfaState> x, Set<DfaState> y, Set<Set<DfaState>> w) {
        // Local copies of the data.
        this.x = x;
        this.y = y;
        this.w = w;
        
        toRemove = new HashSet();
        toAdd = new HashSet();                
        toRemoveW = new HashSet();
        toAddW = new HashSet();
        
        somethingChanged = false;
    }
    
    @Override
    public void run() {        
        Set<DfaState> intersection = new HashSet(y);
        intersection.retainAll(x);

        Set<DfaState> difference = new HashSet(y);
        difference.removeAll(x);

        if (!intersection.isEmpty() && !difference.isEmpty()) {
            somethingChanged = true;
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
