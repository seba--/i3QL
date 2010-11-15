package saere.database;

/**
 * Measures the elapsed time for a task and prints it to the console.
 * <p/>
 * {@link System#nanoTime()} is used to measure time. Results are printed in milliseconds.
 *
 * @author Sebastian Hartte
 * @version $Date: 2010-03-21 13:59:57 +0100 (Sun, 21 Mar 2010) $ $Rev: 1328 $
 * @since 10.02.2010 02:11:07
 */
public final class Stopwatch {

    private long start;

    /**
     * Creates a stopwatch and starts it.
     */
    public Stopwatch() {
        reset();
    }

    /**
     * Resets the stopwatcha and returns the currently elapsed time.
     *
     * @return The elapsed time.
     */
    public long reset() {
        long elapsed = System.nanoTime() - start;
        start = System.nanoTime();
        return elapsed;
    }

    /**
     * Prints the elapsed time since the start or last reset to the console.
     *
     * @param taskName The name of the last task, will be printed on the console.
     */
    public void printElapsed(String taskName) {
        long elapsed = System.nanoTime() - start;
        System.out.println(taskName + " took " + (elapsed / 1000) + " microseconds");
    }

    /**
     * Prints the elapsed time since the start or last reset to the console and resets the timer.
     *
     * @param taskName The name of the last task, will be printed on the console.
     */
    public void printElapsedAndReset(String taskName) {
        printElapsed(taskName);
        reset();
    }

}
