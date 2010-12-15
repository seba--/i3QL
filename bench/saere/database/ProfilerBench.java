package saere.database;

import java.io.File;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import saere.Term;
import saere.database.index.DefaultTrieBuilder;
import saere.database.index.FullFlattener;
import saere.database.index.ShallowFlattener;
import saere.database.predicate.ClassFile10;
import saere.database.predicate.Instr3;
import saere.database.profiling.Profiler;
import saere.database.util.Stopwatch;

/**
 * This class measures the impact of profiling. A serialization of orders is 
 * created as side-effect.
 * 
 * @author David Sullivan
 * @version 0.1, 12/6/2010
 */
public final class ProfilerBench {

	private static final int MAP_THRESHOLD = 100;
	private static final String PROFILES_PATH = System.getProperty("user.dir");
	private static final int TEST_RUNS = 2;
	private static final String TEST_FILE = DatabaseTest.GLOBAL_TEST_FILE;
	
	private static final Profiler PROFILER = Profiler.getInstance();
	private static final Factbase FACTS = Factbase.getInstance();
	private static final Database REFERENCE_DB = new ReferenceDatabase();
	private static final Database SHALLOW_DB = new TrieDatabase(new DefaultTrieBuilder(new ShallowFlattener(), MAP_THRESHOLD));
	private static final Database FULL_DB = new TrieDatabase(new DefaultTrieBuilder(new FullFlattener(), MAP_THRESHOLD));
	
	private static final Term[] INSTR3_QUERIES = BATTestQueries.ALL_INSTR3_QUERIES;
	private static final Term[] CLASSFILE10_QUERIES = BATTestQueries.ALL_CLASSFILE10_QUERIES;
	//private static final Term[] METHOD15_QUERIES = BATTestQueries.ALL_METHOD15_QUERIES;
	
	private static String profileName;
	
	@BeforeClass
	public static void initialize() {	
		Stopwatch sw = new Stopwatch();
		FACTS.read(TEST_FILE);
		sw.printElapsedAndReset("Filling the factbase with " + FACTS.size() + " terms");
		
		//FactsPrinter.print("c:/users/leaf/desktop/facts.txt");
		
		profileName = System.currentTimeMillis() + ".ser";
	}

	@AfterClass
	public static void finish() {
		FACTS.drop();
		// XXX aaaand remove profiles
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
	public void noProfiles() {
		
		Instr3 instr3Reference = new Instr3(REFERENCE_DB);
		Instr3 instr3Shallow = new Instr3(SHALLOW_DB);
		Instr3 instr3Full = new Instr3(FULL_DB);
		ClassFile10 classFile10Reference = new ClassFile10(REFERENCE_DB);
		ClassFile10 classFile10Shallow = new ClassFile10(SHALLOW_DB);
		ClassFile10 classFile10Full = new ClassFile10(FULL_DB);
		//Method15 method15Reference = new Method15(REFERENCE_DB);
		//Method15 method15Shallow = new Method15(SHALLOW_DB);
		//Method15 method15Full = new Method15(FULL_DB);
		for (int i = 0; i < TEST_RUNS; i++) {
			System.out.println("\nTest run " + (i + 1) + "...");
			
			// instr/3
			for (Term query : INSTR3_QUERIES) {
				System.out.print("\nReference: ");
				Utils.query(instr3Reference, query);
				System.out.print("Shallow: ");
				Utils.query(instr3Shallow, query);
				System.out.print("Full: ");
				Utils.query(instr3Full, query);
			}
			
			// class_file/10
			for (Term query : CLASSFILE10_QUERIES) {
				System.out.print("\nReference: ");
				Utils.query(classFile10Reference, query);
				System.out.print("Shallow: ");
				Utils.query(classFile10Shallow, query);
				System.out.print("Full: ");
				Utils.query(classFile10Full, query);
			}
			
			/*
			// method/15
			for (Term query : METHOD15_QUERIES) {
				System.out.print("\nReference: ");
				Utils.query(method15Reference, query);
				System.out.print("Shallow: ");
				Utils.query(method15Shallow, query);
				System.out.print("Full: ");
				Utils.query(method15Full, query);
			}
			*/
		}
		
	}
	
	@Test
	public void createProfiles() {
		PROFILER.setMode(Profiler.Mode.PROFILE);
		
		Instr3 instr3Reference = new Instr3(REFERENCE_DB);
		Instr3 instr3Shallow = new Instr3(SHALLOW_DB);
		Instr3 instr3Full = new Instr3(FULL_DB);
		ClassFile10 classFile10Reference = new ClassFile10(REFERENCE_DB);
		ClassFile10 classFile10Shallow = new ClassFile10(SHALLOW_DB);
		ClassFile10 classFile10Full = new ClassFile10(FULL_DB);
		//Method15 method15Reference = new Method15(REFERENCE_DB);
		//Method15 method15Shallow = new Method15(SHALLOW_DB);
		//Method15 method15Full = new Method15(FULL_DB);
		for (int i = 0; i < TEST_RUNS; i++) {
			System.out.println("\nTest run " + (i + 1) + "...");
			
			// instr/3
			for (Term query : BATTestQueries.ALL_INSTR3_FREQ_QUERIES) {
				Utils.queryNoPrint(instr3Reference, query);
				Utils.queryNoPrint(instr3Shallow, query);
				Utils.queryNoPrint(instr3Full, query);
			}
			
			// class_file/10
			for (Term query : BATTestQueries.ALL_CLASSFILE10_FREQ_QUERIES) {
				Utils.queryNoPrint(classFile10Reference, query);
				Utils.queryNoPrint(classFile10Shallow, query);
				Utils.queryNoPrint(classFile10Full, query);
			}
			
			/*
			// method/15
			for (Term query : BATTestQueries.ALL_METHOD15_FREQ_QUERIES) {
				Utils.queryNoPrint(method15Reference, query);
				Utils.queryNoPrint(method15Shallow, query);
				Utils.queryNoPrint(method15Full, query);
			}
			*/
		}
		
		PROFILER.saveProfiles(PROFILES_PATH + File.separator + profileName);
		PROFILER.loadProfiles(PROFILES_PATH + File.separator + profileName);
		System.out.println("Profiles:\n" + PROFILER.toString());
		PROFILER.setMode(Profiler.Mode.USE);
	}
	
	@Test
	public void useProfiles() {
		
		Instr3 instr3Reference = new Instr3(REFERENCE_DB);
		Instr3 instr3Shallow = new Instr3(SHALLOW_DB);
		Instr3 instr3Full = new Instr3(FULL_DB);
		ClassFile10 classFile10Reference = new ClassFile10(REFERENCE_DB);
		ClassFile10 classFile10Shallow = new ClassFile10(SHALLOW_DB);
		ClassFile10 classFile10Full = new ClassFile10(FULL_DB);
		//Method15 method15Reference = new Method15(REFERENCE_DB);
		//Method15 method15Shallow = new Method15(SHALLOW_DB);
		//Method15 method15Full = new Method15(FULL_DB);
		for (int i = 0; i < TEST_RUNS; i++) {
			System.out.println("\nTest run " + (i + 1) + "...");
			
			// instr/3
			for (Term query : INSTR3_QUERIES) {
				System.out.print("\nReference: ");
				Utils.query(instr3Reference, query);
				System.out.print("Shallow: ");
				Utils.query(instr3Shallow, query);
				System.out.print("Full: ");
				Utils.query(instr3Full, query);
			}
			
			// class_file/10
			for (Term query : CLASSFILE10_QUERIES) {
				System.out.print("\nReference: ");
				Utils.query(classFile10Reference, query);
				System.out.print("Shallow: ");
				Utils.query(classFile10Shallow, query);
				System.out.print("Full: ");
				Utils.query(classFile10Full, query);
			}
			
			/*
			// method/15
			for (Term query : METHOD15_QUERIES) {
				System.out.print("\nReference: ");
				Utils.query(method15Reference, query);
				System.out.print("Shallow: ");
				Utils.query(method15Shallow, query);
				System.out.print("Full: ");
				Utils.query(method15Full, query);
			}
			*/
		}
	}
	
	@Test
	public void testRatings() {
		for (Term query : BATTestQueries.ALL_QUERIES) {
			System.out.println("Query " + query + " has rating " + PROFILER.rate(query));
		}
	}
}
