package saere.database;

import java.io.File;
import java.util.Collection;
import java.util.List;

import org.junit.runner.JUnitCore;

import saere.State;
import saere.Term;
import saere.database.index.IteratorsTest;
import saere.database.index.LabelStackTest;
import saere.database.index.LabelTest;
import saere.database.index.TermRemovalTest;
import saere.database.index.Trie;
import saere.database.index.TrieBuilder;
import saere.database.util.Stopwatch;

/**
 * Starter for JUnit tests concering the {@link saere.database} package. 
 * Contains also some useful utility methods for testing.
 * 
 * @author David Sullivan
 * @version 0.11, 12/6/2010
 */
public final class DatabaseTest {
	
	public static final String DATA_PATH = "test" + File.separator + "data";
	public static final String[] TEST_FILES = {
		 DATA_PATH + File.separator + "HelloWorld.class",
		 DATA_PATH + File.separator + "MMC.jar",
		 DATA_PATH + File.separator + "opal-0.5.0.jar",
		 DATA_PATH + File.separator + "Tomcat-6.0.20.zip",
		 DATA_PATH + File.separator + "Vuze_4510.jar"
	};
	public static String GLOBAL_TEST_FILE = TEST_FILES[2]; // not final
	public static final int GLOBAL_MAP_THRESHOLD = 100;
	
	public static void main(String[] args) {
		if (args.length == 1) {
			GLOBAL_TEST_FILE = args[0];
		}
		
		JUnitCore.runClasses(
			IteratorsTest.class,
			LabelStackTest.class,
			LabelTest.class,
			TermRemovalTest.class
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
		if (coll0.size() == coll1.size() && coll0.containsAll(coll1)) {
			return true;
		} else {
			return false;
		}
	}
	
	/**
	 * Checks wether the first specified collection contains all elements of 
	 * the second.
	 * 
	 * @param coll0 The first collection.
	 * @param coll1 The second collection.
	 * @return <tt>true</tt> if the first collection contains the second.
	 */
	public static boolean contains(Collection<?> coll0, Collection<?> coll1) {
		return coll0.containsAll(coll1);
	}
	
	/**
	 * Checks wether the specified term and query unify. Sets the state of term 
	 * and query as is at was before the unification.
	 * 
	 * @param term The term.
	 * @param query The query.
	 * @return <tt>true</tt> if so.
	 */
	public static boolean match(Term term, Term query) {
		State termState = term.manifestState();
		State queryState = query.manifestState();
		boolean unifies = term.unify(query);
		term.setState(termState);
		query.setState(queryState);
		
		return unifies;
	}
	
	/**
	 * Prints the specified {@link Term} sets to the console.
	 * 
	 * @param expecteds The expected set.
	 * @param actuals The actual set.
	 */
	public static void printTermCollections(Collection<Term> expecteds, Collection<Term> actuals) {
		System.out.println("\n\nExpected set 1 (" + expecteds.size() + " elements)");
		for (Term term : expecteds) {
			System.out.println(Utils.termToString(term));
		}

		System.out.println("\nActual set 2 (" + actuals.size() + " elements)");
		for (Term term : actuals) {
			System.out.println(Utils.termToString(term));
		}
	}
	
	public static void printMissingActuals(Collection<Term> expecteds, Collection<Term> actuals) {
		if (actuals.size() < expecteds.size()) {
			System.out.println("expecteds size is " + expecteds.size() + ", actuals size is " + actuals.size() + ", missing actuals:");
			for (Term actual : actuals) {
				if (!expecteds.contains(actual))
					System.out.println(Utils.termToString(actual));
			}
		}
	}
	
	/**
	 * Fills the root with the builder by using the {@link Factbase} and 
	 * {@link OtherTestFacts}.
	 * 
	 * @param builder The builder that inserts.
	 * @param root The root that gets inserts.
	 */
	public static void fill(TrieBuilder builder, Trie root) {
		TrieBuilder.replaceCounter = 0;
		List<Term> facts = Factbase.getInstance().getFacts();
		Stopwatch sw = new Stopwatch();
		for (Term fact : facts) {
			builder.insert(fact, root);
		}
		sw.printElapsed("\nFilling a " + builder.toString() + " trie with " + facts.size() + " facts");
		System.out.println(TrieBuilder.replaceCounter + " replacements");
	}
}
