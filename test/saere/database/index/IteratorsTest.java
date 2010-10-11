package saere.database.index;

import java.io.File;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import junit.framework.TestCase;

import org.junit.Test;

import saere.Term;
import saere.Variable;
import saere.database.DatabaseTermFactory;
import saere.database.DatabaseTest;
import saere.database.Factbase;
import saere.database.Utils;

/**
 * Test class for the various iterator classes. Note that these tests assume 
 * that associated classes like {@link TermFlattener} or {@link TermInserter} 
 * implementations work properly.<br />
 * <br />
 * If tests fail because of an {@link StackOverflowError}, try raising the 
 * thread stack size (e.g., pass <tt>-Xss256M</tt> as JVM argument).
 * 
 * @author David Sullivan
 * @version 0.2, 10/4/2010
 */
/*
 * This class also contains some methods/code that are helpful for debbuging...
 */
public class IteratorsTest extends TestCase {
	
	private static final TermFlattener SHALLOW = new ShallowTermFlattener();
	private static final TermFlattener RECURSIVE = new RecursiveTermFlattener();
	private static final TermInserter SIMPLE = new SimpleTermInserter();
	private static final TermInserter COMPLEX = new ComplexTermInserter();
	
	private static final Factbase FACTS = Factbase.getInstance();
	
	/**
	 * The test file for this {@link TestCase}. It must be a JAR, ZIP or CLASS 
	 * file. The larger the file, the longer will the tests of this 
	 * {@link TestCase} take.
	 */
	private String testFile = DatabaseTest.DATA_PATH + File.separator + "HelloWorld.class";
	
	/**
	 * Sets the {@link #testFile}.
	 * 
	 * @param testFile
	 * @see #testFile
	 */
	public void testFile(String testFile) {
		this.testFile = testFile;
	}
	
	/**
	 * Gets the {@link #testFile}.
	 * 
	 * @return The test file for this instance.
	 * @see #testFile
	 */
	public String testFile() {
		return testFile;
	}
	
	// XXX This is run before every test!
	@Override
	protected void setUp() throws Exception {
		super.setUp();
		System.out.print("Reading test file " + testFile + "... ");
		FACTS.drop();
		FACTS.read(testFile);
		System.out.println("DONE");
	}
	
	/**
	 * Test for the {@link TrieNodeIterator}. (It's relatively small since all 
	 * nodes of a {@link Trie} have to be known.)
	 */
	@Test
	public void testTrieNodeIterator() {
		Trie.setTermFlattener(SHALLOW);
		Trie.setTermInserter(SIMPLE);
		
		Term f = DatabaseTermFactory.makeStringAtom("f");
		Term fa = DatabaseTermFactory.makeCompoundTerm("f", new Term[] {
				DatabaseTermFactory.makeStringAtom("a")
		});
		Term fab = DatabaseTermFactory.makeCompoundTerm("f", new Term[] {
				DatabaseTermFactory.makeStringAtom("a"),
				DatabaseTermFactory.makeStringAtom("b")
		});
		Term fabc = DatabaseTermFactory.makeCompoundTerm("f", new Term[] {
				DatabaseTermFactory.makeStringAtom("a"),
				DatabaseTermFactory.makeStringAtom("b"),
				DatabaseTermFactory.makeStringAtom("c")
		});
		Term fabd = DatabaseTermFactory.makeCompoundTerm("f", new Term[] {
				DatabaseTermFactory.makeStringAtom("a"),
				DatabaseTermFactory.makeStringAtom("b"),
				DatabaseTermFactory.makeStringAtom("d")
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
		
		assertTrue(expecteds.equals(actuals));
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
		
		Term instr = DatabaseTermFactory.makeStringAtom("instr");
		Term m_2 = DatabaseTermFactory.makeStringAtom("m_2");
		Term i1 = DatabaseTermFactory.makeIntegerAtom(1);
		Term invoke = DatabaseTermFactory.makeStringAtom("invoke");
		Term X = new Variable();
		
		Term[][] queries = {
				new Term[] { instr, X, i1 },
				new Term[] { X, X, invoke },
				new Term[] { instr, m_2 }
		};
		
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
		
		Term instr = DatabaseTermFactory.makeStringAtom("instr");
		Term m_2 = DatabaseTermFactory.makeStringAtom("m_2");
		Term i1 = DatabaseTermFactory.makeIntegerAtom(1);
		Term invoke = DatabaseTermFactory.makeStringAtom("invoke");
		Term X = new Variable();
		
		Term[][] queries = {
				new Term[] { instr, X, i1 },
				new Term[] { X, X, invoke },
				new Term[] { instr, m_2 }
		};
		
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
		
		Term instr = DatabaseTermFactory.makeStringAtom("instr");
		Term m_2 = DatabaseTermFactory.makeStringAtom("m_2");
		Term i1 = DatabaseTermFactory.makeIntegerAtom(1);
		Term invoke = DatabaseTermFactory.makeStringAtom("invoke");
		Term X = new Variable();
		
		Term[][] queries = {
				new Term[] { instr, X, i1 },
				new Term[] { X, X, X, invoke },
				new Term[] { instr, m_2 }
		};
		
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
		
		Term instr = DatabaseTermFactory.makeStringAtom("instr");
		Term m_2 = DatabaseTermFactory.makeStringAtom("m_2");
		Term i1 = DatabaseTermFactory.makeIntegerAtom(1);
		Term invoke = DatabaseTermFactory.makeStringAtom("invoke");
		Term X = new Variable();
		
		Term[][] queries = {
				new Term[] { instr, X, i1 },
				new Term[] { X, X, X, invoke },
				new Term[] { instr, m_2 }
		};
		
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
		
		List<Term> expecteds = new LinkedList<Term>();
		for (Term fact : FACTS.getFacts()) {
			expecteds.add(fact);
		}
		
		Trie root = new Trie();
		for (Term fact : FACTS.getFacts()) {
			root.insert(fact);
		}
		
		List<Term> actuals = new LinkedList<Term>();
		Iterator<Term> iter = root.iterator();
		while (iter.hasNext()) {
			actuals.add(iter.next());
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
		
		List<Term> expecteds = new LinkedList<Term>();
		for (Term fact : FACTS.getFacts()) {
			//boolean allow = false;
			if (filter.allow(fact)) {
				expecteds.add(fact);
				//allow = true;
			}
			//System.out.println("Query " + Arrays.toString(query) + "," + (allow ? "" : " NOT") + " allowing " + Utils.termToString(fact));
		}
		
		Trie root = new Trie();
		for (Term fact : FACTS.getFacts()) {
			root.insert(fact);
		}
		
		List<Term> actuals = new LinkedList<Term>();
		Iterator<Term> iter = root.iterator(query);
		while (iter.hasNext()) {
			actuals.add(iter.next());
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
	 * Checks wether the two specified lists are the <i>same</i>. Two lists 
	 * are the same if they have the same size and if one list contains all 
	 * elements of the other (but possibly in different order).
	 * 
	 * @param list0 The first list.
	 * @param list1 The second list.
	 * @return <tt>true</tt> if the two lists are the same.
	 */
	@SuppressWarnings("all")
	private boolean same(List<Term> list0, List<Term> list1) {
		if (list0.size() == list1.size() && list0.containsAll(list1)) {
			return true;
		} else {
			return false;
		}
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
			Term[] flattened = flattener.flatten(term);
			return query.length == Matcher.match(query, flattened);
		}
	}
}
