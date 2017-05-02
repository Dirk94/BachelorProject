package testingstuff.algorithms.tewariparallel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import testingstuff.data.Dfa;
import testingstuff.data.DfaState;
import testingstuff.data.Edge;

public class TewariParallel {   
    
    public HashMap<DfaState, Integer> run(Dfa dfa) {
        HashMap<DfaState, Integer> blockNo = new HashMap();               
        
        for (DfaState finalState : dfa.getFinalStates()) {           
            blockNo.put(finalState, 1);
        }
        for (DfaState nonFinalState : dfa.getNonFinalStates()) {
            blockNo.put(nonFinalState, 2);
        }
        
        double n = (double)dfa.states.size();
        double logN = Math.log(n) / Math.log(2);
        double doubleResult =  n / logN;
        int nLogN = (int)(Math.ceil(doubleResult));
        
        int numberOfBlocks = 2;
        do {
            for (int i = 0; i < dfa.alphabetSize; i++) {
                
                HashMap<DfaState, Integer>[] threadLabels = new HashMap[nLogN+1];
                
                AtomicInteger maxBValue = new AtomicInteger(0);
                for (int j = 1; j <= nLogN; j++) {
                    threadLabels[j] = new HashMap();
                    
                    for (int m=(int)(Math.ceil((j-1)*logN)); m<=(int)(Math.ceil(j*logN-1)); m++) {
                        if (m >= dfa.states.size()) { continue; }
                        DfaState state = dfa.statesAsList.get(m);
                        
                        Integer b1 = blockNo.get(state);
                        while(true) {
                            int oldValue = maxBValue.get();
                            if (b1 > oldValue) {
                                if (maxBValue.compareAndSet(oldValue, b1)) {
                                    break;
                                }
                            } else {
                                break;
                            }
                        }
                        
                        DfaState b2State = getB2State(state, i);
                        Integer b2 = blockNo.get(b2State);
                        while(true) {
                            int oldValue = maxBValue.get();
                            if (b2 > oldValue) {
                                if (maxBValue.compareAndSet(oldValue, b2)) {
                                    break;
                                }
                            } else {
                                break;
                            }
                        }

                        int labelNumber = b1 * ((int)n + 1) + b2;     
                        threadLabels[j].put(state, labelNumber);
                    }                    
                }
                
                int k = (int)Math.ceil(((double)maxBValue.get() * (n + 1.0) + (double)maxBValue.get()) / n);
                
                HashMap<Integer, Boolean> PRESENT = new HashMap();
                
                for (int j=1; j<=nLogN; j++) {
                    for (int m=(int)(Math.ceil((j-1)*logN)); m<=(int)(Math.ceil(j*logN-1)); m++) {
                        if (m >= dfa.states.size()) { continue; }
                        
                        Integer label = threadLabels[j].get(dfa.statesAsList.get(m));                        
                        PRESENT.put(label, true);
                    }
                }
                
                int ai[] = new int[nLogN+1];
                for (int j=1; j<=nLogN; j++) {
                    ai[j] = 0;
                    for (int m=(int)((j-1)*(k*logN)+1); m<=(int)(j*k*logN); m++) {
                        if (m > k*n) { continue; }
                        
                        Boolean isPresent = PRESENT.get(m);
                        if (isPresent != null) {
                            ai[j] += 1;
                        }
                    }
                }
                
                // Compute partial sums..
                int[] si = new int[nLogN+1];
                for (int j=1; j<=nLogN; j++) {
                    si[j] = 0;
                    for (int m=0; m<j; m++) {
                        si[j] += ai[m];
                    } 
                }
                
                // Compute new block numbers
                HashMap<Integer, Integer> newBlockNo = new HashMap();
                
                for (int j=1; j<=nLogN; j++) {
                    int initialValue = si[j];
                    for (int m=(int)((j-1)*(k*logN)+1); m<=(int)(j*k*logN); m++) {
                        if (m > k*n) { continue; }
                        
                        Boolean isPresent = PRESENT.get(m);
                        if (isPresent != null) {
                            initialValue++;
                            newBlockNo.put(m, initialValue);
                        }
                        
                    }
                }
                                
                // Update the blockNo according to newBlockNo.
                for (int j=1; j<=nLogN; j++) {
                    for (int m=(int)(Math.ceil((j-1)*logN)); m<=(int)(Math.ceil(j*logN-1)); m++) {
                        if (m >= dfa.states.size()) { continue; }
                        
                        DfaState state = dfa.statesAsList.get(m);
                        Integer label = threadLabels[j].get(state);
                        int newBlockNumber = newBlockNo.get(label);
                        blockNo.put(state, newBlockNumber);
                    }
                }
            }
            
            int newNumberOfBlocks = countNumberOfBlocks(blockNo);
            if (newNumberOfBlocks == numberOfBlocks) {
                break;
            }
            numberOfBlocks = newNumberOfBlocks;
        } while(true);
        
        return blockNo;
    }
    
    private int countNumberOfBlocks(HashMap<DfaState, Integer> blockNo) {        
        List<Integer> distinctLabels = new ArrayList();
        
        for (Integer labels : blockNo.values()) {
            if (!distinctLabels.contains(labels)) {
                distinctLabels.add(labels);
            }
        }
        
        return distinctLabels.size();
    }
    
    private DfaState getB2State(DfaState state, int label) {
        for (Edge edge : state.edges) {
            if (edge.label == label) {
                return edge.stateTo;
            }
        }
        return null;
    }
    
}
