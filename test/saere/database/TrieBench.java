package saere.database;

import static saere.database.DatabaseTermFactory.ct;
import static saere.database.DatabaseTermFactory.sa;
import static saere.database.DatabaseTermFactory.v;

import java.io.File;
import java.util.HashMap;
import java.util.LinkedList;

import org.junit.BeforeClass;
import org.junit.Test;

import saere.State;
import saere.StringAtom;
import saere.Term;
import saere.database.index.FullFlattener;
import saere.database.index.ShallowFlattener;
import saere.database.index.SimpleTrieBuilder;
import saere.database.index.Trie;
import saere.database.index.TrieBuilder;
import saere.database.index.reference.ReferenceDatabase;
import saere.database.predicate.DatabasePredicate;

public final class TrieBench {
	
	private static final int MAP_THRESHOLD = 120;
	//private static final String TEST_FILE = DatabaseTest.GLOBAL_TEST_FILE;
	//private static final String TEST_FILE = "../test/classfiles/Tomcat-6.0.20.zip";
	private static final String TEST_FILE = DatabaseTest.DATA_PATH + File.separator + "opal-0.5.0.jar";
	
	@BeforeClass
	public static void initialize() {
		Factbase.getInstance().read(TEST_FILE);
	}
	
	@Test
	public void testInstr3ArgumentIndexing1() {
		for (int i = 0; i < 3; i++) {
			System.out.println("\nRun " + (i + 1));
			
			TrieBuilder shallowBuilder = new SimpleTrieBuilder(new ShallowFlattener(), MAP_THRESHOLD);
			Trie shallowRoot = Trie.newRoot();
			
			TrieBuilder fullBuilder = new SimpleTrieBuilder(new FullFlattener(), MAP_THRESHOLD);
			Trie fullRoot = Trie.newRoot();
			
			HashMap<Term, Term> functorIndex = new HashMap<Term, Term>();
			HashMap<Term, Term> completeIndex = new HashMap<Term, Term>();
			
			StringAtom instr = StringAtom.StringAtom("instr");
			LinkedList<Term> instr3Arg2s = new LinkedList<Term>();
			for (Term fact : Factbase.getInstance().getFacts()) {
				if (fact.functor().sameAs(instr)) {
					instr3Arg2s.push(fact.arg(2));
				}
			}
			//Factbase.getInstance().drop();
			
			Stopwatch sw = new Stopwatch();
			for (Term arg2 : instr3Arg2s) {
				shallowBuilder.insert(arg2, shallowRoot);
			}
			sw.printElapsedAndReset("Filling the shallow trie index");
			for (Term arg2 : instr3Arg2s) {
				fullBuilder.insert(arg2, fullRoot);
			}
			sw.printElapsedAndReset("Filling the full trie index");
			for (Term arg2 : instr3Arg2s) {
				functorIndex.put(arg2.functor(), arg2);
			}
			sw.printElapsedAndReset("Filling the functor index");
			for (Term arg2 : instr3Arg2s) {
				completeIndex.put(arg2, arg2);
			}
			sw.printElapsedAndReset("Filling the complete index");
			
			/*
			Iterator<Term> shallowIter = shallowBuilder.iterator(shallowRoot);
			while (shallowIter.hasNext()) {
				System.out.println(shallowIter.next());
			}
			*/
			/*
			Iterator<Term> fullIter = fullBuilder.iterator(fullRoot);
			LinkedList<Term> arg2s = new LinkedList<Term>();
			while (fullIter.hasNext()) {
				arg2s.push(fullIter.next());
			}
			FactsPrinter.print(arg2s, "c:/users/leaf/desktop/instr3arg2.txt");
			*/
		}
	}
	
	@Test
	public void testInstr3ArgumentIndexing2() {
		for (int i = 0; i < 3; i++) {
			System.out.println("\nRun " + (i + 1));
			
			TrieBuilder shallowBuilder = new SimpleTrieBuilder(new ShallowFlattener(), MAP_THRESHOLD);
			TrieBuilder fullBuilder = new SimpleTrieBuilder(new FullFlattener(), MAP_THRESHOLD);
			
			Database shallowDatabase = new TrieDatabase(shallowBuilder);
			Database fullDatabase = new TrieDatabase(fullBuilder);
			Database referenceDatabase = new ReferenceDatabase();
			((ReferenceDatabase) referenceDatabase).allowDuplicates(false);
			
			StringAtom instr = StringAtom.StringAtom("instr");
			LinkedList<Term> instr3Arg2s = new LinkedList<Term>();
			for (Term fact : Factbase.getInstance().getFacts()) {
				if (fact.functor().sameAs(instr)) {
					instr3Arg2s.push(fact.arg(2));
				}
			}
			//Factbase.getInstance().drop();
			
			Stopwatch sw = new Stopwatch();
			for (Term arg2 : instr3Arg2s) {
				shallowDatabase.add(arg2);
			}
			sw.printElapsedAndReset("Filling the shallow database");
			for (Term arg2 : instr3Arg2s) {
				fullDatabase.add(arg2);
			}
			sw.printElapsedAndReset("Filling the full database");
			for (Term arg2 : instr3Arg2s) {
				referenceDatabase.add(arg2);
			}
			sw.printElapsed("Filling reference database");
			
			for (Term query : Queries.ALL) {
				State state = query.manifestState();
				
				DatabasePredicate shallowPredicate = new DatabasePredicate(query.functor().toString(), query.arity(), shallowDatabase);
				DatabasePredicate fullPredicate = new DatabasePredicate(query.functor().toString(), query.arity(), fullDatabase);
				DatabasePredicate referencePredicate = new DatabasePredicate(query.functor().toString(), query.arity(), referenceDatabase);
				
				System.out.println("\nQuery " + query);
				
				System.out.print("Shallow: ");
				Utils.query(shallowPredicate, query);
				
				query.setState(state);
				
				System.out.print("Full: ");
				Utils.query(fullPredicate, query);
				
				query.setState(state);
				
				System.out.print("Reference: ");
				Utils.query(referencePredicate, query);
				
				query.setState(state);
				/*
				sw = new Stopwatch();
				Iterator<Term> iter = shallowDatabase.query(query);
				int c = 0;
				while (iter.hasNext()) {
					iter.next();
					c++;
				}
				sw.printElapsedAndReset("Finding " + c + " solutions with the shallow trie database");
				
				iter = fullDatabase.query(query);
				c = 0;
				while (iter.hasNext()) {
					iter.next();
					c++;
				}
				sw.printElapsedAndReset("Finding " + c + " solutions with the full trie database");
				
				iter = referenceDatabase.query(query);
				c = 0;
				while (iter.hasNext()) {
					iter.next();
					c++;
				}
				sw.printElapsedAndReset("Finding " + c + " solutions with the reference database");
				*/
			}
		}
	}
	
	private static class Queries {
		
		/** instanceof(class(de/tud/cs/st/bat/reader, X)). */
		public static final Term Q0 = ct("instanceof", ct("class", sa("de/tud/cs/st/bat/reader"), v()));
		
		/** checkcast(class(de/tud/cs/st/bat/instruction, X)) */
		public static final Term Q1 = ct("checkcast", ct("class", sa("de/tud/cs/st/bat/instruction"), v()));
		
		/** if_cmp(int, ne, X) */
		public static final Term Q2 = ct("if_cmp", sa("int"), sa("ne"), v());
		
		/** if_cmp(int, X, Y) */
		public static final Term Q3 = ct("if_cmp", sa("int"), v(), v());
		
		/** if_cmp(X, X, Y) */
		public static final Term Q4 = ct("if_cmp", v(), v(), v());
		
		/** if_cmp(X, ne, Y) */
		public static final Term Q5 = ct("if_cmp", v(), sa("ne"), v());
		
		/** new_object(class(de/tud/cs/st/bat/instruction, X)) */
		public static final Term Q6 = ct("new_object", ct("class", sa("de/tud/cs/st/bat/instruction"), v()));
		
		public static final Term[] ALL = { Q0, Q1, Q2, Q3, Q4, Q5, Q6 };
	}
}
