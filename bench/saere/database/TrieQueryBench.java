package saere.database;

import java.util.Iterator;

import org.junit.BeforeClass;
import org.junit.Test;

import saere.State;
import saere.Term;
import saere.database.BATTestQueries;
import saere.database.Database;
import saere.database.DatabaseTest;
import saere.database.Factbase;
import saere.database.TrieDatabase;
import saere.database.index.FullFlattener;
import saere.database.index.ShallowFlattener;
import saere.database.index.DefaultTrieBuilder;
import saere.database.util.Stopwatch;

/**
 * Class to measure queries directly to tries.
 * 
 * @author David Sullivan
 * @version 0.1, 12/6/2010
 */
public final class TrieQueryBench {
	
private static final int MAP_THRESHOLD = 120;
	
	private static final Factbase FACTS = Factbase.getInstance();
	private static final Database REFERENCE_DB = new ReferenceDatabase();
	private static final Database SHALLOW_DB = new TrieDatabase(new DefaultTrieBuilder(new ShallowFlattener(), MAP_THRESHOLD));
	private static final Database FULL_DB = new TrieDatabase(new DefaultTrieBuilder(new FullFlattener(), MAP_THRESHOLD));
	
	private static final int TEST_RUNS = 3;
	
	@BeforeClass
	public static void initialize() {
		Stopwatch sw = new Stopwatch();
		FACTS.read(DatabaseTest.GLOBAL_TEST_FILE);
		sw.printElapsedAndReset("Reading the factbase");
		((ReferenceDatabase) REFERENCE_DB).allowDuplicates(true);
		REFERENCE_DB.fill();
		sw.printElapsedAndReset("Filling the reference database");
		SHALLOW_DB.fill();
		sw.printElapsedAndReset("Filling the (shallow) trie database");
		FULL_DB.fill();
		sw.printElapsedAndReset("Filling the (full) trie database");
		//FactsPrinter.print("c:/users/leaf/desktop/facts.txt");
	}
	
	@Test
	public void go() {
		for (int i = 0; i < TEST_RUNS; i++) {
			System.out.println("\nRun " + (i + 1) + "...");
			
			for (Term query : BATTestQueries.ALL_QUERIES) {
				System.out.println("\nQuery " + query);
				
				Iterator<Term> iter;
				int counter;
				Stopwatch sw;
				
				State state = query.manifestState();
				
				sw = new Stopwatch();
				counter = 0;
				iter = REFERENCE_DB.query(query);
				while (iter.hasNext()) {
					counter++;
					iter.next();
				}
				sw.printElapsed("Finding " + counter + " (possible) solutions with reference database");
				query.setState(state);
				
				sw = new Stopwatch();
				counter = 0;
				iter = SHALLOW_DB.query(query);
				while (iter.hasNext()) {
					counter++;
					iter.next();
				}
				sw.printElapsed("Finding " + counter + " (possible) solutions with shallow trie database");
				query.setState(state);
				
				sw = new Stopwatch();
				counter = 0;
				iter = FULL_DB.query(query);
				while (iter.hasNext()) {
					counter++;
					iter.next();
				}
				sw.printElapsed("Finding " + counter + " solutions with full trie database");
				query.setState(state);
			}
		}
	}
}
