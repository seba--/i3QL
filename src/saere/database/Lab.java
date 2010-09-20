package saere.database;

import static saere.database.Utils.query;
import static saere.database.Utils.queryNoPrint;
import static saere.database.Utils.termToString;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

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
		//demo2();
		//demo2b();
		printStatistics();
		//demo4();
		//trieNodeIteratorTest();
		//smallFlatteningTests();
		//bigFlatteningTests();
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
		
		/*
		List<Term> facts = Factbase.getInstance().getFacts();
		StringAtom instrFunct = DatabaseTermFactory.makeStringAtom("instr");
		for (Term fact : facts) {
			if (fact.functor().sameAs(instrFunct))
				System.out.println(termToString(fact));
		}
		System.out.println();
		*/
		
		Trie instr = ((Database_Trie) TDB).getPredicateSubtrie(DatabaseTermFactory.makeStringAtom("instr"));
		Term t0 = new Variable();
		//t0 = StringAtom.StringAtom("m_2");
		Term t1 = new Variable();
		//t1 = IntegerAtom.IntegerAtom(0);
		Term t2 = new Variable();
		t2 = StringAtom.StringAtom("return");
		Term t3 = StringAtom.StringAtom("reference");
		Iterator<Term> iterator = instr.iterator(t0, t1, t2, t3);
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
	private static void printStatistics() {
		Trie.setTermFlattener(new ShallowTermFlattener());
		for (String file : files) {
			dropDBs();
			AbstractTermFlattener shallow = new ShallowTermFlattener();
			shallow.setMaxLength(5);
			((Database_Trie) TDB).getRoot().setTermFlattener(shallow);
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
	
	// we test only the nodes with terms
	private static void trieNodeIteratorTest() {
		fillDBs(files[4]);
		
		List<Term> facts = Factbase.getInstance().getFacts();
		for (Term fact : facts) {
			System.out.println(termToString(fact));
		}
		
		System.out.println();
		
		Trie instr = ((Database_Trie) TDB).getPredicateSubtrie(DatabaseTermFactory.makeStringAtom("instr"));
		Iterator<Trie> iterator = instr.nodeIterator();
		while (iterator.hasNext()) {
			Trie next = iterator.next();
			if (next.getTerm() != null) { // XXX only the first term!
				System.out.println(termToString(next.getTerm()));
			}
		}
	}
	
	private static void smallFlatteningTests() {
		
		// f(a, b, c)
		Term fabc0 = DatabaseTermFactory.makeCompoundTerm("f", new Term[] {
				DatabaseTermFactory.makeStringAtom("a"),
				DatabaseTermFactory.makeStringAtom("b"),
				DatabaseTermFactory.makeStringAtom("c"),
		});
		
		// f(a, b(c))
		Term fabc1 = DatabaseTermFactory.makeCompoundTerm("f", new Term[] {
				DatabaseTermFactory.makeStringAtom("a"),
				DatabaseTermFactory.makeCompoundTerm("b", new Term[] {
						DatabaseTermFactory.makeStringAtom("c")	
				})
		});
		
		TermFlattener shallow = new ShallowTermFlattener();
		TermFlattener recursive = new RecursiveTermFlattener();
		
		// shallow flattening
		System.out.println("Shallow Flattening");
		Term[] terms0 = shallow.flatten(fabc0);
		System.out.print(termToString(fabc0) + " - > ");
		for (Term term : terms0) {
			System.out.print(termToString(term) + " ");
		}
		System.out.println();
		Term[] terms1 = shallow.flatten(fabc1);
		System.out.print(termToString(fabc1) + " - > ");
		for (Term term : terms1) {
			System.out.print(termToString(term) + " ");
		}
		
		System.out.println("\n");
		
		// recursive flattening
		System.out.println("Recursive Flattening");
		terms0 = recursive.flatten(fabc0);
		System.out.print(termToString(fabc0) + " - > ");
		for (Term term : terms0) {
			System.out.print(termToString(term) + " ");
		}
		System.out.println();
		terms1 = recursive.flatten(fabc1);
		System.out.print(termToString(fabc1) + " - > ");
		for (Term term : terms1) {
			System.out.print(termToString(term) + " ");
		}
		
		// generate some output for graphviz
		TrieInspector inspector = new TrieInspector();
		
		Trie.setTermFlattener(shallow);
		Trie root = new Trie();
		root.add(fabc0);
		root.add(fabc1);
		inspector.print(root, "c:/users/leaf/desktop/shallow_trie.gv", false);
		
		Trie.setTermFlattener(recursive);
		root = new Trie();
		root.add(fabc0);
		root.add(fabc1);
		inspector.print(root, "c:/users/leaf/desktop/recursive_trie.gv", false);
	}
	
	private static void bigFlatteningTests() {
		AbstractTermFlattener shallow = new ShallowTermFlattener();
		AbstractTermFlattener recursive = new RecursiveTermFlattener();
		TrieInspector inspector = new TrieInspector();
		
		shallow.setMaxLength(5);
		Trie.setTermFlattener(shallow);
		fillDBs(files[4]);
		Trie root = ((Database_Trie) TDB).getRoot();
		inspector.print(root, "c:/users/leaf/desktop/shallow_trie.gv", false);
		
		dropDBs();
		recursive.setMaxLength(5);
		Trie.setTermFlattener(recursive);
		fillDBs(files[4]);
		root = ((Database_Trie) TDB).getRoot();
		inspector.print(root, "c:/users/leaf/desktop/recursive_trie.gv", false);
	}
}
