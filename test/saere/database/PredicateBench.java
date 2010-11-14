package saere.database;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import saere.Term;
import saere.State;
import saere.database.index.FullFlattener;
import saere.database.index.ShallowFlattener;
import saere.database.index.SimpleTrieBuilder;
import saere.database.predicate.ClassFile10;
import saere.database.predicate.DatabasePredicate;
import saere.database.predicate.Instr3;
import saere.database.predicate.Method15;

public class PredicateBench {
	
	private static final int MAP_THRESHOLD = 50;
	
	private static final Factbase FACTS = Factbase.getInstance();
	private static final Database LIST_DB = ListDatabase.getInstance();
	private static final Database SHALLOW_DB = new TrieDatabase(new SimpleTrieBuilder(new ShallowFlattener(), 50));
	private static final Database FULL_DB = new TrieDatabase(new SimpleTrieBuilder(new FullFlattener(), 50));
	
	// 'Normal' predicates which get sets of candidates (because of collisions)
	private static final Instr3 INSTR3_NORMAL = new Instr3();
	private static final ClassFile10 CLASSFILE10_NORMAL = new ClassFile10();
	private static final Method15 METHOD15_NORMAL = new Method15();
	
	// Predicates that rely on exact result sets (no collision)
	private static final saere.database.predicate.full.Instr3 INSTR3_NOCOL = new saere.database.predicate.full.Instr3();
	private static final saere.database.predicate.full.ClassFile10 CLASSFILE10_NOCOL = new saere.database.predicate.full.ClassFile10();
	private static final saere.database.predicate.full.Method15 METHOD15_NOCOL = new saere.database.predicate.full.Method15();
	
	private static final int TEST_RUNS = 3;
	
	@BeforeClass
	public static void initialize() {
		FACTS.read(DatabaseTest.GLOBAL_TEST_FILE);
		LIST_DB.fill();
		SHALLOW_DB.fill();
		FULL_DB.fill();
	}

	@AfterClass
	public static void finish() {
		FACTS.drop();
		LIST_DB.drop();
		SHALLOW_DB.drop();
		FULL_DB.drop();
	}
	
	@Test
	public void test1() {
		for (int i = 0; i < TEST_RUNS; i++) {
			System.out.println("Test run " + (i + 1) + "...");
			for (int j = 0; j < BATTestQueries.ALL_QUERIES.length; j++) {
				Term query = BATTestQueries.ALL_QUERIES[j];
				State state = query.manifestState();
				
				System.out.print("\nList: ");
				DatabasePredicate.useDatabase(LIST_DB);
				Utils.query(INSTR3_NORMAL, BATTestQueries.ALL_QUERIES[j]);
				query.setState(state);
				
				System.out.print("Shallow: ");
				DatabasePredicate.useDatabase(SHALLOW_DB);
				Utils.query(INSTR3_NORMAL, BATTestQueries.ALL_QUERIES[j]);
				query.setState(state);
				
				System.out.print("Full: ");
				DatabasePredicate.useDatabase(FULL_DB);
				Utils.query(INSTR3_NOCOL, BATTestQueries.ALL_QUERIES[j]);
				query.setState(state);
			}
		}
	}
}
