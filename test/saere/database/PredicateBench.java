package saere.database;

import java.util.Arrays;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import saere.IntegerAtom;
import saere.Solutions;
import saere.StringAtom;
import saere.Term;
import saere.Variable;
import saere.database.index.map.MapDatabase;
import saere.database.index.unique.UniqueDatabase;
import saere.database.predicate.ClassFile10;
import saere.database.predicate.DatabasePredicate;
import saere.database.predicate.Instr3;
import saere.database.predicate.Method15;
import saere.database.profiling.Keys;

public class PredicateBench {
	
	private static final Factbase FACTS = Factbase.getInstance();
	private static final Database LIST_DB = ListDatabase.getInstance();
	private static final Database MAP_DB = MapDatabase.getInstance();
	private static final Database UNIQUE_DB = UniqueDatabase.getInstance();
	
	private static final Keys KEYS = Keys.getInstance();
	
	// 'Normal' predicates which get sets of candidates (because of collisions)
	private static final Instr3 INSTR3_NORMAL = new Instr3();
	private static final ClassFile10 CLASSFILE10_NORMAL = new ClassFile10();
	private static final Method15 METHOD15_NORMAL = new Method15();
	
	// Predicates that rely on exact result sets (no collision)
	private static final saere.database.index.unique.Instr3 INSTR3_UNIQUE = new saere.database.index.unique.Instr3();
	private static final saere.database.index.unique.ClassFile10 CLASSFILE10_UNIQUE = new saere.database.index.unique.ClassFile10();
	private static final saere.database.index.unique.Method15 METHOD15_UNIQUE = new saere.database.index.unique.Method15();
	
	private static final int TEST_RUNS = 3;
	
	private static Term[][] instr3Queries;
	private static Term[][] classfile10Queries;
	private static Term[][] method15Queries;
	
	@BeforeClass
	public static void initialize() {
		FACTS.read(DatabaseTest.GLOBAL_TEST_FILE);
		LIST_DB.fill();
		MAP_DB.fill();
		UNIQUE_DB.fill();
		
		// Actually a horrible way to build queries...
		instr3Queries = new Term[][] {
			new Term[] { v(), v(), v() },
			new Term[] { sa("m_" + KEYS.getLowestKey("m")), v(), v() },
			new Term[] { sa("m_" + KEYS.getHighestKey("m")), v(), v() },
			new Term[] { v(), ia(0), v() }
		};
	}

	@AfterClass
	public static void finish() {
		FACTS.drop();
		LIST_DB.drop();
		MAP_DB.drop();
		UNIQUE_DB.drop();
		
		// XXX FIXME flattenForQuery seems buggy
	}
	
	@Test
	public void test1() {
		for (int i = 0; i < TEST_RUNS; i++) {
			System.out.println("Test run " + (i + 1) + "...");
			DatabasePredicate.useDatabase(MAP_DB);
			for (int j = 0; j < instr3Queries.length; j++) {
				System.out.print("Normal: ");
				queryAndPrintResult(INSTR3_NORMAL, instr3Queries[j]);
			}
			System.out.println();
			DatabasePredicate.useDatabase(UNIQUE_DB);
			for (int j = 0; j < instr3Queries.length; j++) {
				System.out.print("Unique: ");
				queryAndPrintResult(INSTR3_UNIQUE, instr3Queries[j]);
			}
		}
	}
	
	private static void queryAndPrintResult(DatabasePredicate predicate, Term[] query) {
		Stopwatch sw = new Stopwatch();
		int c = 0;
		Solutions s = predicate.unify(query);
		while (s.next()) {
			c++;
		}
		sw.printElapsed("Finding " + c + " solutions for query " + Arrays.toString(query) + " for predicate " + predicate.toString());
	}
	
	private static StringAtom sa(String s) {
		return StringAtom.StringAtom(s);
	}
	
	private static IntegerAtom ia(int i) {
		return IntegerAtom.IntegerAtom(i);
	}
	
	private static Variable v() {
		return new Variable();
	}
}
