package saere.database.index;

import static org.junit.Assert.assertTrue;
import static saere.database.DatabaseTest.contains;
import static saere.database.DatabaseTest.fill;
import static saere.database.DatabaseTest.match;
import static saere.database.DatabaseTest.same;
import static saere.database.Utils.termToString;

import java.io.File;
import java.util.Deque;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.junit.BeforeClass;
import org.junit.Test;

import saere.StringAtom;
import saere.Term;
import saere.database.BATTestQueries;
import saere.database.DatabaseTest;
import saere.database.Factbase;
import saere.database.Stopwatch;
import saere.database.TestFacts;
import saere.meta.GenericCompoundTerm;

/**
 * Test class for the various iterator classes. Note that these tests assume 
 * that associated classes like {@link TermFlattener} or {@link TrieBuilder} 
 * implementations work properly. The test may take also relatively long as the 
 * results have to be validated with real unification.
 * 
 * @author David Sullivan
 * @version 0.4, 11/15/2010
 */

public class IteratorsTest {
	
	private static final boolean PRINT_QUERY_RESULTS = true;
	
	private static final TermFlattener SHALLOW = new ShallowFlattener();
	private static final TermFlattener FULL = new FullFlattener();
	
	private static final TrieBuilder SHALLOW_SIMPLE = new SimpleTrieBuilder(SHALLOW, 50); // 50 seems good
	private static final TrieBuilder FULL_SIMPLE = new SimpleTrieBuilder(FULL, 50);
	private static final TrieBuilder SHALLOW_COMPLEX = new ComplexTrieBuilder(SHALLOW, 50);
	private static final TrieBuilder FULL_COMPLEX = null;
	
	private static final Factbase FACTS = Factbase.getInstance();
	
	/** The BAT and TestFacts queries together. */
	private static Term[] allQueries;
	
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
	public static void initialize() {
		Stopwatch sw = new Stopwatch();
		FACTS.drop();
		FACTS.read(testFile);
		sw.printElapsed("Reading the facts from " + testFile);
		
		for (Term fact : TestFacts.ALL_TERMS) {
			FACTS.add(fact);
		}
		
		allQueries = new Term[BATTestQueries.ALL_QUERIES.length + TestFacts.ALL_QUERIES.length];
		int i;
		for (i = 0; i < BATTestQueries.ALL_QUERIES.length; i++) {
			allQueries[i] = BATTestQueries.ALL_QUERIES[i];
		}
		for (int j = 0; j < TestFacts.ALL_QUERIES.length; j++) {
			allQueries[j + i] = TestFacts.ALL_QUERIES[j];
		}
	}

	/**
	 * Test for the {@link NodeIterator}. (It's very small since all 
	 * nodes of a {@link Trie} have to be known.)
	 */
	@Test
	public void testNodeIterator() {
		Trie root = Trie.newRoot();
		
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
			System.out.println("\nTrie builder is null");
			return false;
		}
		
		Deque<Term> expecteds = new LinkedList<Term>();
		for (Term fact : FACTS.getFacts()) {
			expecteds.push(fact);
		}
		
		Trie root = Trie.newRoot();
		fill(builder, root);
		
		Deque<Term> actuals = new LinkedList<Term>();
		Iterator<Term> iter = builder.iterator(root);
		while (iter.hasNext()) {
			actuals.push(iter.next());
		}
		
		if (!DatabaseTest.same(expecteds, actuals)) {
			//TriePrinter.print(root, builder, "c:/users/leaf/desktop/trie.gv", Mode.BOX);
			return false;
		} else {
			return true;
		}
	}

	@Test
	public void testQueryIterator_ShallowSimple() {	
		Trie root = Trie.newRoot();
		fill(SHALLOW_SIMPLE, root);
		assertTrue(testQueryIterator(root, SHALLOW_SIMPLE));
	}
	
	@Test
	public void testQueryIterator_FullSimple() {
		Trie root = Trie.newRoot();		
		fill(FULL_SIMPLE, root);
		assertTrue(testQueryIterator(root, FULL_SIMPLE));
	}
	
	private boolean testQueryIterator(Trie trie, TrieBuilder builder) {
		boolean error = false;
		for (Term query : allQueries) {
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
			
			boolean success = false;
			if (builder == SHALLOW_SIMPLE || builder == SHALLOW_COMPLEX) {
				if (!contains(actuals, expecteds)) {
					System.out.println("FAILED");
					error = true;
				} else {
					success = true;
					System.out.println("SUCCEEDED");
				}
			} else {
				if (!same(expecteds, actuals)) {
					System.out.println("FAILED");
					error = true;
				} else {
					success = true;
					System.out.println("SUCCEEDED");
				}
			}
			System.out.println(builder + " query iterator result size " + actuals.size() + ", real result size " + expecteds.size());
			
			if (PRINT_QUERY_RESULTS && !success) {
				System.out.println("Query results:");
				for (Term result : actuals) {
					System.out.println(termToString(result));
				}
				System.out.println("Expected results:");
				for (Term result : expecteds) {
					System.out.println(termToString(result));
				}
			}
		}
		
		return !error;
	}
	
	@Test
	public void testVariableIterator() {
		TrieBuilder builder = FULL_SIMPLE;
		Trie root = Trie.newHashRoot();
		for (Term fact : TestFacts.ALL_TERMS) {
			builder.insert(fact, root);
		}
		
		//TriePrinter.print(root, builder, "c:/users/leaf/desktop/var-trie.gv", Mode.BOX);
		
		Trie a2 = root.getFirstChild().getFirstChild();
		VariableIterator iter = new VariableIterator(a2);
		List<Trie> actuals = new LinkedList<Trie>();
		while (iter.hasNext()) {
			actuals.add(iter.next());
		}
		
		// Manually compose the set of expected nodes w.r.t. TestFacts
		Trie faxY = root.getFirstChild().getFirstChild().getFirstChild().getFirstChild();
		Trie faX = root.getFirstChild().getFirstChild().getNextSibling().getFirstChild();
		Trie faxyZ = root.getFirstChild().getFirstChild().getNextSibling().getNextSibling().getFirstChild().getFirstChild().getFirstChild();
		Trie fA = root.getFirstChild().getFirstChild().getNextSibling().getNextSibling().getNextSibling();
		Trie fdxY = root.getFirstChild().getFirstChild().getNextSibling().getNextSibling().getNextSibling().getNextSibling().getFirstChild().getFirstChild();
		List<Trie> expecteds = new LinkedList<Trie>();
		expecteds.add(faxY);
		expecteds.add(faX);
		expecteds.add(faxyZ);
		expecteds.add(fA);
		expecteds.add(fdxY);
		
		assertTrue(same(expecteds, actuals));
	}
}
