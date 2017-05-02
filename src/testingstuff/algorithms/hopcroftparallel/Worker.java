package testingstuff.algorithms.hopcroftparallel;

import java.util.HashSet;
import java.util.Set;
import testingstuff.data.DfaState;

public class Worker extends Thread {
    
    public Set<DfaState>
            y,
            x,
            toRemoveFromP = null,
            toRemoveFromW = null;
    
    public Set<Set<DfaState>>
            w,
            toAddToP = new HashSet(),
            toAddToW = new HashSet();
    
    public Worker(Set<DfaState> y, Set<DfaState> x, Set<Set<DfaState>> w) {
        this.x = x;
        this.y = y;
        this.w = w;
    }
    
    @Override
    public void run() {
        Set<DfaState> intersection = new HashSet(y);
        intersection.retainAll(x);

        Set<DfaState> difference = new HashSet(y);
        difference.removeAll(x);
        
        if (!intersection.isEmpty() && !difference.isEmpty()) {
            toRemoveFromP = y;
            toAddToP.add(intersection);
            toAddToP.add(difference);
            
            if (w.contains(y)) {
                toRemoveFromW = y;
                toAddToW.add(intersection);
                toAddToW.add(difference);                
            } else {
                if (intersection.size() <= difference.size()) {
                    toAddToW.add(intersection);
                } else {
                    toAddToW.add(difference);
                }
            }
        }
    }
    
}
