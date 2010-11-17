package saere.database;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import saere.State;
import saere.Term;
import saere.database.index.FullFlattener;
import saere.database.index.ShallowFlattener;
import saere.database.index.SimpleTrieBuilder;
import saere.database.index.reference.ReferenceDatabase;
import saere.database.predicate.ClassFile10;
import saere.database.predicate.DatabasePredicate;
import saere.database.predicate.Instr3;
import saere.database.predicate.Method15;

public class PredicateBench {
	
	private static final int MAP_THRESHOLD = 50;
	
	private static final Factbase FACTS = Factbase.getInstance();
	private static final Database REFERENCE_DB = new ReferenceDatabase();
	private static final Database SHALLOW_DB = new TrieDatabase(new SimpleTrieBuilder(new ShallowFlattener(), MAP_THRESHOLD));
	private static final Database FULL_DB = new TrieDatabase(new SimpleTrieBuilder(new FullFlattener(), MAP_THRESHOLD));
	
	// 'Normal' predicates which get sets of candidates (because of collisions)
	private static final Instr3 INSTR3_NORMAL = new Instr3();
	private static final ClassFile10 CLASSFILE10_NORMAL = new ClassFile10();
	private static final Method15 METHOD15_NORMAL = new Method15();
	
	// Predicates that rely on exact result sets (no collision)
	private static final saere.database.predicate.full.Instr3 INSTR3_NOCOLL = new saere.database.predicate.full.Instr3();
	private static final saere.database.predicate.full.ClassFile10 CLASSFILE10_NOCOLL = new saere.database.predicate.full.ClassFile10();
	private static final saere.database.predicate.full.Method15 METHOD15_NOCOLL = new saere.database.predicate.full.Method15();
	
	private static final int TEST_RUNS = 3;
	
	@BeforeClass
	public static void initialize() {
		FACTS.read(DatabaseTest.GLOBAL_TEST_FILE);
		Stopwatch sw = new Stopwatch();
		REFERENCE_DB.fill();
		sw.printElapsedAndReset("Filling the reference database");
		SHALLOW_DB.fill();
		sw.printElapsedAndReset("Filling the (shallow) trie database");
		FULL_DB.fill();
		sw.printElapsedAndReset("Filling the (full) trie database");
		//FactsPrinter.print("c:/users/leaf/desktop/facts.txt");
	}

	@AfterClass
	public static void finish() {
		FACTS.drop();
		REFERENCE_DB.drop();
		SHALLOW_DB.drop();
		FULL_DB.drop();
	}
	
	@Test
	public void go() {
		for (int i = 0; i < TEST_RUNS; i++) {
			System.out.println("\nTest run " + (i + 1) + "...");
			for (int j = 0; j < BATTestQueries.ALL_INSTR_QUERIES.length; j++) {
				Term query = BATTestQueries.ALL_INSTR_QUERIES[j];
				State state = query.manifestState();
				
				System.out.print("\nList: ");
				DatabasePredicate.useDatabase(REFERENCE_DB);
				Utils.query(INSTR3_NORMAL, BATTestQueries.ALL_INSTR_QUERIES[j]);
				query.setState(state);
				
				System.out.print("Shallow: ");
				DatabasePredicate.useDatabase(SHALLOW_DB);
				Utils.query(INSTR3_NORMAL, BATTestQueries.ALL_INSTR_QUERIES[j]);
				query.setState(state);
				
				System.out.print("Full: ");
				DatabasePredicate.useDatabase(FULL_DB);
				Utils.query(INSTR3_NOCOLL, BATTestQueries.ALL_INSTR_QUERIES[j]);
				query.setState(state);
			}
			
			for (int j = 0; j < BATTestQueries.ALL_CLASSFILE_QUERIES.length; j++) {
				Term query = BATTestQueries.ALL_CLASSFILE_QUERIES[j];
				State state = query.manifestState();
				
				System.out.print("\nList: ");
				DatabasePredicate.useDatabase(REFERENCE_DB);
				Utils.query(CLASSFILE10_NORMAL, BATTestQueries.ALL_CLASSFILE_QUERIES[j]);
				query.setState(state);
				
				System.out.print("Shallow: ");
				DatabasePredicate.useDatabase(SHALLOW_DB);
				Utils.query(CLASSFILE10_NORMAL, BATTestQueries.ALL_CLASSFILE_QUERIES[j]);
				query.setState(state);
				
				System.out.print("Full: ");
				DatabasePredicate.useDatabase(FULL_DB);
				Utils.query(CLASSFILE10_NOCOLL, BATTestQueries.ALL_CLASSFILE_QUERIES[j]);
				query.setState(state);
			}
			
			for (int j = 0; j < BATTestQueries.ALL_METHOD_QUERIES.length; j++) {
				Term query = BATTestQueries.ALL_METHOD_QUERIES[j];
				State state = query.manifestState();
				
				System.out.print("\nList: ");
				DatabasePredicate.useDatabase(REFERENCE_DB);
				Utils.query(METHOD15_NORMAL, BATTestQueries.ALL_METHOD_QUERIES[j]);
				query.setState(state);
				
				System.out.print("Shallow: ");
				DatabasePredicate.useDatabase(SHALLOW_DB);
				Utils.query(METHOD15_NORMAL, BATTestQueries.ALL_METHOD_QUERIES[j]);
				query.setState(state);
				
				System.out.print("Full: ");
				DatabasePredicate.useDatabase(FULL_DB);
				Utils.query(METHOD15_NOCOLL, BATTestQueries.ALL_METHOD_QUERIES[j]);
				query.setState(state);
			}
		}
	}
}
