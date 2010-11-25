package saere.database.profiling;

/**
 * A stopwatch for time measuring based on {@link System#nanoTime()}.
 * 
 * @author David Sullivan
 * @version 1.01, 10/26/2010
 * @see saere.database.Stopwatch
 */
public final class Stopwatch {
	
	private long start;
	
	/**
	 * Creates and starts a stopwatch.
	 */
	public Stopwatch() {
		start();
	}
	
	/**
	 * Starts the stopwatch. 
	 * (Not required after constructor or {@link #stop()}).
	 */
	public void start() {
		start = System.nanoTime();
	}
	
	/**
	 * Returns the elapsed time in nanoseconds and restarts the stopwatch.
	 * 
	 * @return The elapsed time in nanoseconds.
	 */
	public long stop() {
		long stop = System.nanoTime() - start;
		start();
		return stop / 1000;
	}
}
