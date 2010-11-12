package saere.database.index;

import static org.junit.Assert.*;
import static saere.database.BATTestQueries.*;
import static saere.database.DatabaseTest.*;
import static saere.database.Utils.*;

import java.io.File;
import java.util.Deque;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.junit.BeforeClass;
import org.junit.Test;

import saere.StringAtom;
import saere.Term;
import saere.database.DatabaseTest;
import saere.database.Factbase;
import saere.database.Stopwatch;
import saere.database.index.map.MapTrie;
import saere.database.profiling.TriePrinter;
import saere.database.profiling.TriePrinter.Mode;
import saere.meta.GenericCompoundTerm;

/**
 * Test class for the various iterator classes. Note that these tests assume 
 * that associated classes like {@link TermFlattener} or {@link TrieBuilder} 
 * implementations work properly.
 * 
 * @author David Sullivan
 * @version 0.3, 11/11/2010
 */
/*
 * This class also contains some methods/code that are helpful for debbuging...
 */
public class IteratorsTest {
	
	private static final boolean PRINT_QUERY_RESULTS = false;
	
	private static final TermFlattener SHALLOW = new ShallowFlattener();
	private static final TermFlattener FULL = new FullFlattener();
	
	private static final TrieBuilder SHALLOW_SIMPLE = new SimpleTrieBuilder(SHALLOW, 10); // 50 seems good
	private static final TrieBuilder FULL_SIMPLE = null;
	private static final TrieBuilder SHALLOW_COMPLEX = null;
	private static final TrieBuilder FULL_COMPLEX = null;
	
	private static final Factbase FACTS = Factbase.getInstance();
	
	/**
	 * The test file for this {@link IteratorsTest}. It must be a JAR, ZIP or CLASS 
	 * file. The larger the file, the longer will the tests of this 
	 * {@link IteratorsTest} take.
	 */
	//private static String testFile = DatabaseTest.DATA_PATH + File.separator + "HelloWorld.class";
	private static String testFile = DatabaseTest.DATA_PATH + File.separator + "opal-0.5.0.jar";
	
	/**
	 * Sets the {@link #testFile}.
	 * 
	 * @param testFile
	 * @see #testFile
	 */
	public static void testFile(String testFile) {
		IteratorsTest.testFile = testFile;
	}
	
	/**
	 * Gets the {@link #testFile}.
	 * 
	 * @return The test file for this instance.
	 * @see #testFile
	 */
	public static String testFile() {
		return testFile;
	}
	
	@BeforeClass
	public static void readFactsOnce() {
		Stopwatch sw = new Stopwatch();
		FACTS.drop();
		FACTS.read(testFile);
		sw.printElapsed("Reading the facts from " + testFile);
	}

	/**
	 * Test for the {@link NodeIterator}. (It's very small since all 
	 * nodes of a {@link Trie} have to be known.)
	 */
	@Test
	public void testNodeIterator() {
		Trie root = Trie.root();
		
		Term f = StringAtom.StringAtom("f");
		Term fa = new GenericCompoundTerm(StringAtom.StringAtom("f"), new Term[] {
			StringAtom.StringAtom("a")
		});
		Term fab = new GenericCompoundTerm(StringAtom.StringAtom("f"), new Term[] {
			StringAtom.StringAtom("a"),
			StringAtom.StringAtom("b")
		});
		Term fabc = new GenericCompoundTerm(StringAtom.StringAtom("f"), new Term[] {
			StringAtom.StringAtom("a"),
			StringAtom.StringAtom("b"),
			StringAtom.StringAtom("c")
		});
		Term fabd = new GenericCompoundTerm(StringAtom.StringAtom("f"), new Term[] {
			StringAtom.StringAtom("a"),
			StringAtom.StringAtom("b"),
			StringAtom.StringAtom("d")
		});
		
		List<Trie> expecteds = new LinkedList<Trie>();
		expecteds.add(root);
		expecteds.add(SHALLOW_SIMPLE.insert(f, root));
		expecteds.add(SHALLOW_SIMPLE.insert(fa, root));
		expecteds.add(SHALLOW_SIMPLE.insert(fab, root));
		expecteds.add(SHALLOW_SIMPLE.insert(fabc, root));
		expecteds.add(SHALLOW_SIMPLE.insert(fabd, root));
		// TODO Also add intermediate nodes to 'expecteds'...
		
		List<Trie> actuals = new LinkedList<Trie>();
		Iterator<Trie> iter = SHALLOW_SIMPLE.nodeIterator(root);
		while (iter.hasNext()) {
			actuals.add(iter.next());
		}
		
		// Since we don't really test, print the trie nodes so we could check the result manually
		int number = 0;
		for (Trie trie : actuals) {
			System.out.println("Trie " + (number++) + ": " + trie);
		}
		
		//TriePrinter.print(root, SHALLOW_SIMPLE, "c:/users/leaf/desktop/mini-trie.gv", Mode.BOX);
		//assertTrue(DatabaseTest.same(expecteds, actuals));
	}
	
	/**
	 * Test for the {@link TermIterator} with a 
	 * {@link ShallowFlattener} and a {@link SimpleTrieBuilder}.
	 */
	@Test
	public void testTermIterator_ShallowSimple() {
		assertTrue(testTermIterator(SHALLOW_SIMPLE));
	}
	
	/**
	 * Test for the {@link TermIterator} with a 
	 * {@link FullFlattener} and a {@link SimpleTrieBuilder}.
	 */
	@Test
	public void testTermIterator_FullSimple() {
		assertTrue(testTermIterator(FULL_SIMPLE));
	}
	
	/**
	 * Test for the {@link TermIterator} with a 
	 * {@link ShallowFlattener} and a {@link ComplexTrieBuilder}.
	 */
	@Test
	public void testTermIterator_ShallowComplex() {
		assertTrue(testTermIterator(SHALLOW_COMPLEX));
	}
	
	/**
	 * Test for the {@link TermIterator} with a 
	 * {@link FullFlattener} and a {@link ComplexTrieBuilder}.
	 */
	@Test
	public void testTermIterator_FullComplex() {
		assertTrue(testTermIterator(FULL_COMPLEX));
	}
	
	/**
	 * Tests the {@link MapTrie.SimpleTermIterator} with the current configuration and 
	 * the specified <tt>filename</tt>.
	 * 
	 * @return <tt>true</tt> if the iterator works correct.
	 */
	private boolean testTermIterator(TrieBuilder builder) {
		if (builder == null) {
			System.out.println("Trie builder is null");
			return false;
		}
		
		Deque<Term> expecteds = new LinkedList<Term>();
		for (Term fact : FACTS.getFacts()) {
			expecteds.push(fact);
		}
		
		Trie root = Trie.root();
		fill(builder, root);
		
		Deque<Term> actuals = new LinkedList<Term>();
		Iterator<Term> iter = builder.iterator(root);
		while (iter.hasNext()) {
			actuals.push(iter.next());
		}
		
		//TriePrinter.print(root, builder, "c:/users/leaf/desktop/trie.gv", Mode.BOX);
		
		if (!DatabaseTest.same(expecteds, actuals)) {
			return false;
		} else {
			return true;
		}
	}

	@Test
	public void testQueryIterator_ShallowSimple() {	
		Trie root = Trie.root();
		fill(SHALLOW_SIMPLE, root);
		assertTrue(testQueryIterator(root, SHALLOW_SIMPLE));
	}
	
	private boolean testQueryIterator(Trie trie, TrieBuilder builder) {
		boolean error = false;
		for (Term query : ALL_QUERIES) {
			System.out.print("\nQuerying for " + termToString(query) + "... ");
			
			// Compose the list of expected facts by real unification.
			Deque<Term> expecteds = new LinkedList<Term>();
			for (Term fact : FACTS.getFacts()) {
				if (match(fact, query)) {
					expecteds.push(fact);
				}
			}
			
			// Compose the list of actual facts with a trie. This set can be 
			// larger than the expected set if we use shallow term flattening. 
			// However, every element in the expected set must be in the actual 
			// set.
			Deque<Term> actuals = new LinkedList<Term>();
			Iterator<Term> iter = builder.iterator(trie, query);
			while (iter.hasNext()) {
				actuals.push(iter.next());
			}
			
			if (builder == SHALLOW_SIMPLE || builder == SHALLOW_COMPLEX) {
				if (!contains(actuals, expecteds)) {
					System.out.println("FAILED");
					error = true;
				} else {
					System.out.println("SUCCEEDED");
				}
				System.out.println("Shallow simple query iterator result size " + actuals.size() + ", real result size " + expecteds.size());
			} else {
				if (!same(expecteds, actuals)) {
					System.out.println("FAILED");
					error = true;
				} else {
					System.out.println("SUCCEEDED");
				}
			}
			
			if (PRINT_QUERY_RESULTS) {
				System.out.println("Query results:");
				for (Term result : actuals) {
					System.out.println(termToString(result));
				}
			}
		}
		
		return !error;
	}
}
