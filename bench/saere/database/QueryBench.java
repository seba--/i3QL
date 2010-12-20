package saere.database;

import java.io.File;

import org.junit.Test;

import saere.Solutions;
import saere.Term;
import saere.database.index.DefaultTrieBuilder;
import saere.database.index.FullFlattener;
import saere.database.index.ShallowFlattener;
import saere.database.predicate.DatabasePredicate;
import saere.database.profiling.Profiler;
import saere.database.profiling.Profiler.Mode;
import saere.database.util.Stopwatch;
import saere.database.util.Stopwatch.Unit;

public final class QueryBench {

	private static final String TBL_SEP = "\t";
	private static final int RUNS = 10; // 10
	private static final boolean USE_PROFILES = true;
	private static final Factbase FACTS = Factbase.getInstance();
	private static final Term[] INSTR3_QUERIES = BATTestQueries.ALL_INSTR3_QUERIES;
	private static final Term[] CLASS_FILE10_QUERIES = BATTestQueries.ALL_CLASSFILE10_QUERIES;
	private static final Stopwatch SW = new Stopwatch(Unit.MICROSECONDS);
	
	private static Database referenceDB;
	private static Database shallowDB;
	private static Database fullDB;
	
	public static void main(String[] args) {
		QueryBench bench = new QueryBench();
		bench.benchmarkQueries();
	}
	
	@Test
	public void benchmarkQueries() {
		// Stabilize JVM with MMC.jar
		String[] testFiles = { DatabaseTest.TEST_FILES[1], DatabaseTest.TEST_FILES[1], DatabaseTest.TEST_FILES[2], DatabaseTest.TEST_FILES[3] };
		
		if (USE_PROFILES) {
			Profiler.getInstance().loadProfiles(DatabaseTest.DATA_PATH + File.separator + "profiles.ser");
			Profiler.getInstance().setMode(Mode.USE);
			System.out.println("Using profiles");
		} else {
			System.out.println("Not using profiles");
		}
		
		for (String testFile : testFiles) {
			FACTS.drop();
			FACTS.read(testFile);
			
			referenceDB = new ReferenceDatabase();
			((ReferenceDatabase) referenceDB).allowDuplicates(true);
			referenceDB.fill();
			
			shallowDB = new TrieDatabase(new DefaultTrieBuilder(new ShallowFlattener(), DatabaseTest.GLOBAL_MAP_THRESHOLD));
			shallowDB.fill();
			
			fullDB = new TrieDatabase(new DefaultTrieBuilder(new FullFlattener(), DatabaseTest.GLOBAL_MAP_THRESHOLD));
			fullDB.fill();
			
			DatabasePredicate instr3Ref = new DatabasePredicate("instr", 3, referenceDB);
			DatabasePredicate instr3STF = new DatabasePredicate("instr", 3, shallowDB);
			DatabasePredicate instr3FTF = new DatabasePredicate("instr", 3, fullDB);
			
			DatabasePredicate classfile10Ref = new DatabasePredicate("class_file", 10, referenceDB);
			DatabasePredicate classfile10STF = new DatabasePredicate("class_file", 10, shallowDB);
			DatabasePredicate classfile10FTF = new DatabasePredicate("class_file", 10, fullDB);
			
			// Sophisticated result tables
			double[][] instr3Results = new double[3][INSTR3_QUERIES.length];
			double[][] classfile10Results = new double[3][CLASS_FILE10_QUERIES.length];
			int[] instr3ResultNums = new int[INSTR3_QUERIES.length];
			int[] classfile10ResultNums = new int[INSTR3_QUERIES.length];
			
			for (int i = 0; i < RUNS; i++) {
				System.out.print(".");
				for (int j = 0; j < INSTR3_QUERIES.length; j++) {
					System.gc();
					instr3ResultNums[j] = count(instr3Ref, INSTR3_QUERIES[j]); // XXX Yes, we count every run again
					instr3Results[0][j] += time(instr3Ref, INSTR3_QUERIES[j]);
					instr3Results[1][j] += time(instr3STF, INSTR3_QUERIES[j]);
					instr3Results[2][j] += time(instr3FTF, INSTR3_QUERIES[j]);
				}
				
				for (int j = 0; j < CLASS_FILE10_QUERIES.length; j++) {
					System.gc();
					classfile10ResultNums[j] = count(classfile10Ref, CLASS_FILE10_QUERIES[j]); // XXX Yes, we count every run again
					classfile10Results[0][j] += time(classfile10Ref, CLASS_FILE10_QUERIES[j]);
					classfile10Results[1][j] += time(classfile10STF, CLASS_FILE10_QUERIES[j]);
					classfile10Results[2][j] += time(classfile10FTF, CLASS_FILE10_QUERIES[j]);
				}
			}
			
			referenceDB.drop();
			shallowDB.drop();
			fullDB.drop();
			
			System.out.println("\n" + testFile);
			System.out.println("instr/3");
			System.out.println("SNum" + TBL_SEP + "Ref" + TBL_SEP + "STF" + TBL_SEP + "FTF" + TBL_SEP + "Query");
			for (int i = 0; i < INSTR3_QUERIES.length; i++) {
				System.out.println(instr3ResultNums[i] + TBL_SEP + (instr3Results[0][i] / RUNS) + TBL_SEP + (instr3Results[1][i] / RUNS) + TBL_SEP + (instr3Results[2][i] / RUNS) + TBL_SEP + Utils.termToString(INSTR3_QUERIES[i]));
			}
			System.out.println("\nclass_file/10");
			System.out.println("SNum" + TBL_SEP + "Ref" + TBL_SEP + "STF" + TBL_SEP + "FTF" + TBL_SEP + "Query");
			for (int i = 0; i < CLASS_FILE10_QUERIES.length; i++) {
				System.out.println(classfile10ResultNums[i] + TBL_SEP + (classfile10Results[0][i] / RUNS) + TBL_SEP + (classfile10Results[1][i] / RUNS) + TBL_SEP + (classfile10Results[2][i] / RUNS) + TBL_SEP + Utils.termToString(CLASS_FILE10_QUERIES[i]));
			}
		}
		
	}
	
	// Counts results number
	private int count(DatabasePredicate predicate, Term query) {
		int counter = 0;
		Solutions solutions = predicate.unify(query);
		while (solutions.next()) {
			counter++;
		}
		return counter;
	}
	
	// Measures elapsed time for getting all solutions
	private long time(DatabasePredicate predicate, Term query) {
		SW.reset();
		Solutions solutions = predicate.unify(query);
		while (solutions.next()) { /* empty */ }
		return SW.reset();
	}
}
