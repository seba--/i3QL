package saere.database;

import java.io.File;

import saere.database.index.DefaultTrieBuilder;
import saere.database.index.FullFlattener;
import saere.database.index.ShallowFlattener;
import saere.database.profiling.Profiler;
import saere.database.util.TrieInspector;

/**
 * Class to compare the structure of STF and FTF tries.
 * 
 * @author David Sullivan
 * @version 0.1, 12/10/2010
 */
public class StructureAnalyzer {
	
	private static final Factbase FACTS = Factbase.getInstance();
	private static final Profiler PROFILER = Profiler.getInstance();
	private static final TrieDatabase STF_TRIE_DB = new TrieDatabase(new DefaultTrieBuilder(new ShallowFlattener(), DatabaseTest.GLOBAL_MAP_THRESHOLD));
	private static final TrieDatabase FTF_TRIE_DB = new TrieDatabase(new DefaultTrieBuilder(new FullFlattener(), DatabaseTest.GLOBAL_MAP_THRESHOLD));
	
	public static void main(String[] args) {
		new StructureAnalyzer().analyze();
	}
	
	public void analyze() {
		String[] files = { DatabaseTest.TEST_FILES[1], DatabaseTest.TEST_FILES[2], DatabaseTest.TEST_FILES[3] };
		PROFILER.loadProfiles(DatabaseTest.DATA_PATH + File.separator + "profiles.ser");
		TrieInspector inspector = new TrieInspector();
		int i = 0;
		for (String file : files) {
			System.out.print("\nReading file " + file + " ...");
			FACTS.drop();
			FACTS.read(file);
			System.out.println(" DONE (" + FACTS.size() + " facts)");
			
			System.out.println("\nWithout profiles:");
			PROFILER.setMode(Profiler.Mode.OFF);
			STF_TRIE_DB.fill();
			FTF_TRIE_DB.fill();
			System.out.println("\nSTF trie:");
			inspector.inspect(STF_TRIE_DB.root(), STF_TRIE_DB.trieBuilder());
			System.out.println("\nFTF trie:");
			inspector.inspect(FTF_TRIE_DB.root(), FTF_TRIE_DB.trieBuilder());
			STF_TRIE_DB.drop();
			FTF_TRIE_DB.drop();
			
			i++;
			if (i == 3) {
				System.out.println();
			}
			
			System.out.println("\nWith profiles:");
			PROFILER.setMode(Profiler.Mode.USE);
			STF_TRIE_DB.fill();
			FTF_TRIE_DB.fill();
			System.out.println("\nSTF trie:");
			inspector.inspect(STF_TRIE_DB.root(), STF_TRIE_DB.trieBuilder());
			System.out.println("\nFTF trie:");
			inspector.inspect(FTF_TRIE_DB.root(), FTF_TRIE_DB.trieBuilder());
			STF_TRIE_DB.drop();
			FTF_TRIE_DB.drop();
		}
	}
}
