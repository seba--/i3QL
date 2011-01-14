package saere.database.util;

/**
 * Measures the elapsed time for a task and prints it to the console.
 * <p/>
 * {@link System#nanoTime()} is used to measure time. Results are printed in milliseconds.
 *
 * @author Sebastian Hartte
 * @author David Sullivan
 * @version 1.1, 12/10/2010
 */
// Added units...
public final class Stopwatch {

	private final String unitName;
	private final int divisor;
	
    private long start;

    /**
     * Creates a stopwatch and starts it.
     */
    public Stopwatch() {
        this(Unit.MICROSECONDS);
    }
    
    public Stopwatch(Unit unit) {
    	if (unit == Unit.NANOSECONDS) {
    		divisor = 1;
    		unitName = "nanoseconds";
    	} else if (unit == Unit.MICROSECONDS) {
    		divisor = 1000;
    		unitName = "microseconds";
    	} else if (unit == Unit.MILLISECONDS) {
    		divisor = 1000000;
    		unitName = "milliseconds";
    	} else if (unit == Unit.SECONDS) {
    		divisor = 1000000000;
    		unitName = "seconds";
    	} else {
    		divisor = 60 * 1000000000;
    		unitName = "minutes";
    	}
    	reset();
    }

    /**
     * Resets the stopwatcha and returns the currently elapsed time.
     *
     * @return The elapsed time.
     */
    public long reset() {
        long elapsed = (System.nanoTime() - start) / divisor;
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
        System.out.println(taskName + " took " + (elapsed / divisor) + " " + unitName);
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

    public enum Unit {
    	MINUTES,
    	SECONDS,
    	MILLISECONDS,
    	MICROSECONDS,
    	NANOSECONDS
    }
}
