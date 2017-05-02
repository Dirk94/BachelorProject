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
        System.out.println("n: " + n + ", logN: " + logN);
        System.out.println("DoubleResult: " + doubleResult);
        System.out.println("nLogN: " + nLogN);
        
        int iterations = 0;
        
        int numberOfBlocks = 2;
        do {
            iterations++;
            for (int i = 0; i < dfa.alphabetSize; i++) {
                
                HashMap<DfaState, Integer>[] threadLabels = new HashMap[nLogN+1];
                
                AtomicInteger maxBValue = new AtomicInteger(0);
                for (int j = 1; j <= nLogN; j++) {
                    //HashMap<DfaState, Integer> labels = new HashMap();
                    threadLabels[j] = new HashMap();
                    
                    for (int m=(int)(Math.ceil((j-1)*logN)); m<=(int)(Math.ceil(j*logN-1)); m++) {
                        if (m >= dfa.states.size()) { continue; }
                        DfaState state = dfa.statesAsList.get(m); // Maybe a lock if it is not atomic..
                        
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
                        System.out.println("b1, b2: " + b1 + ", " + b2 + " = " + labelNumber);
                        threadLabels[j].put(state, labelNumber);
                    }                    
                }
                
                int k = (int)Math.ceil(((double)maxBValue.get() * (n + 1.0) + (double)maxBValue.get()) / n);
                System.out.println("K: " + k);
                
                HashMap<Integer, Boolean> PRESENT = new HashMap();
                
                for (int j=1; j<=nLogN; j++) {
                    for (int m=(int)((j-1)*(k*logN)+1); m<=(int)(j*k*logN); m++) {
                        if (m > k*n) { continue; }
                        
                        System.out.println("(" + j + "): " + m);
                        Integer label = threadLabels[j].get(dfa.statesAsList.get(m));
                        System.out.println("Label is: " + label);
                    }
                }
                
                for (int j = 1; j <= nLogN; j++) {
                    System.out.println("Thread(" + j + "): " + threadLabels[j]);
                    for (Map.Entry<DfaState, Integer> entry : threadLabels[j].entrySet()) {
                        DfaState state = entry.getKey();
                        Integer label = entry.getValue();
                
                        //blockNo.put(state, label);
                        
                    }
                }
                
                // Use parallel hashing to map the n labels to [1..O(n)]
                // a number to which a state's label gets mapped to its
                // new block_no.
                
                // Reduce the range of block_no from O(n) to n.
            }
            
            int newNumberOfBlocks = countNumberOfBlocks(blockNo);
            if (newNumberOfBlocks == numberOfBlocks) {
                //break;
            }
            if (iterations >= 3) {
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
