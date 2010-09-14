package saere.database;

import static saere.database.Utils.queryNoPrint;
import static saere.database.Utils.query;
import static saere.database.Utils.termToString;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import saere.IntegerAtom;
import saere.StringAtom;
import saere.Term;
import saere.Variable;

public class Lab {
	
	private static final boolean PRINT = false;
	
	private static final Database TDB = Database_Trie.getInstance();
	private static final Database SDB = Database_Default.getInstance();
	
	private static final String[] files = {
		/* 0 Large:			*/	//"../test/classfiles/org.eclipse.jdt.ui_3.5.0.v20090604.zip",
		/* 1 Medium:		*/	"../test/classfiles/Tomcat-6.0.20.zip",
		/* 2 Small:			*/	"../bat/build/opal-0.5.0.jar",
		/* 3 Small:			*/	"../test/classfiles/shiftone-jrat.jar",
		/* 4 Very small:	*/	"../test/classfiles/MMC.jar",
		/* 5 Tiny:			*/	"../test/classfiles/HelloWorld.class"
	};
	
	public static void main(String[] args) {		
		//demo1();
		demo2();
		//demo2b();
		//demo3();
		//demo4();
	}
	
	private static void fillDBs(String filename) {
		System.out.println("=== " + filename + " ===");
		Stopwatch sw = new Stopwatch();
		Factbase.getInstance().read(filename);
		sw.printElapsedAndReset("Creating the facts");
		TDB.fill();
		sw.printElapsedAndReset("Filling the trie database");
		SDB.fill();
		sw.printElapsedAndReset("Filling the default database");
	}
	
	private static void dropDBs() {
		Factbase.getInstance().drop();
		TDB.drop();
		SDB.drop();
	}
	
	private static void demo1() {
		fillDBs("?");
		
		Instr3_Trie instr3p_Trie = new Instr3_Trie(); // with trie
		Instr3_Default instr3p_Default = new Instr3_Default(); // with list
		
		Variable x = new Variable();
		Variable y = new Variable();
		Variable z = new Variable();
		
		// instr(X, Y, Z).
		System.out.print("With Tries: ");
		q(instr3p_Trie, x, y, z);

		System.out.print("Without Tries: ");
		q(instr3p_Default, x, y, z);
		
		// instr(X, 1, Z).
		System.out.print("With Tries: ");
		q(instr3p_Trie, x, DatabaseTermFactory.makeIntegerAtom(1), z);
		
		System.out.print("Without Tries: ");
		q(instr3p_Default, x, DatabaseTermFactory.makeIntegerAtom(1), z);
		
		// instr(m_1, Y, Z).
		System.out.print("With Tries: ");
		q(instr3p_Trie, DatabaseTermFactory.makeStringAtom("m_20"), y, z);
		
		System.out.print("Without Tries: ");
		q(instr3p_Default, DatabaseTermFactory.makeStringAtom("m_20"), y, z);
	}
	
	// trie iterator test
	private static void demo2() {
		fillDBs(files[3]);
		
		List<Term> facts = Factbase.getInstance().getFacts();
		StringAtom instrFunct = DatabaseTermFactory.makeStringAtom("instr");
		for (Term fact : facts) {
			if (fact.functor().sameAs(instrFunct))
				System.out.println(termToString(fact));
		}
		System.out.println();
		
		Trie instr = ((Database_Trie) TDB).getPredicateSubtrie(DatabaseTermFactory.makeStringAtom("instr"));
		Term t0 = new Variable();
		//t0 = StringAtom.StringAtom("m_2");
		Term t1 = new Variable();
		t1 = IntegerAtom.IntegerAtom(0);
		Term t2 = new Variable();
		//t2 = StringAtom.StringAtom("return");
		Iterator<Term> iterator = instr.iterator(t0, t1, t2);
		while (iterator.hasNext()) {
			System.out.println(termToString(iterator.next()));
		}
	}
	
	// simple trie iterator test
	private static void demo2b() {
		fillDBs(files[4]);
		
		List<Term> facts = Factbase.getInstance().getFacts();
		StringAtom instrFunct = DatabaseTermFactory.makeStringAtom("instr");
		for (Term fact : facts) {
			if (fact.functor().sameAs(instrFunct))
				System.out.println(termToString(fact));
		}
		System.out.println();
		
		Trie instr = ((Database_Trie) TDB).getPredicateSubtrie(DatabaseTermFactory.makeStringAtom("instr"));
		Iterator<Term> iter = instr.iterator();
		while (iter.hasNext()) {
			System.out.println(termToString(iter.next()));
		}
	}
	
	// print statistics
	private static void demo3() {
		for (String file : files) {
			dropDBs();
			fillDBs(file);
			List<Term> facts = Factbase.getInstance().getFacts();
			List<Term> instrs = new LinkedList<Term>();
			StringAtom instrFunctor = DatabaseTermFactory.makeStringAtom("instr");
			for (Term fact : facts) {
				if (fact.functor().sameAs(instrFunctor)) {
					instrs.add(fact);
				}
			}

			Trie instr = ((Database_Trie) TDB).getPredicateSubtrie(DatabaseTermFactory.makeStringAtom("instr"));
			Iterator<Term> iterator = instr.iterator();

			// How much slower is the iterator compared to a list?
			// (One run for each program.)

			/*
			 * "../test/classfiles/HelloWorld.class"
			 * 
			 * Reading the facts took 495ms Iteration over the trie with 7 terms
			 * took 0ms Iteration over the list with 7 terms took 0ms
			 */

			/*
			 * "../test/classfiles/MMC.jar"
			 * 
			 * Reading the facts took 4029ms Iteration over the trie with 14156
			 * terms took 25ms Iteration over the list with 14156 terms took 1ms
			 */

			/*
			 * "../test/classfiles/shiftone-jrat.jar"
			 * 
			 * Reading the facts took 23097ms Iteration over the trie with
			 * 161447 terms took 31ms Iteration over the list with 161447 terms
			 * took 9ms
			 */

			/*
			 * "../bat/build/opal-0.5.0.jar"
			 * 
			 * Reading the facts took 10564ms Iteration over the trie with 59412
			 * terms took 17ms Iteration over the list with 59412 terms took 7ms
			 */

			/*
			 * "../test/classfiles/Tomcat-6.0.20.zip"
			 * 
			 * Reading the facts took 55239ms Iteration over the trie with
			 * 395420 terms took 40ms Iteration over the list with 395420 terms
			 * took 12ms
			 */

			int counter = 0;
			Stopwatch sw = new Stopwatch();
			while (iterator.hasNext()) {
				iterator.next();
				counter++;
			}
			sw.printElapsedAndReset("Iteration over the trie with " + counter + " terms");
			counter = 0;
			for (Term term : instrs) {
				counter++; // not necessary, but iterator requires this
			}
			sw.printElapsedAndReset("Iteration over the list with " + counter + " terms");
			
			TrieInspector inspector = new TrieInspector();
			inspector.inspect(((Database_Trie) TDB).getRoot());
			inspector.printStats();
			System.out.println();
		}
	}
	
	// create trie gv
	private static void demo4() {
		fillDBs(files[4]);
		TrieInspector inspector = new TrieInspector();
		inspector.print(((Database_Trie) TDB).getRoot(), "C:/Users/Leaf/Desktop/trie.gv", true);
	}
	
	private static void q(DatabasePredicate p, Term ... terms) {
		if (PRINT) {
			query(p, terms);
		} else {
			queryNoPrint(p, terms);
		}	
	}
}
