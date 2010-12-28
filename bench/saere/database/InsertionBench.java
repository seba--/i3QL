package saere.database;

import java.io.File;

import saere.database.index.DefaultTrieBuilder;
import saere.database.index.FullFlattener;
import saere.database.index.ShallowFlattener;
import saere.database.profiling.Profiler;
import saere.database.profiling.Profiler.Mode;
import saere.database.util.Stopwatch;
import saere.database.util.Stopwatch.Unit;

/**
 * Benchmarks insertion times.
 * 
 * @author David Sullivan
 * @version 0.2, 12/18/2010
 */
public class InsertionBench {
	
	private static final int DEACTIVATED = Integer.MAX_VALUE; // A node should never have this much children
	private static final int RUNS = 20;
	private static final String TBL_SEP = "\t";
	private static final Factbase FACTS = Factbase.getInstance();
	
	private static Database referenceDB;
	private static Database shallowDB;
	private static Database fullDB;
	
	private static boolean useProfiles = false;
	
	public static void main(String[] args) {
		InsertionBench bench = new InsertionBench();
		bench.benchmarkMapThresholds();
		useProfiles = true;
		bench.benchmarkMapThresholds();
		//bench.benchmarkInsertionOverallTimes();
		//bench.benchmarkInsertionMinMaxAvgTimes();
	}
	
	public void benchmarkMapThresholds() {
		if (!useProfiles) {
			System.out.println("Not using profiles");
		} else {
			System.out.println("Using profiles");
		}
		
		// Stabilize JVM with MMC.jar
		String[] testFiles = { DatabaseTest.TEST_FILES[1], DatabaseTest.TEST_FILES[1], DatabaseTest.TEST_FILES[2], DatabaseTest.TEST_FILES[3] };
		
		for (String testFile : testFiles) {
			benchmarkMapThresholdsForFile(testFile);
		}
	}
	
	private void benchmarkMapThresholdsForFile(String testFile) {
		Stopwatch sw = new Stopwatch(Unit.MILLISECONDS);
		FACTS.read(testFile);
		sw.printElapsed("\nReading with BAT and filling the factbase with " + testFile);
		
		int[] thresholds = { 20, 50, 100, 125, 150, 200, 300, 500, 750, 1000, DEACTIVATED };
		
		if (useProfiles) {
			Profiler.getInstance().loadProfiles(DatabaseTest.DATA_PATH + File.separator + "profiles.ser");
			Profiler.getInstance().setMode(Mode.USE);
		}
		
		// Sophisticated result table
		double[][] results = new double[3][thresholds.length];
		
		for (int i = 0; i < RUNS; i++) {			
			for (int j = 0; j < thresholds.length; j++) {		
				referenceDB = new ReferenceDatabase();
				((ReferenceDatabase) referenceDB).allowDuplicates(true);
				shallowDB = new TrieDatabase(new DefaultTrieBuilder(new ShallowFlattener(), thresholds[j]));
				fullDB = new TrieDatabase(new DefaultTrieBuilder(new FullFlattener(), thresholds[j]));
				
				sw = new Stopwatch(Unit.MILLISECONDS);
				referenceDB.fill();
				results[0][j] += sw.reset();
				shallowDB.fill();
				results[1][j] += sw.reset();
				fullDB.fill();
				results[2][j] += sw.reset();
				
				referenceDB.drop();
				shallowDB.drop();
				fullDB.drop();
			}
		}
		
		for (int i = 0; i < 3; i++) {
			for (int j = 0; j < thresholds.length; j++) {
				results[i][j] = results[i][j] / RUNS;
			}
		}
		
		System.out.print("Thr." + TBL_SEP);
		for (int threshold : thresholds)
			if (threshold == DEACTIVATED) {
				System.out.print("t=dis." + TBL_SEP);
			} else {
				System.out.print("t=" + threshold + TBL_SEP);
			}
		System.out.print("\nRef." + TBL_SEP);
		for (double result : results[0]) {
			System.out.print(result + TBL_SEP);
		}
		System.out.print("\nSTF" + TBL_SEP);
		for (double result : results[1]) {
			System.out.print(result + TBL_SEP);
		}
		System.out.print("\nFTF" + TBL_SEP);
		for (double result : results[2]) {
			System.out.print(result + TBL_SEP);
		}
		System.out.println();
		
		FACTS.drop();
	}
	
	public void benchmarkInsertionOverallTimes() {
		// Stabilize JVM with MMC.jar
		String[] testFiles = { DatabaseTest.TEST_FILES[1], DatabaseTest.TEST_FILES[1], DatabaseTest.TEST_FILES[2], DatabaseTest.TEST_FILES[3] };
		
		if (useProfiles) {
			Profiler.getInstance().loadProfiles("profiles.ser");
			Profiler.getInstance().setMode(Mode.USE);
			System.out.println("Using profiles");
		} else {
			System.out.println("Not using profiles");
		}
		
		// Sophisticated result table
		double[] results = new double[3];
		
		for (String testFile : testFiles) {
			FACTS.drop();
			FACTS.read(testFile);
			
			for (int i = 0; i < RUNS; i++) {
				System.gc();
				
				referenceDB = new ReferenceDatabase();
				((ReferenceDatabase) referenceDB).allowDuplicates(true);
				shallowDB = new TrieDatabase(new DefaultTrieBuilder(new ShallowFlattener(), DatabaseTest.GLOBAL_MAP_THRESHOLD));
				fullDB = new TrieDatabase(new DefaultTrieBuilder(new FullFlattener(), DatabaseTest.GLOBAL_MAP_THRESHOLD));
				
				Stopwatch sw = new Stopwatch(Unit.MILLISECONDS);
				referenceDB.fill();
				results[0] += sw.reset();
				
				shallowDB.fill();
				results[1] += sw.reset();
				
				fullDB.fill();
				results[2] += sw.reset();
				
				referenceDB.drop();
				shallowDB.drop();
				fullDB.drop();
			}
			
			for (int i = 0; i < results.length; i++) {
				results[i] /= RUNS;
			}
			
			System.out.println("\n" + testFile);
			System.out.println("Filling reference DB took " + results[0] + " ms in average");
			System.out.println("Filling STF DB took " + results[1] + " ms in average");
			System.out.println("Filling FTF DB took " + results[2] + " ms in average");
		}
	}
}
