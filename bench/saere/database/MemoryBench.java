package saere.database;

import org.junit.BeforeClass;
import org.junit.Test;

import saere.database.Database;
import saere.database.DatabaseTest;
import saere.database.Factbase;
import saere.database.Stopwatch;
import saere.database.TrieDatabase;
import saere.database.index.FullFlattener;
import saere.database.index.ShallowFlattener;
import saere.database.index.DefaultTrieBuilder;

/**
 * Estimates memory consumption of {@link Database}s.
 * 
 * @author David Sullivan
 * @version 0.1, 12/6/2010
 */
public final class MemoryBench {
	
	private static final String TEST_FILE = DatabaseTest.GLOBAL_TEST_FILE;
	private static final Factbase FACTS = Factbase.getInstance();
	
	@BeforeClass
	public static void info() {
		System.out.println("Test file " + TEST_FILE);
	}
	
	@Test
	public void reference() {
		System.out.println("\nReference database");
		Stopwatch sw = new Stopwatch();
		FACTS.read(TEST_FILE);
		sw.printElapsed("Reading " + FACTS.size() + " facts");
		printMemoryConsumption(estimateMemoryConsumption());
		
		Database reference = new ReferenceDatabase();
		((ReferenceDatabase) reference).allowDuplicates(true);
		sw = new Stopwatch();
		reference.fill();
		sw.printElapsed("Filling the reference database");
		
		double size = FACTS.size();
		FACTS.drop();
		System.out.println("Dropping the factbase");
		long memory = estimateMemoryConsumption();
		printMemoryConsumption(memory);
		System.out.println("Ratio of number of terms to memory consumption: " + (size / (memory / 1024)));
		reference.drop();
	}
	
	@Test
	public void simpleShallow() {
		System.out.println("\nSimple shallow trie database");
		Stopwatch sw = new Stopwatch();
		FACTS.read(TEST_FILE);
		sw.printElapsed("Reading " + FACTS.size() + " facts");
		printMemoryConsumption(estimateMemoryConsumption());
		
		Database simpleShallow = new TrieDatabase(new DefaultTrieBuilder(new ShallowFlattener(), 120));
		sw = new Stopwatch();
		simpleShallow.fill();
		sw.printElapsed("Filling the simple shallow trie database");
		
		double size = FACTS.size();
		FACTS.drop();
		System.out.println("Dropping the factbase");
		long memory = estimateMemoryConsumption();
		printMemoryConsumption(memory);
		System.out.println("Ratio of number of terms to memory consumption: " + (size / (memory / 1024)));
		simpleShallow.drop();
	}
	
	@Test
	public void simpleFull() {
		System.out.println("\nSimple full trie database");
		Stopwatch sw = new Stopwatch();
		FACTS.read(TEST_FILE);
		sw.printElapsed("Reading " + FACTS.size() + " facts");
		printMemoryConsumption(estimateMemoryConsumption());
		
		Database simpleFull = new TrieDatabase(new DefaultTrieBuilder(new FullFlattener(), 120));
		sw = new Stopwatch();
		simpleFull.fill();
		sw.printElapsed("Filling the simple full database");
		
		double size = FACTS.size();
		FACTS.drop();
		System.out.println("Dropping the factbase");
		long memory = estimateMemoryConsumption();
		printMemoryConsumption(memory);
		System.out.println("Ratio of number of terms to memory consumption: " + (size / (memory / 1024)));
		simpleFull.drop();
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
