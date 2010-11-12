package saere.database;

import java.io.File;
import java.util.Collection;
import java.util.List;

import org.junit.runner.JUnitCore;

import saere.State;
import saere.Term;
import saere.database.index.IteratorsTest;
import saere.database.index.LabelTest;
import saere.database.index.TermFlattenerTest;
import saere.database.index.LabelStackTest;
import saere.database.index.Trie;
import saere.database.index.TrieBuilder;

/**
 * Starter for JUnit tests concering the {@link saere.database} package. 
 * Contains also some useful utility methods for testing.
 * 
 * @author David Sullivan
 * @version 0.105, 11/11/2010
 */
public class DatabaseTest {
	
	public static final String DATA_PATH = "test" + File.separator + "data";
	public static final String GLOBAL_TEST_FILE = DATA_PATH + File.separator + "opal-0.5.0.jar";
	
	public static void main(String[] args) {
		JUnitCore.runClasses(
			IteratorsTest.class,
			LabelTest.class,
			LabelStackTest.class,
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
	 * @param expected The expected set.
	 * @param actuals The actual set.
	 */
	public static void printTermCollections(Collection<Term> expected, Collection<Term> actuals) {
		System.out.println("\n\nExpected set 1 (" + expected.size() + " elements)");
		for (Term term : expected) {
			System.out.println(Utils.termToString(term));
		}

		System.out.println("\nActual set 2 (" + actuals.size() + " elements)");
		for (Term term : actuals) {
			System.out.println(Utils.termToString(term));
		}
	}
	
	/**
	 * Fills the root with the builder by using the {@link Factbase}.
	 * 
	 * @param builder The builder that inserts.
	 * @param root The root that gets inserts.
	 */
	public static void fill(TrieBuilder builder, Trie root) {
		Stopwatch sw = new Stopwatch();
		List<Term> facts = Factbase.getInstance().getFacts();
		for (Term fact : facts) {
			builder.insert(fact, root);
		}
		sw.printElapsed("Filling a " + builder.toString() + " trie with " + facts.size() + " facts");
		System.out.println(TrieBuilder.replaceCounter + " replacements");
	}
}
