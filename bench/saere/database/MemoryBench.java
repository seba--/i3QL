package saere.database;

import saere.database.index.DefaultTrieBuilder;
import saere.database.index.FullFlattener;
import saere.database.index.ShallowFlattener;
import saere.database.profiling.Profiler;
import saere.database.profiling.Profiler.Mode;

/**
 * Estimates memory consumption of {@link Database}s. Each 'test' should be run alone.
 * 
 * @author David Sullivan
 * @version 0.1, 12/6/2010
 */
public final class MemoryBench {
	
	private static final int MAP_THRESHOLD = DatabaseTest.GLOBAL_MAP_THRESHOLD;
	private static final Factbase FACTS = Factbase.getInstance();
	
	private static String testFile = DatabaseTest.TEST_FILES[2];
	private static String profilesFile;
	private static boolean useProfiles;
	
	/**
	 * Call JAR with parameters<br>
	 * <br>
	 * <tt><i>test-file</i> (m|r|s|f) [<i>profiles-file</i>]</tt><br>
	 * <br>
	 * whereas memory consumption is measured depending on the second parameter:
	 * <ul>
	 * <li><tt>n</tt> without any facts</li>
	 * <li><tt>m</tt> only with a filled factbase</li>
	 * <li><tt>r</tt> reference database</li>
	 * <li><tt>s</tt> STF database</li>
	 * <li><tt>s</tt> FTF trie database</li>
	 * </ul>
	 * 
	 * @param args The arguments.
	 */
	public static void main(String[] args) {
		
		String mode = null;
		if (args.length == 2) {
			testFile = args[0];
			mode = args[1];
		} else if (args.length == 3) {
			testFile = args[0];
			mode = args[1];
			useProfiles = true;
			profilesFile = args[2];
		} else {
			System.err.println("Invalid parameters");
			System.exit(1);
		}
		
		MemoryBench bench = new MemoryBench();
		if (mode.equals("n")) {
			bench.memoryWithoutFacts();
		} else if (mode.equals("m")) {
			bench.memoryWithFactsOnly();
		} else if (mode.equals("r")) {
			bench.reference();
		}  else if (mode.equals("s")) {
			bench.simpleShallow();
		}  else if (mode.equals("f")) {
			bench.simpleFull();
		}
	}
	
	public void memoryWithoutFacts() {
		System.out.println("Memory consumption w/o factbase");
		printMemoryConsumption(estimateMemoryConsumption());
	}
	
	public void memoryWithFactsOnly() {
		FACTS.read(testFile);
		System.out.println("Memory consumption with factbase only");
		printMemoryConsumption(estimateMemoryConsumption());
		FACTS.drop();
	}
	
	public void reference() {
		Database reference = new ReferenceDatabase();
		((ReferenceDatabase) reference).allowDuplicates(true);
		System.out.println("\nReference database");
		estimateDatabaseSize(reference);
	}
	
	public void simpleShallow() {
		System.out.println("\nSimple shallow trie database");
		Database simpleShallow = new TrieDatabase(new DefaultTrieBuilder(new ShallowFlattener(), MAP_THRESHOLD));
		estimateDatabaseSize(simpleShallow);
	}
	
	public void simpleFull() {
		System.out.println("\nSimple full trie database");
		Database simpleFull = new TrieDatabase(new DefaultTrieBuilder(new FullFlattener(), MAP_THRESHOLD));
		estimateDatabaseSize(simpleFull);
	}
	
	private static void estimateDatabaseSize(Database database) {		
		// Profiles?
		if (useProfiles) {
			Profiler.getInstance().loadProfiles(profilesFile);
			Profiler.getInstance().setMode(Mode.USE);
		}
		
		FACTS.read(testFile);
		database.fill();
		FACTS.drop();
		printMemoryConsumption(estimateMemoryConsumption());
		database.drop();
	}
	
	private static long estimateMemoryConsumption() {
		System.gc();
		System.runFinalization();
		System.gc();
		return Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
	}
	
	private static void printMemoryConsumption(long memory) {
		if (memory < 1024) {
			System.out.println("Estimated memory consumption: " + memory + "B");
		} else if (memory < 1048576) {
			memory /= 1024;
			System.out.println("Estimated memory consumption: " + memory + "KB");
		} else {
			memory /= 1048576;
			System.out.println("Estimated memory consumption: " + memory + "MB");
		}
	}
}
