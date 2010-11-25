package saere.database;

import java.io.File;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import saere.State;
import saere.Term;
import saere.database.index.FullFlattener;
import saere.database.index.ShallowFlattener;
import saere.database.index.SimpleTrieBuilder;
import saere.database.index.Trie;
import saere.database.index.TrieBuilder;
import saere.database.index.reference.ReferenceDatabase;
import saere.database.predicate.DatabasePredicate;
import saere.database.predicate.Instr3;
import saere.database.profiling.QueryProfiler;
import saere.database.profiling.TriePrinter;
import saere.database.profiling.TriePrinter.Mode;

public final class ProfilerBench {

	private static final int MAP_THRESHOLD = 120;
	private static final String PROFILES_PATH = "src/saere/database/profiling";
	private static final int TEST_RUNS = 3;
	private static final String TEST_FILE = DatabaseTest.DATA_PATH + File.separator + "opal-0.5.0.jar";
	
	private static final Factbase FACTS = Factbase.getInstance();
	private static final Database REFERENCE_DB = new ReferenceDatabase();
	private static final Database SHALLOW_DB = new TrieDatabase(new SimpleTrieBuilder(new ShallowFlattener(), MAP_THRESHOLD));
	private static final Database FULL_DB = new TrieDatabase(new SimpleTrieBuilder(new FullFlattener(), MAP_THRESHOLD));
	
	private static String profileName;
	
	@BeforeClass
	public static void initialize() {	
		Stopwatch sw = new Stopwatch();
		FACTS.read(TEST_FILE);
		sw.printElapsedAndReset("Reading the factbase");
		
		//FactsPrinter.print("c:/users/leaf/desktop/facts.txt");
		
		profileName = System.currentTimeMillis() + ".ser";
	}

	@AfterClass
	public static void finish() {
		FACTS.drop();
	}
	
	@Before
	public void before() {
		Stopwatch sw = new Stopwatch();
		((ReferenceDatabase) REFERENCE_DB).allowDuplicates(true);
		REFERENCE_DB.fill();
		sw.printElapsedAndReset("Filling the reference database");
		SHALLOW_DB.fill();
		sw.printElapsedAndReset("Filling the (shallow) trie database");
		FULL_DB.fill();
		sw.printElapsed("Filling the (full) trie database");
	}
	
	@After
	public void after() {
		REFERENCE_DB.drop();
		SHALLOW_DB.drop();
		FULL_DB.drop();
	}
	
	@Test
	public void createProfiles() {
		DatabasePredicate.enableProfiling(true);
		
		// 'Normal' predicates which get sets of candidates (because of collisions)
		Instr3 instr3Reference = new Instr3(REFERENCE_DB);
		
		// Predicates with shallow trie database
		Instr3 instr3Shallow = new Instr3(SHALLOW_DB);
		
		// Predicates that rely on exact result sets (no collision)
		Instr3 instr3Full = new Instr3(FULL_DB);
		
		for (int i = 0; i < TEST_RUNS; i++) {
			System.out.println("\nTest run " + (i + 1) + "...");
			for (Term query : BATInstr3Queries.QUERIES) {
				State state = query.manifestState();
				
				System.out.print("\nList: ");
				Utils.query(instr3Reference, query);
				query.setState(state);
				
				System.out.print("Shallow: ");
				Utils.query(instr3Shallow, query);
				query.setState(state);
				
				System.out.print("Full: ");
				Utils.query(instr3Full, query);
				query.setState(state);
			}
		}
		
		QueryProfiler.getInstance().saveOrders(PROFILES_PATH + File.separator + profileName);
		QueryProfiler.getInstance().loadOrders(PROFILES_PATH + File.separator + profileName);
		DatabasePredicate.enableProfiling(false);
	}
	
	@Test
	public void useProfiles() {
		QueryProfiler profiler = QueryProfiler.getInstance(); // XXX To inspect in debugger
		Trie root = ((TrieDatabase) FULL_DB).root();
		TrieBuilder builder = ((TrieDatabase) FULL_DB).trieBuilder();
		
		TriePrinter.print(root, builder, "c:/users/leaf/desktop/profiled-trie.gv", Mode.BOX);
		
		// 'Normal' predicates which get sets of candidates (because of collisions)
		Instr3 instr3Reference = new Instr3(REFERENCE_DB);
		
		// Predicates with shallow trie database
		Instr3 instr3Shallow = new Instr3(SHALLOW_DB);
		
		// Predicates that rely on exact result sets (no collision)
		Instr3 instr3Full = new Instr3(FULL_DB);
		
		for (int i = 0; i < TEST_RUNS; i++) {
			System.out.println("\nTest run " + (i + 1) + "...");
			for (Term query : BATInstr3Queries.QUERIES) {
				State state = query.manifestState();
				
				System.out.print("\nList: ");
				Utils.query(instr3Reference, query);
				query.setState(state);
				
				System.out.print("Shallow: ");
				Utils.query(instr3Shallow, query);
				query.setState(state);
				
				System.out.print("Full: ");
				Utils.query(instr3Full, query);
				query.setState(state);
			}
		}
	}
}
