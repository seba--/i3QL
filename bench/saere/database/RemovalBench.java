package saere.database;

import java.util.HashSet;
import java.util.Random;

import org.junit.Test;

import saere.Term;
import saere.database.index.DefaultTrieBuilder;
import saere.database.index.FullFlattener;
import saere.database.index.ShallowFlattener;
import saere.database.profiling.Profiler;
import saere.database.profiling.Profiler.Mode;
import saere.database.util.Stopwatch;
import saere.database.util.Stopwatch.Unit;

public final class RemovalBench {
	
	private static final String TBL_SEP = "\t";
	private static final int RUNS = 5; // 10
	private static final boolean USE_PROFILES = false;
	private static final Factbase FACTS = Factbase.getInstance();
	
	private static Database referenceDB;
	private static Database shallowDB;
	private static Database fullDB;
	private static Random r = new Random();
	
	public static void main(String[] args) {
		RemovalBench bench = new RemovalBench();
		Stopwatch sw = new Stopwatch(Unit.SECONDS);
		bench.benchmarkRemovals();
		sw.printElapsed("Benchmarking removals");
	}
	
	@Test
	public void benchmarkRemovals() {
		// one warm-up run with MMC.jar
		String[] testFiles = { DatabaseTest.TEST_FILES[1], DatabaseTest.TEST_FILES[1], DatabaseTest.TEST_FILES[2], DatabaseTest.TEST_FILES[3] };
		int[] removeNumbers = { 1, 20, 100, 500, 1000, 3000 };
		
		if (USE_PROFILES) {
			Profiler.getInstance().loadProfiles("profiles.ser");
			Profiler.getInstance().setMode(Mode.USE);
			System.out.println("Using profiles");
		} else {
			System.out.println("Not using profiles");
		}
		
		for (String testFile : testFiles) {
			FACTS.drop();
			FACTS.read(testFile);
			
			// Sophisticated result table
			double[][] results = new double[3][removeNumbers.length];
			
			for (int i = 0; i < removeNumbers.length; i++) {
				
				System.out.print("\nRemoving " + removeNumbers[i] + " terms ");
				for (int j = 0; j < RUNS; j++) {
					
					// Compose remove sets
					Term[][] removeTermSets = new Term[removeNumbers.length][];
					int c = 0;
					for (int removeNumber : removeNumbers) {
						removeTermSets[c++] = composeRemovalSet(removeNumber);
					}
					
					System.out.print(".");
					System.gc();
					Stopwatch sw = new Stopwatch(Unit.MICROSECONDS);
					
					referenceDB = new ReferenceDatabase();
					((ReferenceDatabase) referenceDB).allowDuplicates(true);
					referenceDB.fill();
					sw.reset();
					for (Term termToRemove : removeTermSets[i]) {
						referenceDB.remove(termToRemove);
					}
					results[0][i] += sw.reset();
					referenceDB.drop();
					
					shallowDB = new TrieDatabase(new DefaultTrieBuilder(new ShallowFlattener(), DatabaseTest.GLOBAL_MAP_THRESHOLD));
					shallowDB.fill();
					sw.reset();
					for (Term termToRemove : removeTermSets[i]) {
						shallowDB.remove(termToRemove);
					}
					results[1][i] += sw.reset();
					shallowDB.drop();
					
					fullDB = new TrieDatabase(new DefaultTrieBuilder(new FullFlattener(), DatabaseTest.GLOBAL_MAP_THRESHOLD));
					fullDB.fill();
					sw.reset();
					for (Term termToRemove : removeTermSets[i]) {
						fullDB.remove(termToRemove);
					}
					results[2][i] += sw.reset();
					fullDB.drop();
				}
			}
			
			System.out.println("\n\n" + testFile);
			for (int removeNumber : removeNumbers) {
				System.out.print(removeNumber + TBL_SEP);
			}
			for (int i = 0; i < 3; i++) {
				System.out.println();
				for (int j = 0; j < removeNumbers.length; j++) {
					results[i][j] /= RUNS;
					System.out.print(results[i][j] + TBL_SEP);
				}
			}
			System.out.println();
		}
	}
	
	private Term[] composeRemovalSet(int numberToRemove) {
		HashSet<Integer> removalSet = new HashSet<Integer>();
		
		int counter = 0;
		while (counter < numberToRemove) { // XXX Infinite loop in theory
			int rndNum = r.nextInt(FACTS.size());
			if (!removalSet.contains(rndNum)) {
				counter++;
				removalSet.add(rndNum);
			}
		}
		
		Term[] termsToRemove = new Term[numberToRemove];
		int i = 0;
		for (Integer index : removalSet) {
			termsToRemove[i++] = FACTS.get(index);
		}
		
		return termsToRemove;
	}
}
