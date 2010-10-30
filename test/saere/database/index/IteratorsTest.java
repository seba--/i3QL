package saere.database.index;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.Arrays;
import java.util.Collection;
import java.util.Deque;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.junit.BeforeClass;
import org.junit.Test;

import saere.IntegerAtom;
import saere.StringAtom;
import saere.Term;
import saere.Variable;
import saere.database.DatabaseTest;
import saere.database.Factbase;
import saere.database.Stopwatch;
import saere.database.Utils;
import saere.meta.GenericCompoundTerm;

/**
 * Test class for the various iterator classes. Note that these tests assume 
 * that associated classes like {@link TermFlattener} or {@link TrieBuilder} 
 * implementations work properly.
 * 
 * @author David Sullivan
 * @version 0.3, 10/11/2010
 */
/*
 * This class also contains some methods/code that are helpful for debbuging...
 */
public class IteratorsTest {
	
	private static final TermFlattener SHALLOW = new ShallowTermFlattener();
	private static final TermFlattener RECURSIVE = new RecursiveTermFlattener();
	private static final TrieBuilder SIMPLE = new SimpleTermInserter();
	private static final TrieBuilder COMPLEX = new ComplexTermInserter();
	
	private static final Factbase FACTS = Factbase.getInstance();
	
	private Trie trie;
	
	/**
	 * The test file for this {@link IteratorsTest}. It must be a JAR, ZIP or CLASS 
	 * file. The larger the file, the longer will the tests of this 
	 * {@link IteratorsTest} take.
	 */
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
	 * Test for the {@link TrieNodeIterator}. (It's relatively small since all 
	 * nodes of a {@link Trie} have to be known.)
	 */
	@Test
	public void testTrieNodeIterator() {
		Trie.setTermFlattener(SHALLOW);
		Trie.setTermInserter(SIMPLE);
		
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
		Trie root = new Trie();
		expecteds.add(root);
		expecteds.add(root.insert(f));
		expecteds.add(root.insert(fa));
		expecteds.add(root.insert(fab));
		expecteds.add(root.insert(fabc));
		expecteds.add(root.insert(fabd));
		
		/*
		 * The trie should now look like this (and each node stores one term):
		 * 
		 * 			   ,--d
		 * root--f--a--b--c
		 */
		
		List<Trie> actuals = new LinkedList<Trie>();
		Iterator<Trie> iter = root.nodeIterator();
		while (iter.hasNext()) {
			actuals.add(iter.next());
		}
		
		assertTrue(same(expecteds, actuals));
	}
	
	/**
	 * Test for the {@link TrieTermIterator} with a 
	 * {@link ShallowTermFlattener} and a {@link SimpleTermInserter}.
	 */
	@Test
	public void testTrieTermIterator_ShallowSimple() {
		Trie.setTermFlattener(SHALLOW);
		Trie.setTermInserter(SIMPLE);
		assertTrue(testTrieTermIterator());
	}
	
	/**
	 * Test for the {@link TrieTermIterator} with a 
	 * {@link RecursiveTermFlattener} and a {@link SimpleTermInserter}.
	 */
	@Test
	public void testTrieTermIterator_RecursiveSimple() {
		Trie.setTermFlattener(RECURSIVE);
		Trie.setTermInserter(SIMPLE);
		assertTrue(testTrieTermIterator());
	}
	
	/**
	 * Test for the {@link TrieTermIterator} with a 
	 * {@link ShallowTermFlattener} and a {@link ComplexTermInserter}.
	 */
	@Test
	public void testTrieTermIterator_ShallowComplex() {
		Trie.setTermFlattener(SHALLOW);
		Trie.setTermInserter(COMPLEX);
		assertTrue(testTrieTermIterator());
	}
	
	/**
	 * Test for the {@link TrieTermIterator} with a 
	 * {@link RecursiveTermFlattener} and a {@link ComplexTermInserter}.
	 */
	@Test
	public void testTrieTermIterator_RecursiveComplex() {
		Trie.setTermFlattener(RECURSIVE);
		Trie.setTermInserter(COMPLEX);
		assertTrue(testTrieTermIterator());
	}
	
	@Test
	public void testSimpleTrieTermIterator_Shallow() {
		Trie.setTermFlattener(SHALLOW);
		Trie.setTermInserter(SIMPLE);
		
		Term instr = StringAtom.StringAtom("instr");
		Term m_2 = StringAtom.StringAtom("m_2");
		Term i1 = IntegerAtom.IntegerAtom(1);
		Term invoke = StringAtom.StringAtom("invoke");
		Term X = new Variable();
		
		Term[][] queries = {
				new Term[] { instr, X, i1 },
				new Term[] { X, X, invoke },
				new Term[] { instr, m_2 }
		};
		
		trie = new Trie();
		fill(trie);
		
		boolean error = false;
		for (Term[] query : queries) {
			if (!testQueryTermIterator(query, SHALLOW)) {
				error = true;
				System.err.println("Query " + Arrays.toString(query) + " failed (shallow, simple)");
			}
		}
		
		assertTrue(!error); // yes, ANY of the three queries may be wrong...
	}
	
	@Test
	public void testSimpleTrieTermIterator_Recursive() {
		Trie.setTermFlattener(RECURSIVE);
		Trie.setTermInserter(SIMPLE);
		
		Term instr = StringAtom.StringAtom("instr");
		Term m_2 = StringAtom.StringAtom("m_2");
		Term i1 = IntegerAtom.IntegerAtom(1);
		Term invoke = StringAtom.StringAtom("invoke");
		Term X = new Variable();
		
		Term[][] queries = {
				new Term[] { instr, X, i1 },
				new Term[] { X, X, invoke },
				new Term[] { instr, m_2 }
		};
		
		trie = new Trie();
		fill(trie);
		
		boolean error = false;
		for (Term[] query : queries) {
			if (!testQueryTermIterator(query, SHALLOW)) {
				error = true;
				System.err.println("Query " + Arrays.toString(query) + " failed (recursive, simple)");
			}
		}
		
		assertTrue(!error); // yes, ANY of the three queries may be wrong...
	}
	
	@Test
	public void testComplexTrieTermIterator_Shallow() {
		Trie.setTermFlattener(SHALLOW);
		Trie.setTermInserter(COMPLEX);
		
		Term instr = StringAtom.StringAtom("instr");
		Term m_2 = StringAtom.StringAtom("m_2");
		Term i1 = IntegerAtom.IntegerAtom(1);
		Term invoke = StringAtom.StringAtom("invoke");
		Term X = new Variable();
		
		Term[][] queries = {
				new Term[] { instr, X, i1 },
				new Term[] { X, X, X, invoke },
				new Term[] { instr, m_2 }
		};
		
		trie = new Trie();
		fill(trie);
		
		boolean error = false;
		for (Term[] query : queries) {
			if (!testQueryTermIterator(query, SHALLOW)) {
				error = true;
				System.err.println("Query " + Arrays.toString(query) + " failed (shallow, complex)");
			}
		}
		
		assertTrue(!error); // yes, ANY of the three queries may be wrong...
	}
	
	@Test
	public void testComplexTrieTermIterator_Recursive() {
		Trie.setTermFlattener(RECURSIVE);
		Trie.setTermInserter(COMPLEX);
		
		Term instr = StringAtom.StringAtom("instr");
		Term m_2 = StringAtom.StringAtom("m_2");
		Term i1 = IntegerAtom.IntegerAtom(1);
		Term invoke = StringAtom.StringAtom("invoke");
		Term X = new Variable();
		
		Term[][] queries = {
				new Term[] { instr, X, i1 },
				new Term[] { X, X, X, invoke },
				new Term[] { instr, m_2 }
		};
		
		trie = new Trie();
		fill(trie);
		
		boolean error = false;
		for (Term[] query : queries) {
			if (!testQueryTermIterator(query, SHALLOW)) {
				error = true;
				System.err.println("Query " + Arrays.toString(query) + " failed (recursive, complex)");
			}
		}
		
		assertTrue(!error); // yes, ANY of the three queries may be wrong...
	}
	
	/**
	 * Tests the {@link Trie.SimpleTermIterator} with the current configuration and 
	 * the specified <tt>filename</tt>.
	 * 
	 * @return <tt>true</tt> if the iterator works correct.
	 */
	private boolean testTrieTermIterator() {
		
		Deque<Term> expecteds = new LinkedList<Term>();
		for (Term fact : FACTS.getFacts()) {
			expecteds.push(fact);
		}
		
		Trie root = new Trie();
		fill(root);
		
		Deque<Term> actuals = new LinkedList<Term>();
		Iterator<Term> iter = root.iterator();
		while (iter.hasNext()) {
			actuals.push(iter.next());
		}
		
		if (!same(expecteds, actuals)) {
			//printTermCollections(expecteds, actuals);
			//new TrieInspector().print(root, "c:/users/leaf/desktop/" + System.currentTimeMillis() + ".test-failed.gv", true);
			return false;
		} else {
			return true;
		}
	}
	
	private boolean testQueryTermIterator(Term[] query, TermFlattener flattener) {
		
		TermFilter filter = new TermFilter(query, flattener);
		
		Deque<Term> expecteds = new LinkedList<Term>();
		for (Term fact : FACTS.getFacts()) {
			//boolean allow = false;
			if (filter.allow(fact)) {
				expecteds.push(fact);
				//allow = true;
			}
			//System.out.println("Query " + Arrays.toString(query) + "," + (allow ? "" : " NOT") + " allowing " + Utils.termToString(fact));
		}
		
		Deque<Term> actuals = new LinkedList<Term>();
		Iterator<Term> iter = trie.iterator(query);
		while (iter.hasNext()) {
			actuals.push(iter.next());
		}
		
		if (!same(expecteds, actuals)) {
			printTermCollections(expecteds, actuals);
			//new TrieInspector().print(root, "c:/users/leaf/desktop/" + System.currentTimeMillis() + ".test-failed.gv", true);
			return false;
		} else {
			return true;
		}
	}
	
	/**
	 * Prints the to specified {@link Term} sets to the console.
	 * 
	 * @param s0 The first set.
	 * @param s1 The second set.
	 */
	@SuppressWarnings("all")
	private void printTermCollections(Collection<Term> s0, Collection<Term> s1) {
		System.out.println("\n\nSet 1 (" + s0.size() + " elements)");
		for (Term term : s0) {
			System.out.println(Utils.termToString(term));
		}

		System.out.println("\nSet 2 (" + s1.size() + " elements)");
		for (Term term : s1) {
			System.out.println(Utils.termToString(term));
		}
		
		if (s0.size() != s1.size()) {
			System.out.println("\nElements not in both sets:"); // actually not always correct...
			Collection<Term> bigger = (s0.size() > s1.size() ? s0 : s1);
			Collection<Term> smaller = (bigger == s0 ? s1 : s0);
			for (Term term : bigger) {
				if (!smaller.contains(term)) {
					System.out.println(Utils.termToString(term));
				}
			}
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
	private boolean same(Collection<?> coll0, Collection<?> coll1) {
		if (coll0.size() == coll1.size() && coll0.containsAll(coll1)) { // may take very long
			return true;
		} else {
			return false;
		}
	}
	
	private void fill(Trie root) {
		Stopwatch sw = new Stopwatch();
		List<Term> facts = FACTS.getFacts();
		for (Term fact : facts) {
			root.insert(fact);
		}
		String flattener = (Trie.getTermFlattener() instanceof ShallowTermFlattener) ? "shallow" : "recursive";
		String inserter = (Trie.getTermInserter() instanceof SimpleTermInserter) ? "simple" : "complex";
		sw.printElapsed("Filling a " + flattener + ", " + inserter + " trie with " + facts.size() + " facts");
	}
	
	/**
	 * A class for filtering terms that match a query. (As for use with plain 
	 * lists, for example.)
	 * 
	 * @author David Sullivan
	 * @version 0.1, 10/6/2010
	 */
	private class TermFilter {
		
		private final Term[] query;
		private final TermFlattener flattener;
		
		/**
		 * Creates a new {@link TermFilter} with the specified query and a 
		 * {@link TermFlattener}. The latter must be the same as the one that 
		 * was used by creating the query.
		 * 
		 * @param query The query.
		 * @param flattener The term flattener.
		 */
		public TermFilter(Term[] query, TermFlattener flattener) {
			this.query = query;
			this.flattener = flattener;
		}
		
		/**
		 * Checks wether the specified {@link Term} is <i>allowed</i>, i.e., it
		 * satisfies the query.
		 * 
		 * @param term The term to check.
		 * @return <tt>true</tt> if the terms matches the query of this instance.
		 */
		public boolean allow(Term term) {
			Term[] flattened = flattener.flattenInsertion(term);
			return query.length == Matcher.match(query, flattened);
		}
	}
}
