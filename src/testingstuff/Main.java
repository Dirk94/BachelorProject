package testingstuff;

import java.util.*;

import testingstuff.data.DfaState;

import java.util.Map.Entry;

import testingstuff.algorithms.HopcroftSequential;
import testingstuff.algorithms.TewariSequential;
import testingstuff.algorithms.hopcroftparallel.HopcroftParallel;
import testingstuff.algorithms.tewariparallel.TewariParallel;
import testingstuff.data.Dfa;
import testingstuff.data.DfaFactory;

public class Main {

    private static final int NUMBER_OF_TESTS = 100;
    
    private static final long DEFAULT_SEED = 65465;

    public Main(String algorithm, int states, int alphabetSize) {
        DfaFactory factory = new DfaFactory();

        Dfa dfas[] = new Dfa[NUMBER_OF_TESTS];            
        for (int i=0; i<NUMBER_OF_TESTS; i++) {
        Dfa dfa = factory.randomDfa(states, alphabetSize);
            dfas[i] = dfa;
            System.out.println("States: " + dfa.states.size());
        }

        HopcroftSequential hcSeq = new HopcroftSequential();
        HopcroftParallel hcPar = new HopcroftParallel();
        TewariSequential tSeq = new TewariSequential();
        TewariParallel tPar = new TewariParallel();

        long start = System.nanoTime();
        System.out.println("Running");
        for (int i=0; i<NUMBER_OF_TESTS; i++) {
            switch(algorithm) {
                case "HopcroftSequential":
                    hcSeq.run(dfas[i]);
                    break;
                
                case "HopcroftParallel":
                    List<Set<DfaState>> r1 = hcPar.run(dfas[i]);
                    HashMap<DfaState, Integer> r2 = tPar.run(dfas[i]);
                    System.out.println("Result: " + compare(r2, r1));
                    break;
                    
                case "TewariSequential":
                    tSeq.run(dfas[i]);
                    break;
                    
                case "TewariParallel":
                    tPar.run(dfas[i]);
                    break;
                    
                default:
                    printError();
            }
        }
        long end = System.nanoTime();
        long time = (end - start) / 1000000;
        System.out.println("Finished in " + time + "ms");
    }
    
    public boolean compare(HashMap<DfaState, Integer> result1, List<Set<DfaState>> result2) {
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
    
    private static void printError() {
        System.out.println("Usage: ./Main Algorithm=[HopcroftSequential|TewariSequential|HopcroftParallel|TewariParallel] States AlphabetSize");
        System.exit(0);
    }
    
    public static void main(String[] args) {
        if (args.length != 3) {
            printError();
        }
        new Main(args[0], Integer.parseInt(args[1]), Integer.parseInt(args[2]));
    }
    
}

