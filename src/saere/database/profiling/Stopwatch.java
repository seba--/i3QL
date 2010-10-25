package saere.database.profiling;

public final class Stopwatch {
	
	private long start;
	
	public void start() {
		start = System.nanoTime();
	}
	
	public long stop() {
		return System.nanoTime() - start;
	}
}
