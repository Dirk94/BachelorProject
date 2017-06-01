package testingstuff.algorithms.tewariparallel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import testingstuff.data.Dfa;
import testingstuff.data.DfaState;
import testingstuff.data.Edge;

public class TewariParallel {   

    public static int THREADS = 1;
    
    public HashMap<DfaState, Integer> run(final Dfa dfa) {
        final HashMap<DfaState, Integer> blockNo = new HashMap();               
        
        for (DfaState finalState : dfa.getFinalStates()) {           
            blockNo.put(finalState, 1);
        }
        for (DfaState nonFinalState : dfa.getNonFinalStates()) {
            blockNo.put(nonFinalState, 2);
        }   
        
        // Some much used calculations.
        final double n = (double)dfa.states.size();
        final double logN = Math.log(n) / Math.log(2);
        final double doubleResult =  n / logN;
        final int nLogN = (int)(Math.ceil(doubleResult));
        if (dfa.states.size() == 1) {
            return blockNo;
        }

        Thread[] threads = new Thread[nLogN];
        
        int numberOfBlocks = 2;
        do {
            for (int i = 0; i < dfa.alphabetSize; i++) {
                final int finalI = i;
                final HashMap<DfaState, Integer>[] threadLabels = new HashMap[nLogN+1];
                
                final AtomicInteger maxBValue = new AtomicInteger(0);
                ExecutorService pool = Executors.newFixedThreadPool(TewariParallel.THREADS);
                for (int j = 1; j <= nLogN; j++) {
                    final int jFinal = j;
                    threadLabels[jFinal] = new HashMap();
                    
                    threads[j-1] = new Thread() {
                        @Override
                        public void run() {
                            for (int m = (int) (Math.ceil((jFinal - 1) * logN)); m <= (int) (Math.ceil(jFinal * logN - 1)); m++) {
                                if (m >= dfa.states.size()) {
                                    continue;
                                }
                                DfaState state = dfa.statesAsList.get(m);

                                Integer b1 = blockNo.get(state);
                                while (true) {
                                    int oldValue = maxBValue.get();
                                    if (b1 > oldValue) {
                                        if (maxBValue.compareAndSet(oldValue, b1)) {
                                            break;
                                        }
                                    } else {
                                        break;
                                    }
                                }

                                DfaState b2State = getB2State(state, finalI);
                                Integer b2 = blockNo.get(b2State);
                                while (true) {
                                    int oldValue = maxBValue.get();
                                    if (b2 > oldValue) {
                                        if (maxBValue.compareAndSet(oldValue, b2)) {
                                            break;
                                        }
                                    } else {
                                        break;
                                    }
                                }

                                int labelNumber = b1 * ((int) n + 1) + b2;
                                threadLabels[jFinal].put(state, labelNumber);

                            }
                        }
                    };
                    pool.execute(threads[j-1]);
                }

                pool.shutdown();
                try {
                    pool.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
                } catch(InterruptedException e) {
                    e.printStackTrace();
                }
                               
                final int k = (int)Math.ceil(((double)maxBValue.get() * (n + 1.0) + (double)maxBValue.get()) / n);
                
                final ConcurrentHashMap<Integer, Boolean> PRESENT = new ConcurrentHashMap();

                pool = Executors.newFixedThreadPool(TewariParallel.THREADS);
                for (int j = 1; j <= nLogN; j++) {
                    final int finalJ = j;
                    threads[j - 1] = new Thread() {
                        @Override
                        public void run() {

                            for (int m = (int) (Math.ceil((finalJ - 1) * logN)); m <= (int) (Math.ceil(finalJ * logN - 1)); m++) {
                                if (m >= dfa.states.size()) {
                                    continue;
                                }

                                Integer label = threadLabels[finalJ].get(dfa.statesAsList.get(m));
                                PRESENT.put(label, true);
                            }
                        }
                    };
                    pool.execute(threads[j-1]);
                }
                pool.shutdown();
                try {
                    pool.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
                } catch(InterruptedException e) {
                    e.printStackTrace();
                }

                pool = Executors.newFixedThreadPool(TewariParallel.THREADS);
                final int ai[] = new int[nLogN+1];
                for (int j=1; j<=nLogN; j++) {
                    final int finalJ = j;
                    threads[j-1] = new Thread() {
                        @Override
                        public void run() {        
                            ai[finalJ] = 0;
                            for (int m = (int) ((finalJ - 1) * (k * logN) + 1); m <= (int) (finalJ * k * logN); m++) {
                                if (m > k * n) {
                                    continue;
                                }

                                Boolean isPresent = PRESENT.get(m);
                                if (isPresent != null) {
                                    ai[finalJ] += 1;
                                }
                            }
                        }
                    };
                    pool.execute(threads[j-1]);
                }

                pool.shutdown();
                try {
                    pool.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
                } catch(InterruptedException e) {
                    e.printStackTrace();
                }
                               
                // Compute partial sums..
                pool = Executors.newFixedThreadPool(TewariParallel.THREADS);
                final int[] si = new int[nLogN + 1];
                for (int j = 1; j <= nLogN; j++) {
                    final int finalJ = j;
                    threads[j - 1] = new Thread() {
                        @Override
                        public void run() {
                            si[finalJ] = 0;
                            for (int m = 0; m < finalJ; m++) {
                                si[finalJ] += ai[m];
                            }
                        }
                    };
                    pool.execute(threads[j - 1]);
                }

                pool.shutdown();
                try {
                    pool.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
                } catch(InterruptedException e) {
                    e.printStackTrace();
                }
                                
                // Compute new block numbers
                final ConcurrentHashMap<Integer, Integer> newBlockNo = new ConcurrentHashMap();

                pool = Executors.newFixedThreadPool(TewariParallel.THREADS);
                for (int j = 1; j <= nLogN; j++) {
                    final int finalJ = j;
                    threads[j - 1] = new Thread() {
                        @Override
                        public void run() {

                            int initialValue = si[finalJ];
                            for (int m = (int) ((finalJ - 1) * (k * logN) + 1); m <= (int) (finalJ * k * logN); m++) {
                                if (m > k * n) {
                                    continue;
                                }

                                Boolean isPresent = PRESENT.get(m);
                                if (isPresent != null) {
                                    initialValue++;
                                    newBlockNo.put(m, initialValue);
                                }

                            }
                        }
                    };
                    pool.execute(threads[j-1]);
                }

                pool.shutdown();
                try {
                    pool.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
                } catch(InterruptedException e) {
                    e.printStackTrace();
                }

                pool = Executors.newFixedThreadPool(TewariParallel.THREADS);
                // Update the blockNo according to newBlockNo.
                for (int j=1; j<=nLogN; j++) {
                    final int finalJ = j;
                    threads[j-1] = new Thread() {
                        @Override
                        public void run() {
                            for (int m=(int)(Math.ceil((finalJ-1)*logN)); m<=(int)(Math.ceil(finalJ*logN-1)); m++) {
                                if (m >= dfa.states.size()) { continue; }

                                DfaState state = dfa.statesAsList.get(m);
                                Integer label = threadLabels[finalJ].get(state);
                                int newBlockNumber = -1;
                                newBlockNumber = newBlockNo.get(label);
                                
                                blockNo.put(state, newBlockNumber);
                            }
                        }
                    };
                    pool.execute(threads[j-1]);
                }
                
                // Wait for all threads.
                pool.shutdown();
                try {
                    pool.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
                } catch(InterruptedException e) {
                    e.printStackTrace();
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
    
    private void waitForThreads(Thread[] threads) {
        for (int i=0; i<threads.length; i++) {
            try {
                threads[i].join();
            } catch(InterruptedException ex) {
                ex.printStackTrace();
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