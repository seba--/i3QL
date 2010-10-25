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

import saere.Atom;
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
	private static final TrieBuilder<Atom> SIMPLE = new SimpleTrieBuilder();
	private static final TrieBuilder<Atom[]> COMPLEX = new ComplexTrieBuilder();
	private static final Factbase FACTS = Factbase.getInstance();
	
	private Trie<Atom> simpleTrie;
	private Trie<Atom[]> complexTrie;
	
	public static void main(String[] args) {
		IteratorsTest test = new IteratorsTest();
		IteratorsTest.readFactsOnce();
		test.testComplexTrieTermIterator_Shallow();
	}
	
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
	
	private <T> void fill(TrieBuilder<T> builder, Trie<T> root) {
		Stopwatch sw = new Stopwatch();
		List<Term> facts = FACTS.getFacts();
		for (Term fact : facts) {
			builder.insert(fact, root);
		}
		
		sw.printElapsed("Filling a " + builder.toString() + " trie with " + facts.size() + " facts");
	}

	/**
	 * Test for the {@link TrieNodeIterator}. (It's relatively small since all 
	 * nodes of a {@link Trie} have to be known.)
	 */
	@Test
	public void testTrieNodeIterator_Simple() {
		SIMPLE.setTermFlattener(SHALLOW);
		Trie<Atom> root = new Trie<Atom>();
		
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
		
		List<Trie<Atom>> expecteds = new LinkedList<Trie<Atom>>();
		expecteds.add(root);
		expecteds.add(SIMPLE.insert(f, root));
		expecteds.add(SIMPLE.insert(fa, root));
		expecteds.add(SIMPLE.insert(fab, root));
		expecteds.add(SIMPLE.insert(fabc, root));
		expecteds.add(SIMPLE.insert(fabd, root));
		
		/*
		 * The trie should now look like this (and each node stores one term):
		 * 
		 * 			   ,--d
		 * root--f--a--b--c
		 */
		
		List<Trie<Atom>> actuals = new LinkedList<Trie<Atom>>();
		Iterator<Trie<Atom>> iter = SIMPLE.nodeIterator(root);
		while (iter.hasNext()) {
			actuals.add(iter.next());
		}
		
		assertTrue(same(expecteds, actuals));
	}
	
	@Test
	public void testTrieNodeIterator_Complex() {
		COMPLEX.setTermFlattener(SHALLOW);
		Trie<Atom[]> root = new Trie<Atom[]>();
		
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
		
		List<Trie<Atom[]>> expecteds = new LinkedList<Trie<Atom[]>>();
		expecteds.add(root);
		expecteds.add(COMPLEX.insert(f, root));
		expecteds.add(COMPLEX.insert(fa, root));
		expecteds.add(COMPLEX.insert(fab, root));
		expecteds.add(COMPLEX.insert(fabc, root));
		expecteds.add(COMPLEX.insert(fabd, root));
		
		/*
		 * The trie should now look like this (and each node stores one term):
		 * 
		 * 			   ,--d
		 * root--f--a--b--c
		 */
		
		List<Trie<Atom[]>> actuals = new LinkedList<Trie<Atom[]>>();
		Iterator<Trie<Atom[]>> iter = COMPLEX.nodeIterator(root);
		while (iter.hasNext()) {
			actuals.add(iter.next());
		}
		
		assertTrue(same(expecteds, actuals));
	}
	
	/**
	 * Test for the {@link TrieTermIterator} with a 
	 * {@link ShallowTermFlattener} and a {@link SimpleTrieBuilder}.
	 */
	@Test
	public void testTrieTermIterator_ShallowSimple() {
		SIMPLE.setTermFlattener(SHALLOW);
		assertTrue(testTrieTermIterator(SIMPLE));
	}
	
	/**
	 * Test for the {@link TrieTermIterator} with a 
	 * {@link RecursiveTermFlattener} and a {@link SimpleTrieBuilder}.
	 */
	@Test
	public void testTrieTermIterator_RecursiveSimple() {
		SIMPLE.setTermFlattener(RECURSIVE);
		assertTrue(testTrieTermIterator(SIMPLE));
	}
	
	/**
	 * Test for the {@link TrieTermIterator} with a 
	 * {@link ShallowTermFlattener} and a {@link ComplexTrieBuilder}.
	 */
	@Test
	public void testTrieTermIterator_ShallowComplex() {
		COMPLEX.setTermFlattener(SHALLOW);
		assertTrue(testTrieTermIterator(COMPLEX));
	}
	
	/**
	 * Test for the {@link TrieTermIterator} with a 
	 * {@link RecursiveTermFlattener} and a {@link ComplexTrieBuilder}.
	 */
	@Test
	public void testTrieTermIterator_RecursiveComplex() {
		COMPLEX.setTermFlattener(RECURSIVE);
		assertTrue(testTrieTermIterator(COMPLEX));
	}
	
	/**
	 * Tests the {@link Trie.SimpleTermIterator} with the current configuration and 
	 * the specified <tt>filename</tt>.
	 * 
	 * @return <tt>true</tt> if the iterator works correct.
	 */
	private <T> boolean testTrieTermIterator(TrieBuilder<T> builder) {
		
		Deque<Term> expecteds = new LinkedList<Term>();
		for (Term fact : FACTS.getFacts()) {
			expecteds.push(fact);
		}
		
		Trie<T> root = new Trie<T>();
		fill(builder, root);
		
		Deque<Term> actuals = new LinkedList<Term>();
		Iterator<Term> iter = builder.iterator(root);
		while (iter.hasNext()) {
			Term term = iter.next();
			//if (builder == COMPLEX) {
				//System.out.println("-->" + Utils.termToString(term));
			//}
			actuals.push(term);
		}
		
		if (!same(expecteds, actuals)) {
			//printTermCollections(expecteds, actuals);
			//new TrieInspector().print(root, builder, "c:/users/leaf/desktop/" + System.currentTimeMillis() + "-" + builder.toString() + ".test-failed.gv", true);
			return false;
		} else {
			return true;
		}
	}

	@Test
	public void testSimpleTrieTermIterator_Shallow() {
		SIMPLE.setTermFlattener(SHALLOW);
		
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
		
		simpleTrie = new Trie<Atom>();
		fill(SIMPLE, simpleTrie);
		
		boolean error = false;
		for (Term[] query : queries) {
			if (!testQueryTermIterator(simpleTrie, query, SIMPLE, SHALLOW)) {
				error = true;
				System.err.println("Query " + Arrays.toString(query) + " failed (shallow, simple)");
			}
		}
		
		assertTrue(!error); // yes, ANY of the three queries may be wrong...
	}
	
	@Test
	public void testSimpleTrieTermIterator_Recursive() {
		SIMPLE.setTermFlattener(RECURSIVE);
		
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
		
		simpleTrie = new Trie<Atom>();
		fill(SIMPLE, simpleTrie);
		
		boolean error = false;
		for (Term[] query : queries) {
			if (!testQueryTermIterator(simpleTrie, query, SIMPLE, SHALLOW)) {
				error = true;
				System.err.println("Query " + Arrays.toString(query) + " failed (recursive, simple)");
			}
		}
		
		assertTrue(!error); // yes, ANY of the three queries may be wrong...
	}
	
	@Test
	public void testComplexTrieTermIterator_Shallow() {
		COMPLEX.setTermFlattener(SHALLOW);
		
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
		
		complexTrie = new Trie<Atom[]>();
		fill(COMPLEX, complexTrie);
		
		boolean error = false;
		for (Term[] query : queries) {
			if (!testQueryTermIterator(complexTrie, query, COMPLEX, SHALLOW)) {
				error = true;
				System.err.println("Query " + Arrays.toString(query) + " failed (shallow, complex)");
			}
		}
		
		assertTrue(!error); // yes, ANY of the three queries may be wrong...
	}
	
	@Test
	public void testComplexTrieTermIterator_Recursive() {
		COMPLEX.setTermFlattener(RECURSIVE);
		
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
		
		complexTrie = new Trie<Atom[]>();
		fill(COMPLEX, complexTrie);
		
		boolean error = false;
		for (Term[] query : queries) {
			if (!testQueryTermIterator(complexTrie, query, COMPLEX, RECURSIVE)) {
				error = true;
				System.err.println("Query " + Arrays.toString(query) + " failed (recursive, complex)");
			}
		}
		
		assertTrue(!error); // yes, ANY of the three queries may be wrong...
	}
	
	private <T> boolean testQueryTermIterator(Trie<T> trie, Term[] query, TrieBuilder<T> builder, TermFlattener flattener) {
		
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
		Iterator<Term> iter = builder.iterator(trie, query);
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
	 * @param expected The expected set.
	 * @param actuals The actual set.
	 */
	@SuppressWarnings("all")
	private void printTermCollections(Collection<Term> expected, Collection<Term> actuals) {
		System.out.println("\n\nExpected set 1 (" + expected.size() + " elements)");
		for (Term term : expected) {
			System.out.println(Utils.termToString(term));
		}

		System.out.println("\nActual set 2 (" + actuals.size() + " elements)");
		for (Term term : actuals) {
			System.out.println(Utils.termToString(term));
		}
		
		if (expected.size() != actuals.size()) {
			System.out.println("\nElements not in the smaller set:"); // actually not always correct...
			Collection<Term> bigger = (expected.size() > actuals.size() ? expected : actuals);
			Collection<Term> smaller = (bigger == expected ? actuals : expected);
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
			Atom[] flattened = flattener.flattenForInsertion(term);
			return query.length == Matcher.match(flattened, query);
		}
	}
}
