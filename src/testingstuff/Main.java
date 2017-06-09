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

    private static final int NUMBER_OF_TESTS = 5;
    
    private static final long DEFAULT_SEED = 65465;

    public Main(String algorithm, int states, int alphabetSize, int nThreads) {
        TewariParallel.THREADS = nThreads;
        HopcroftParallel.THREADS = nThreads;

        DfaFactory factory = new DfaFactory();

        HopcroftSequential hcSeq = new HopcroftSequential();
        HopcroftParallel hcPar = new HopcroftParallel();
        TewariSequential tSeq = new TewariSequential();
        TewariParallel tPar = new TewariParallel();

        System.out.println("states: " + states + ", alphabet: " + alphabetSize + ", threads: " + nThreads);
        System.out.println("Starting " + NUMBER_OF_TESTS + " tests.");
        long totalTime = 0;
        for (int i=0; i<NUMBER_OF_TESTS; i++) {
            Dfa dfa = factory.randomDfa(states, alphabetSize);
            long start = System.nanoTime();

            switch(algorithm) {
                case "HopcroftSequential":
                    hcSeq.run(dfa);
                    break;

                case "HopcroftParallel":
                    hcPar.run(dfa);
                    break;

                case "TewariParallel":
                    tPar.run(dfa);
                    break;

                default:
                    printError();
                    break;
            }

            long end = System.nanoTime();
            long time = (end - start) / 1000000;
            System.out.println("Test " + (i+1) + ": " + time + "ms");
            totalTime += time;
        }

        System.out.println("Average: " + (totalTime / NUMBER_OF_TESTS) + "ms");
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
    
    private static void printError() {
        System.out.println("Usage: ./Main Algorithm=[HopcroftSequential|HopcroftParallel|TewariParallel] States AlphabetSize nThreads");
        System.exit(0);
    }
    
    public static void main(String[] args) {
        if (args.length != 4) {
            printError();
        }
        new Main(args[0], Integer.parseInt(args[1]), Integer.parseInt(args[2]), Integer.parseInt(args[3]));
    }
    
}

