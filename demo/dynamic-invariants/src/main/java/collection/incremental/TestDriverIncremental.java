/**
 * The main driver function for running tests on Ditto.
 */
package collection.incremental;

import java.lang.management.MemoryMXBean;
import java.util.Random;

/**
 * @author aj
 *
 */
public class TestDriverIncremental {
    static Random r = new Random(42);

    /**
     * @param args
     */
    public static void main(String[] args) {
        if (args.length < 2) {
            System.out
                    .println("Usage: TestDriverIncremental.main [num elems] [which test] [skip invariants]");
            System.exit(1);
        }
        int n = Integer.parseInt(args[0]);
        int which = Integer.parseInt(args[1]);
        boolean do_invariants = true;
        if (args.length > 2)
            do_invariants = Integer.parseInt(args[2]) != 1;

        // int off = 43;
        // for (int q = off ; q < off + 31 ; q++) {
        // System.out.println("Q is " + q);
        // runRBTree(q, 100, do_invariants);
        // }

        System.out.println("Do invariants is " + do_invariants);
        System.out.println("n is " + n);
        long start = 0;
        switch (which) {
            case 0:
                start = runList(n, 10000, do_invariants);
                break;
            case 1:
                //start = runTree(n, 10000, do_invariants);
                break;
            case 2:
                //start = runAssocList(n, 10000, do_invariants);
                break;
            case 3:
                //start = runRBTree(n, 10000, do_invariants);
                break;
            case 4:
                //start = runHash(n, 10000, do_invariants);
                break;
        }

        long end = System.currentTimeMillis();
        System.out.println("Total time: " + (end - start) + " ms");

        System.out.println("Done with all operations.");
    }


    private static long runList(int n, int reps, boolean do_invariants) {
        System.out.println("Performing ordered list tests");
        OrderedIntList l = new OrderedIntList();
        System.out.println("Inserting " + n + " elements into list...");
        l.doInvariants = do_invariants;
        int[] added = new int[n];

        MemoryMXBean memoryMXBean = java.lang.management.ManagementFactory.getMemoryMXBean();
        //memoryMXBean.setVerbose(true)
        System.out.println("performing gc");
        memoryMXBean.gc();
        long usedBefore = memoryMXBean.getHeapMemoryUsage().getUsed();



        for (int i = 0; i < n; i++) {
            l.insert(i);
            added[i]++;
        }


        memoryMXBean.gc();
        long usedAfter1 = memoryMXBean.getHeapMemoryUsage().getUsed();
        double used1= ((double)(usedAfter1 - usedBefore) / 1024);
        System.out.println("Used memory (kb): " + used1);

        System.out.println("Starting real test");
        int adds = 0;
        long start = System.currentTimeMillis();
        for (int j = 0; j < reps; j++) {
            // if (j%10 == 0) System.out.println("Size is " + l.size + " with +"
            // + (2*adds - j) + " adds");
            int which = rnd(4);
            switch (which) {
                case 0:
                    int toremove = rnd(n);
                    while (added[toremove] == 0) {
                        toremove = rnd(n);
                    }
                    // System.out.println("Removing " + toremove);
                    added[toremove]--;
                    l.remove(toremove);
                    break;
                case 1:
                    // System.out.println("Shifting");
                    added[l.shift()]--;
                    break;
                default:
                    adds++;
                    int toadd = rnd(n);
                    added[toadd]++;
                    // System.out.println("Adding " + toadd);
                    l.insert(toadd);
                    break;
            }
        }
        System.out.println(adds + " adds of " + reps + " total " + l.size());
        // l.barf();

        memoryMXBean.gc();
        long usedAfter = memoryMXBean.getHeapMemoryUsage().getUsed();
        double used= ((double)(usedAfter - usedBefore) / 1024);
        System.out.println("Used memory (kb): " + used);

        return start;
    }



    private static int rnd(int n) {
        return (r.nextInt(n));
    }


}
