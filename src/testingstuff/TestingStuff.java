package testingstuff;

import java.util.HashMap;
import testingstuff.data.DfaState;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Set;
import testingstuff.algorithms.HopcroftSequential;
import testingstuff.algorithms.TewariSequential;
import testingstuff.algorithms.hopcroftparallel.HopcroftParallel;
import testingstuff.algorithms.tewariparallel.TewariParallel;
import testingstuff.data.Dfa;
import testingstuff.data.DfaFactory;

public class TestingStuff {

    private static final int NUMBER_OF_TESTS = 10;
    
    private static final long DEFAULT_SEED = 234;
    
    public TestingStuff() {
        DfaFactory factory = new DfaFactory(DEFAULT_SEED);

        Dfa dfas[] = new Dfa[NUMBER_OF_TESTS];            
        for (int i=0; i<NUMBER_OF_TESTS; i++) {
        Dfa dfa = factory.randomDfa(10, 4);
            dfas[i] = dfa;
        }
        
        HopcroftSequential hcSeq = new HopcroftSequential();
        HopcroftParallel hcPar = new HopcroftParallel();
        TewariSequential tSeq = new TewariSequential();
        TewariParallel tPar = new TewariParallel();
        
        long start = System.nanoTime();
        System.out.println("Running");
        for (int i=0; i<NUMBER_OF_TESTS; i++) {
            
            tPar.run(dfas[i]);
                        
            HashMap<DfaState, Integer> result = tSeq.run(dfas[i]);
            System.out.println("\nSeq Result:");
            System.out.println(result);
            
            Set<Set<DfaState>> hcResult = hcSeq.run(dfas[i]);
            System.out.println("\nHopcroft Result:");
            System.out.println(hcResult);
            
            /*Dfa dfa = factory.specialDfa2();
            HashMap<DfaState, Integer> parResult = tPar.run(dfa);
            
            System.out.println("Parallel Result:");
            System.out.println(parResult);
            System.out.println();
            
            HashMap<DfaState, Integer> result = tSeq.run(dfa);
            System.out.println("Sequential Result:");
            System.out.println(result);
                        
            System.exit(0);*/
        }
        long end = System.nanoTime();
        long time = (end - start) / 1000000;
        System.out.println("Finished in " + time + "ms");
    }
    
    public static void main(String[] args) {
        new TestingStuff();
    }
    
    public boolean compare(HashMap<DfaState, Integer> result1, Set<Set<DfaState>> result2) {
        for (Set<DfaState> stateSet : result2) {            
            int expectedLabel = -1;
            for (DfaState state : stateSet) {
                int label = result1.get(state);
                if (expectedLabel == -1) {
                    expectedLabel = label;
                } else {
                    if (expectedLabel != label) {
                        return false;
                    }
                }
            }
        }
        
        return true;
    }
    
}

