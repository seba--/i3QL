package saere.database;

import java.io.File;
import java.util.Collection;

import org.junit.runner.JUnitCore;

import saere.database.index.IteratorsTest;
import saere.database.index.MatcherTest;
import saere.database.index.TermFlattenerTest;
import saere.database.index.TermStackTest;

/**
 * Starter for JUnit tests concering the {@link saere.database} package. 
 * Contains also some useful utility methods for testing.
 * 
 * @author David Sullivan
 * @version 0.104, 11/1/2010
 */
public class DatabaseTest {
	
	public static final String DATA_PATH = "test" + File.separator + "data";
	public static final String GLOBAL_TEST_FILE = DATA_PATH + File.separator + "opal-0.5.0.jar";
	
	public static void main(String[] args) {
		JUnitCore.runClasses(
			IteratorsTest.class,
			MatcherTest.class,
			TermStackTest.class,
			TermFlattenerTest.class
		);
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
	public static boolean same(Collection<?> coll0, Collection<?> coll1) {
		if (coll0.size() == coll1.size() && coll0.containsAll(coll1)) { // may take very long
			return true;
		} else {
			return false;
		}
	}
}
