package saere.database.index.simple;

import java.io.File;
import java.util.Collection;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import saere.Term;
import saere.database.DatabaseTest;
import saere.database.Factbase;
import saere.database.Stopwatch;
import saere.database.profiling.InsertionLogger;
import saere.database.profiling.PostgreSQL;

public class NewShallowSimpleTest {
	
	private static final String TEST_FILE = DatabaseTest.DATA_PATH + File.separator + "opal-0.5.0.jar";
	private static final Factbase FACTS = Factbase.getInstance();
	private static final PostgreSQL SQL_DB = new PostgreSQL();
	
	public static void main(String[] args) {
		NewShallowSimpleTest.initialize();
		InsertionLogger.setDatabase(SQL_DB);
		SQL_DB.modify("DELETE FROM insertions");
		new NewShallowSimpleTest().testInsertions();
		NewShallowSimpleTest.shutdown();
	}
	
	@BeforeClass
	public static void initialize() {
		Stopwatch sw = new Stopwatch();
		FACTS.read(TEST_FILE);
		sw.printElapsed("Readings the facts");
		SQL_DB.connect();
	}
	
	@Test
	public void testInsertions() {
		SimpleTrieBuilder builder = new SimpleTrieBuilder();
		Trie root;
		
		for (int i = 0; i < 1; i++) { // Only once, other duplicate primary keys in DB!
			root = new Trie();			
			Stopwatch sw = new Stopwatch();
			for (Term fact : FACTS.getFacts()) {
				builder.insert(fact, root);
			}
			sw.printElapsed("Inserting the terms");
			root = null;
			System.gc();
		}
		
	}
	
	/**
	 * Checks wether the two specified collections are the <i>same</i>. Two 
	 * collections are the same if they have the same size and if one 
	 * collection contains all elements of the other (but possibly in different 
	 * order).
	 * 
	 * @param coll0 The first collection.
	 * @param coll1 The second collection.
	 * @return <tt>true</tt> if the two collections are the same.
	 */
	@SuppressWarnings("all")
	private static boolean same(Collection<?> coll0, Collection<?> coll1) {
		if (coll0.size() == coll1.size() && coll0.containsAll(coll1)) { // may take very long
			return true;
		} else {
			return false;
		}
	}
	
	@AfterClass
	public static void shutdown() {
		SQL_DB.disconnect();
	}
}
