package saere.database.index.map;

import java.io.File;
import java.util.Arrays;
import java.util.Deque;
import java.util.Iterator;
import java.util.LinkedList;

import org.junit.Test;
import org.junit.BeforeClass;

import saere.IntegerAtom;
import saere.StringAtom;
import saere.Term;
import saere.Variable;
import saere.database.DatabaseTest;
import saere.database.Factbase;
import saere.database.Stopwatch;
import saere.database.TermFilter;
import saere.database.index.ShallowFlattener;
import saere.database.profiling.InsertionLogger;

public class MapTest {
	
	//private static final String TEST_FILE = DatabaseTest.DATA_PATH + File.separator + "opal-0.5.0.jar";
	private static final String TEST_FILE = "../test/classfiles/Tomcat-6.0.20.zip";
	private static final Factbase FACTS = Factbase.getInstance();
	private static final ShallowFlattener FLATTENER = new ShallowFlattener();
	private static final int TEST_RUNS = 5;
	
	private static Term[][] queries;
	
	@BeforeClass
	public static void initialize() {
		
		// Read facts with BAT
		Stopwatch sw = new Stopwatch();
		FACTS.read(TEST_FILE);
		sw.printElapsed("Readings the facts");
		
		// Initialize queries
		Term instr = StringAtom.StringAtom("instr");
		Term m_2 = StringAtom.StringAtom("m_2");
		Term m_10000 = StringAtom.StringAtom("m_10000");
		Term X = new Variable(); // We need only one as we don't really unify
		Term i0 = IntegerAtom.IntegerAtom(0);
		Term invoke = StringAtom.StringAtom("invoke");
		
		queries = new Term[][] {
			new Term[] { instr }, 				// all instructions
			new Term[] { instr, m_2 }, 			// all instructiosn of method 2
			new Term[] { instr, m_10000 },	 	// all instructiosn of method 10000
			new Term[] { instr, X, i0 }, 		// all first instructions of each method
			new Term[] { instr, X, X, invoke } 	// all invoke instructions
		};
	}
	
	@Test
	public void testInsertions() {
		System.out.println("Testing insertions...");
		
		MapTrieBuilder builder = new MapTrieBuilder();
		for (int i = 0; i < TEST_RUNS; i++) {
			System.out.println("Run " + (i + 1) + ":");
			
			// Log the last run with DB only
			if (i < TEST_RUNS - 1) {
				InsertionLogger.setActive(false);
			} else {
				InsertionLogger.setActive(true);
			}
			
			// Insert into a trie
			MapTrie root = new MapTrie();			
			Stopwatch sw = new Stopwatch();
			for (Term fact : FACTS.getFacts()) {
				builder.insert(fact, root);
			}
			sw.printElapsed("Inserting the terms into a trie");
			
			// Insert into a list as comparison
			Deque<Term> list = new LinkedList<Term>();
			sw = new Stopwatch();
			for (Term fact : FACTS.getFacts()) {
				list.push(fact);
			}
			sw.printElapsed("Inserting the terms into a list");
			
			System.out.println("\n");
			
			root = null;
			list = null;
			System.gc();
		}
	}
	
	@Test
	public void testIterator() {
		System.out.println("Testing iterator...");
		
		InsertionLogger.setActive(false);
		
		MapTrieBuilder builder = new MapTrieBuilder();
		for (int i = 0; i < TEST_RUNS; i++) {
			System.out.println("Run " + (i + 1) + ":");
			
			// Insert into a trie
			MapTrie root = new MapTrie();			
			for (Term fact : FACTS.getFacts()) {
				builder.insert(fact, root);
			}
			
			// Insert into a list as comparison
			Deque<Term> list = new LinkedList<Term>();
			for (Term fact : FACTS.getFacts()) {
				list.push(fact);
			}
			
			// Iterate over the trie
			Iterator<Term> iter = builder.iterator(root);
			int termCounter = 0;
			Stopwatch sw = new Stopwatch();
			while (iter.hasNext()) {
				iter.next();
				termCounter++;
			}
			sw.printElapsed("Iterating over a trie with " + termCounter + " terms");
			
			// Iterate over the list
			iter = list.iterator();
			termCounter = 0;
			sw = new Stopwatch();
			while (iter.hasNext()) {
				iter.next();
				termCounter++;
			}
			sw.printElapsed("Iterating over a list with " + termCounter + " terms");
			
			System.out.println("\n");
			
			root = null;
			list = null;
			System.gc();
		}
	}
	
	@Test
	public void testQueries() {
		System.out.println("Testing queries...");
		
		InsertionLogger.setActive(false);
		
		MapTrieBuilder builder = new MapTrieBuilder();
		for (int i = 0; i < TEST_RUNS; i++) {
			System.out.println("Run " + (i + 1) + ":");
			
			// Insert into a trie
			MapTrie root = new MapTrie();			
			for (Term fact : FACTS.getFacts()) {
				builder.insert(fact, root);
			}
			
			// Insert into a list as comparison
			Deque<Term> list = new LinkedList<Term>();
			for (Term fact : FACTS.getFacts()) {
				list.push(fact);
			}
			
			// Do the queries
			for (Term[] query : queries) {
				System.out.println("Querying for " + Arrays.toString(query));
				
				// Query over the trie
				Iterator<Term> iter = builder.iterator(root, query);
				int termCounter = 0;
				Stopwatch sw = new Stopwatch();
				while (iter.hasNext()) {
					iter.next();
					termCounter++;
				}
				sw.printElapsed("Iterating over a trie with " + termCounter + " results");
				
				// Iterate over the list
				TermFilter filter = new TermFilter(query, FLATTENER);
				iter = list.iterator();
				termCounter = 0;
				sw = new Stopwatch();
				while (iter.hasNext()) {
					if (filter.allow(iter.next()))
						termCounter++;
				}
				sw.printElapsed("Iterating over a list filtering " + termCounter + " results");
			}
			
			System.out.println("\n");
			
			root = null;
			list = null;
			System.gc();
		}
	}
}
