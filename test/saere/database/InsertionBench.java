package saere.database;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import saere.database.index.FullFlattener;
import saere.database.index.ShallowFlattener;
import saere.database.index.SimpleTrieBuilder;
import saere.database.index.reference.ReferenceDatabase;

public class InsertionBench {
	
	private static final Factbase FACTS = Factbase.getInstance();
	
	private static Database referenceDB;
	private static Database shallowDB;
	private static Database fullDB;
	
	@BeforeClass
	public static void initialize() {
		Stopwatch sw = new Stopwatch();
		FACTS.read(DatabaseTest.GLOBAL_TEST_FILE);
		sw.printElapsed("Filling the factbase with " + DatabaseTest.GLOBAL_TEST_FILE);
	}
	
	@Test
	public void go() {
		int deactivated = 999999;
		int[] thresholds = { deactivated, 20, 40, 60, 80, 100, 120, 140, 160, 180, 200, 220, 240, 260, 250, 260, 280, 300 };
		
		for (int i = 0; i < 3; i++) {
			System.out.println("\nRun " + (i + 1));
			
			for (int j = 0; j < thresholds.length; j++) {
				System.out.println("\nInserting for threshold " + thresholds[j]);
				
				referenceDB = new ReferenceDatabase();
				((ReferenceDatabase) referenceDB).allowDuplicates(true);
				shallowDB = new TrieDatabase(new SimpleTrieBuilder(new ShallowFlattener(), thresholds[j]));
				fullDB = new TrieDatabase(new SimpleTrieBuilder(new FullFlattener(), thresholds[j]));
				
				Stopwatch sw = new Stopwatch();
				referenceDB.fill();
				sw.printElapsedAndReset("Filling the reference database");
				shallowDB.fill();
				sw.printElapsedAndReset("Filling the (shallow) trie database");
				fullDB.fill();
				sw.printElapsed("Filling the (full) trie database");
				
				shallowDB.drop();
				fullDB.drop();
			}
		}
		
	}

	@AfterClass
	public static void finish() {
		FACTS.drop();
	}
}
