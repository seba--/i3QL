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
import saere.database.index.reference.ReferenceDatabase;
import saere.database.predicate.ClassFile10;
import saere.database.predicate.Instr3;
import saere.database.predicate.Method15;
import saere.database.profiling.Profiler;

public final class ProfilerBench {

	private static final int MAP_THRESHOLD = 120;
	private static final String PROFILES_PATH = "src/saere/database/profiling";
	private static final int TEST_RUNS = 2;
	//private static final String TEST_FILE = DatabaseTest.DATA_PATH + File.separator + "opal-0.5.0.jar";
	//private static final String TEST_FILE = "../test/classfiles/Tomcat-6.0.20.zip";
	private static final String TEST_FILE = DatabaseTest.GLOBAL_TEST_FILE;
	
	private static final Profiler PROFILER = Profiler.getInstance();
	private static final Factbase FACTS = Factbase.getInstance();
	private static final Database REFERENCE_DB = new ReferenceDatabase();
	private static final Database SHALLOW_DB = new TrieDatabase(new SimpleTrieBuilder(new ShallowFlattener(), MAP_THRESHOLD));
	private static final Database FULL_DB = new TrieDatabase(new SimpleTrieBuilder(new FullFlattener(), MAP_THRESHOLD));
	
	private static final Term[] INSTR3_QUERIES = BATTestQueries.ALL_INSTR3_QUERIES;
	private static final Term[] CLASSFILE10_QUERIES = BATTestQueries.ALL_CLASSFILE10_QUERIES;
	private static final Term[] METHOD15_QUERIES = BATTestQueries.ALL_METHOD15_QUERIES;
	
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
		Method15 method15Reference = new Method15(REFERENCE_DB);
		Method15 method15Shallow = new Method15(SHALLOW_DB);
		Method15 method15Full = new Method15(FULL_DB);
		for (int i = 0; i < TEST_RUNS; i++) {
			System.out.println("\nTest run " + (i + 1) + "...");
			
			// instr/3
			for (Term query : INSTR3_QUERIES) {
				State state = query.manifestState();
				
				System.out.print("\nReference: ");
				Utils.query(instr3Reference, query);
				query.setState(state);
				
				System.out.print("Shallow: ");
				Utils.query(instr3Shallow, query);
				query.setState(state);
				
				System.out.print("Full: ");
				Utils.query(instr3Full, query);
				query.setState(state);
			}
			
			/*
			// class_file/10
			for (Term query : CLASSFILE10_QUERIES) {
				State state = query.manifestState();
				
				System.out.print("\nReference: ");
				Utils.query(classFile10Reference, query);
				query.setState(state);
				
				System.out.print("Shallow: ");
				Utils.query(classFile10Shallow, query);
				query.setState(state);
				
				System.out.print("Full: ");
				Utils.query(classFile10Full, query);
				query.setState(state);
			}
			*/
			
			// method/15
			for (Term query : METHOD15_QUERIES) {
				State state = query.manifestState();
				
				System.out.print("\nReference: ");
				Utils.query(method15Reference, query);
				query.setState(state);
				
				System.out.print("Shallow: ");
				Utils.query(method15Shallow, query);
				query.setState(state);
				
				System.out.print("Full: ");
				Utils.query(method15Full, query);
				query.setState(state);
			}
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
		Method15 method15Reference = new Method15(REFERENCE_DB);
		Method15 method15Shallow = new Method15(SHALLOW_DB);
		Method15 method15Full = new Method15(FULL_DB);
		for (int i = 0; i < TEST_RUNS; i++) {
			System.out.println("\nTest run " + (i + 1) + "...");
			
			// instr/3
			for (Term query : BATTestQueries.ALL_INSTR3_FREQ_QUERIES) {
				State state = query.manifestState();
				
				Utils.queryNoPrint(instr3Reference, query);
				query.setState(state);
				
				Utils.queryNoPrint(instr3Shallow, query);
				query.setState(state);
				
				Utils.queryNoPrint(instr3Full, query);
				query.setState(state);
			}
			
			/*
			// class_file/10
			for (Term query : BATTestQueries.ALL_CLASSFILE10_FREQ_QUERIES) {
				State state = query.manifestState();
				
				Utils.queryNoPrint(classFile10Reference, query);
				query.setState(state);
				
				Utils.queryNoPrint(classFile10Shallow, query);
				query.setState(state);
				
				Utils.queryNoPrint(classFile10Full, query);
				query.setState(state);
			}
			*/
			
			for (Term query : BATTestQueries.ALL_METHOD15_FREQ_QUERIES) {
				State state = query.manifestState();
				
				Utils.queryNoPrint(method15Reference, query);
				query.setState(state);
				
				Utils.queryNoPrint(method15Shallow, query);
				query.setState(state);
				
				Utils.queryNoPrint(method15Full, query);
				query.setState(state);
			}
		}
		
		PROFILER.saveProfiles(PROFILES_PATH + File.separator + profileName);
		PROFILER.loadProfiles(PROFILES_PATH + File.separator + profileName);
		PROFILER.setMode(Profiler.Mode.USE);
		Profiler p = Profiler.getInstance();
		System.out.println("!");
	}
	
	@Test
	public void useProfiles() {
		
		Instr3 instr3Reference = new Instr3(REFERENCE_DB);
		Instr3 instr3Shallow = new Instr3(SHALLOW_DB);
		Instr3 instr3Full = new Instr3(FULL_DB);
		ClassFile10 classFile10Reference = new ClassFile10(REFERENCE_DB);
		ClassFile10 classFile10Shallow = new ClassFile10(SHALLOW_DB);
		ClassFile10 classFile10Full = new ClassFile10(FULL_DB);
		Method15 method15Reference = new Method15(REFERENCE_DB);
		Method15 method15Shallow = new Method15(SHALLOW_DB);
		Method15 method15Full = new Method15(FULL_DB);
		for (int i = 0; i < TEST_RUNS; i++) {
			System.out.println("\nTest run " + (i + 1) + "...");
			
			// instr/3
			for (Term query : INSTR3_QUERIES) {
				State state = query.manifestState();
				
				System.out.print("\nReference: ");
				Utils.query(instr3Reference, query);
				query.setState(state);
				
				System.out.print("Shallow: ");
				Utils.query(instr3Shallow, query);
				query.setState(state);
				
				System.out.print("Full: ");
				Utils.query(instr3Full, query);
				query.setState(state);
			}
			
			/*
			// class_file/10
			for (Term query : CLASSFILE10_QUERIES) {
				State state = query.manifestState();
				
				System.out.print("\nReference: ");
				Utils.query(classFile10Reference, query);
				query.setState(state);
				
				System.out.print("Shallow: ");
				Utils.query(classFile10Shallow, query);
				query.setState(state);
				
				System.out.print("Full: ");
				Utils.query(classFile10Full, query);
				query.setState(state);
			}
			*/
			
			// method/15
			for (Term query : METHOD15_QUERIES) {
				State state = query.manifestState();
				
				System.out.print("\nReference: ");
				Utils.query(method15Reference, query);
				query.setState(state);
				
				System.out.print("Shallow: ");
				Utils.query(method15Shallow, query);
				query.setState(state);
				
				System.out.print("Full: ");
				Utils.query(method15Full, query);
				query.setState(state);
			}
		}
	}
	
	/*
	@Test
	public void testRatings() {
		for (Term query : BATInstr3Queries.QUERIES) {
			System.out.println("Query " + query + " has rating " + Profiler.getInstance().rate(query));
		}
	}
	*/
}
