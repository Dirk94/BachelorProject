package testingstuff.algorithms;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import testingstuff.data.Dfa;
import testingstuff.data.DfaState;
import testingstuff.data.Edge;

public class TewariSequential {
    
    public HashMap<DfaState, Integer> run(Dfa dfa) {
        HashMap<DfaState, Integer> blockNo = new HashMap();        
        
        for (DfaState finalState : dfa.getFinalStates()) {           
            blockNo.put(finalState, 1);
        }
        for (DfaState nonFinalState : dfa.getNonFinalStates()) {
            blockNo.put(nonFinalState, 2);
        }
        
        int numberOfBlocks = 2;
        do {
            for (int i=0; i<dfa.alphabetSize; i++) {
                HashMap<DfaState, List<Integer>> labels = new HashMap();
                for (DfaState state : dfa.states) {
                    Integer b1 = blockNo.get(state);
                    
                    DfaState b2State = getB2State(state, i);
                    Integer b2 = blockNo.get(b2State);
                                        
                    List<Integer> newLabel = new ArrayList();
                    newLabel.add(b1);
                    newLabel.add(b2);
                    
                    labels.put(state, newLabel);
                }
                
                relabel(blockNo, labels);                
            }
            
            int newNumberOfBlocks = countNumberOfBlocks(blockNo);
            if (newNumberOfBlocks == numberOfBlocks) {
                break;
            }
            numberOfBlocks = newNumberOfBlocks;
        } while(true);
        
        return blockNo;
    }
    
    private void relabel(HashMap<DfaState, Integer> blockNo, HashMap<DfaState, List<Integer>> labels) {
        int currentLabel = 1;
        HashMap<List<Integer>, Integer> labelValues = new HashMap();
        for (Map.Entry<DfaState,List<Integer>> entry : labels.entrySet()) {
            DfaState state = entry.getKey();
            List<Integer> labelSet = entry.getValue();
            
            if (!labelValues.containsKey(labelSet)) {
                blockNo.put(state, currentLabel);
                labelValues.put(labelSet, currentLabel);
                currentLabel++;
            } else {
                int label = labelValues.get(labelSet);
                blockNo.put(state, label);
            }
        }
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
