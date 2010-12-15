package saere.database;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import saere.State;
import saere.Term;
import saere.database.BATTestQueries;
import saere.database.Database;
import saere.database.DatabaseTest;
import saere.database.Factbase;
import saere.database.TrieDatabase;
import saere.database.Utils;
import saere.database.index.FullFlattener;
import saere.database.index.ShallowFlattener;
import saere.database.index.DefaultTrieBuilder;
import saere.database.predicate.ClassFile10;
import saere.database.predicate.Instr3;
//import saere.database.predicate.Method15;
import saere.database.util.Stopwatch;

/**
 * Measures query times (with actual unification) for the <tt>instr/3</tt> and 
 * <tt>class_file/10</tt>.
 * 
 * @author David Sullivan
 * @version 0.1, 12/6/2010
 */
public class PredicateBench {
	
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

	@AfterClass
	public static void finish() {
		FACTS.drop();
		REFERENCE_DB.drop();
		SHALLOW_DB.drop();
		FULL_DB.drop();
	}
	
	@Test
	public void go() {
		
		// 'Normal' predicates which get sets of candidates (because of collisions)
		Instr3 instr3Reference = new Instr3(REFERENCE_DB);
		ClassFile10 classFile10Reference = new ClassFile10(REFERENCE_DB);
		//Method15 method15Reference = new Method15(REFERENCE_DB);
		
		// Predicates with shallow trie database
		Instr3 instr3Shallow = new Instr3(SHALLOW_DB);
		ClassFile10 classFile10Shallow = new ClassFile10(SHALLOW_DB);
		//Method15 method15Shallow = new Method15(SHALLOW_DB);
		
		// Predicates that rely on exact result sets (no collision)
		Instr3 instr3Full = new Instr3(FULL_DB);
		ClassFile10 classFile10Full = new ClassFile10(FULL_DB);
		//Method15 method15Full = new Method15(FULL_DB);
		
		for (int i = 0; i < TEST_RUNS; i++) {
			System.out.println("\nTest run " + (i + 1) + "...");
			for (int j = 0; j < BATTestQueries.ALL_INSTR3_QUERIES.length; j++) {
				Term query = BATTestQueries.ALL_INSTR3_QUERIES[j];
				State state = query.manifestState();
				
				System.out.print("\nList: ");
				Utils.query(instr3Reference, BATTestQueries.ALL_INSTR3_QUERIES[j]);
				query.setState(state);
				
				System.out.print("Shallow: ");
				Utils.query(instr3Shallow, BATTestQueries.ALL_INSTR3_QUERIES[j]);
				query.setState(state);
				
				System.out.print("Full: ");
				Utils.query(instr3Full, BATTestQueries.ALL_INSTR3_QUERIES[j]);
				query.setState(state);
			}
			
			for (int j = 0; j < BATTestQueries.ALL_CLASSFILE10_QUERIES.length; j++) {
				Term query = BATTestQueries.ALL_CLASSFILE10_QUERIES[j];
				State state = query.manifestState();
				
				System.out.print("\nList: ");
				Utils.query(classFile10Reference, BATTestQueries.ALL_CLASSFILE10_QUERIES[j]);
				query.setState(state);
				
				System.out.print("Shallow: ");
				Utils.query(classFile10Shallow, BATTestQueries.ALL_CLASSFILE10_QUERIES[j]);
				query.setState(state);
				
				System.out.print("Full: ");
				Utils.query(classFile10Full, BATTestQueries.ALL_CLASSFILE10_QUERIES[j]);
				query.setState(state);
			}
			
			/*
			for (int j = 0; j < BATTestQueries.ALL_METHOD15_QUERIES.length; j++) {
				Term query = BATTestQueries.ALL_METHOD15_QUERIES[j];
				State state = query.manifestState();
				
				System.out.print("\nList: ");
				Utils.query(method15Reference, BATTestQueries.ALL_METHOD15_QUERIES[j]);
				query.setState(state);
				
				System.out.print("Shallow: ");
				Utils.query(method15Shallow, BATTestQueries.ALL_METHOD15_QUERIES[j]);
				query.setState(state);
				
				System.out.print("Full: ");
				Utils.query(method15Full, BATTestQueries.ALL_METHOD15_QUERIES[j]);
				query.setState(state);
			}
			*/
		}
	}
}
