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
import saere.database.predicate.ClassFile10;
import saere.database.predicate.DatabasePredicate;
import saere.database.predicate.Instr3;

public class Lab {
	
	private static boolean print = false;
	
	private static final Database TRIE_DB = TrieDatabase.getInstance();
	private static final Database LIST_DB = ListDatabase.getInstance();
	
	private static final String[] files = {		
		/* 0 Tiny:			*/	"../test/classfiles/HelloWorld.class",
		/* 1 Very small:	*/	"../test/classfiles/MMC.jar",
		/* 2 Small:			*/	"../test/classfiles/shiftone-jrat.jar",
		/* 3 Small:			*/	"../bat/build/opal-0.5.0.jar",
		/* 4 Medium:		*/	"../test/classfiles/Tomcat-6.0.20.zip"//,
		/* 5 (Too) Large:	*/	//"../test/classfiles/org.eclipse.jdt.ui_3.5.0.v20090604.zip"
	};
	
	public static void main(String[] args) {		
		compareTriesAndLists();
		//printStatistics();
		//createGvFile();
		//trieNodeIteratorTest();
		//smallFlatteningTests();
		//bigFlatteningTests();
		//labelTests();
		//complexInserterTest();
	}
	
	private static void fillDBs(String filename) {
		System.out.println("=== " + filename + " ===");
		Stopwatch sw = new Stopwatch();
		Factbase.getInstance().read(filename);
		sw.printElapsedAndReset("Creating the facts");
		TRIE_DB.fill();
		sw.printElapsedAndReset("Filling the trie database");
		LIST_DB.fill();
		sw.printElapsedAndReset("Filling the default database");
	}
	
	private static void dropDBs() {
		Factbase.getInstance().drop();
		TRIE_DB.drop();
		LIST_DB.drop();
	}
	
	private static void compareTriesAndLists() {
		fillDBs(files[3]);
		
		Variable v0 = new Variable();
		Variable v1 = new Variable();
		Variable v2 = new Variable();
		Variable v3 = new Variable();
		Variable v4 = new Variable();
		Variable v5 = new Variable();
		Variable v6 = new Variable();
		Variable v7 = new Variable();
		Variable v8 = new Variable();
		Variable v9 = new Variable();
		
		System.out.println("-----------------------");
		
		DatabasePredicate instr3 = new Instr3();
		DatabasePredicate classFile10 = new ClassFile10();
		
		// instr(X, Y, Z).
		System.out.print("With Tries: ");
		instr3.useTries();
		q(instr3, v0, v1, v2);

		System.out.print("Without Tries: ");
		instr3.useLists();
		q(instr3, v0, v1, v2);
		
		System.out.println("-----------------------");
		
		// instr(X, 1, Z).
		System.out.print("With Tries: ");
		instr3.useTries();
		q(instr3, v0, DatabaseTermFactory.makeIntegerAtom(1), v2);
		
		System.out.print("Without Tries: ");
		instr3.useLists();
		q(instr3, v0, DatabaseTermFactory.makeIntegerAtom(1), v2);
		
		System.out.println("-----------------------");
		
		print = true;
		// instr(m_1, Y, Z).
		System.out.print("With Tries: ");
		instr3.useTries();
		q(instr3, DatabaseTermFactory.makeStringAtom("m_20"), v1, v2);
		
		System.out.print("Without Tries: ");
		instr3.useLists();
		q(instr3, DatabaseTermFactory.makeStringAtom("m_20"), v1, v2);
		
		print = false;
		// class_file(?, ? , ?, ?, ?, ?, final(yes), ?, ?, ?);
		System.out.print("With Tries: ");
		classFile10.useTries();
		q(classFile10, DatabaseTermFactory.makeStringAtom("cf_20"), v1, v2, v3, v4, v5, v6, v7, v8, v9/*...*/);
		
		System.out.print("Without Tries: ");
		classFile10.useLists();
		q(classFile10, DatabaseTermFactory.makeStringAtom("cf_20"), v1, v2, v3, v4, v5, v6, v7, v8, v9/*...*/);
	}
	
	// print statistics XXX why is TRie filled twice???
	private static void printStatistics() {
		//Trie.setTermFlattener(new ShallowTermFlattener()); // must be set earlier
		for (String file : files) {
			dropDBs();
			fillDBs(file);
			StringAtom instr = DatabaseTermFactory.makeStringAtom("instr");
			Iterator<Term> listFacts = ListDatabase.getInstance().getFacts(instr);
			Iterator<Term> trieFacts = TrieDatabase.getInstance().getFacts(instr);

			int counter = 0;
			Stopwatch sw = new Stopwatch();
			while (trieFacts.hasNext()) {
				trieFacts.next();
				counter++;
			}
			sw.printElapsedAndReset("Iteration over the trie with " + counter + " terms");
			counter = 0;
			while (listFacts.hasNext()) {
				listFacts.next();
				counter++;
			}
			sw.printElapsedAndReset("Iteration over the list with " + counter + " terms");
			
			TrieInspector inspector = new TrieInspector();
			inspector.inspect(((TrieDatabase) TRIE_DB).getRoot());
			inspector.printStats();
			System.out.println();
		}
	}
	
	// create trie gv
	private static void createGvFile() {
		fillDBs(files[0]);
		TrieInspector inspector = new TrieInspector();
		inspector.print(((TrieDatabase) TRIE_DB).getRoot(), "C:/Users/Leaf/Desktop/trie.gv", true);
	}
	
	private static void q(DatabasePredicate p, Term ... terms) {
		if (print) {
			query(p, terms);
		} else {
			queryNoPrint(p, terms);
		}	
	}
	
	private static void smallFlatteningTests() {
		
		// f(a, b, c)
		Term fabc_0 = DatabaseTermFactory.makeCompoundTerm("f", new Term[] {
				DatabaseTermFactory.makeStringAtom("a"),
				DatabaseTermFactory.makeStringAtom("b"),
				DatabaseTermFactory.makeStringAtom("c"),
		});
		
		// f(a, b(c))
		Term fabc_1 = DatabaseTermFactory.makeCompoundTerm("f", new Term[] {
				DatabaseTermFactory.makeStringAtom("a"),
				DatabaseTermFactory.makeCompoundTerm("b", new Term[] {
						DatabaseTermFactory.makeStringAtom("c")	
				})
		});
		
		// f(a, 1)
		Term fa1_0 = DatabaseTermFactory.makeCompoundTerm("f", new Term[] {
				DatabaseTermFactory.makeStringAtom("a"),
				DatabaseTermFactory.makeIntegerAtom(1)
		});
		
		TermFlattener shallow = new ShallowTermFlattener();
		TermFlattener recursive = new RecursiveTermFlattener();
		
		// shallow flattening
		System.out.println("Shallow Flattening");
		Term[] terms0 = shallow.flatten(fabc_0);
		System.out.print(termToString(fabc_0) + " - > ");
		for (Term term : terms0) {
			System.out.print(termToString(term) + " ");
		}
		System.out.println();
		Term[] terms1 = shallow.flatten(fabc_1);
		System.out.print(termToString(fabc_1) + " - > ");
		for (Term term : terms1) {
			System.out.print(termToString(term) + " ");
		}
		System.out.println();
		Term[] terms2 = shallow.flatten(fa1_0);
		System.out.print(termToString(fa1_0) + " - > ");
		for (Term term : terms2) {
			System.out.print(termToString(term) + " ");
		}
		
		System.out.println("\n");
		
		// recursive flattening
		System.out.println("Recursive Flattening");
		terms0 = recursive.flatten(fabc_0);
		System.out.print(termToString(fabc_0) + " - > ");
		for (Term term : terms0) {
			System.out.print(term + " ");
		}
		System.out.println();
		terms1 = recursive.flatten(fabc_1);
		System.out.print(termToString(fabc_1) + " - > ");
		for (Term term : terms1) {
			System.out.print(term + " ");
		}
		System.out.println();
		terms2 = recursive.flatten(fa1_0);
		System.out.print(termToString(fa1_0) + " - > ");
		for (Term term : terms2) {
			System.out.print(term + " ");
		}
		
		// generate some output for graphviz
		TrieInspector inspector = new TrieInspector();
		
		Trie.setTermFlattener(shallow);
		Trie root = new Trie();
		root.insert(fabc_0);
		root.insert(fabc_1);
		inspector.print(root, "c:/users/leaf/desktop/shallow_trie.gv", false);
		
		Trie.setTermFlattener(recursive);
		root = new Trie();
		root.insert(fabc_0);
		root.insert(fabc_1);
		inspector.print(root, "c:/users/leaf/desktop/recursive_trie.gv", false);
	}
	
	private static void bigFlatteningTests() {
		AbstractTermFlattener shallow = new ShallowTermFlattener();
		AbstractTermFlattener recursive = new RecursiveTermFlattener();
		TrieInspector inspector = new TrieInspector();
		
		shallow.setMaxLength(5);
		Trie.setTermFlattener(shallow);
		fillDBs(files[4]);
		Trie root = ((TrieDatabase) TRIE_DB).getRoot();
		inspector.print(root, "c:/users/leaf/desktop/shallow_trie.gv", false);
		
		dropDBs();
		recursive.setMaxLength(5);
		Trie.setTermFlattener(recursive);
		fillDBs(files[4]);
		root = ((TrieDatabase) TRIE_DB).getRoot();
		inspector.print(root, "c:/users/leaf/desktop/recursive_trie.gv", false);
	}
	
	private static void labelTests() {
		
		// f, a, b, c
		Term f = DatabaseTermFactory.makeStringAtom("f");
		Term a = DatabaseTermFactory.makeStringAtom("a");
		Term b = DatabaseTermFactory.makeStringAtom("b");
		Term c = DatabaseTermFactory.makeStringAtom("c");
		
		Label fabc = Label.makeLabel(new Term[] { f, a, b, c });
		Label[] labels = fabc.split(1);
		Label fa = labels[0];
		Label bc = labels[1];
		
		System.out.println("Label fabc = " + fabc);
		System.out.println("Label fa = " + fa);
		System.out.println("Label bc = " + bc);
		
		System.out.println("Match fabc/fa = " + fabc.match(fa));
		System.out.println("Match fabc/bc = " + fabc.match(bc));
		System.out.println("Match fabc/f = " + fabc.match(Label.makeLabel(f)));
		
		System.out.println("f, a, b, c -> " + termToString(fabc.getLabel(0)) + " " + termToString(fabc.getLabel(1)) + " " + termToString(fabc.getLabel(2)) + " " + termToString(fabc.getLabel(3)));
		System.out.println("a -> " + termToString(Label.makeLabel(a).getLabel(0)));
	}
	
	private static void complexInserterTest() {
		
		// f(a, b, c)
		Term fabc_0 = DatabaseTermFactory.makeCompoundTerm("f", new Term[] {
				DatabaseTermFactory.makeStringAtom("a"),
				DatabaseTermFactory.makeStringAtom("b"),
				DatabaseTermFactory.makeStringAtom("c"),
		});
		
		// f(a, b(c))
		Term fabc_1 = DatabaseTermFactory.makeCompoundTerm("f", new Term[] {
				DatabaseTermFactory.makeStringAtom("a"),
				DatabaseTermFactory.makeCompoundTerm("b", new Term[] {
						DatabaseTermFactory.makeStringAtom("c")	
				})
		});
		
		// f(d, 1)
		Term fd1_0 = DatabaseTermFactory.makeCompoundTerm("f", new Term[] {
				DatabaseTermFactory.makeStringAtom("d"),
				DatabaseTermFactory.makeIntegerAtom(1)
		});
		
		Trie root = new Trie();
		TrieInspector inspector = new TrieInspector();
		
		root.insert(fabc_0);
		System.out.println("Inserted f(a, b, c)");
		inspector.print(root, "c:/users/leaf/desktop/complex-trie_0.gv", false);
		
		root.insert(fabc_1);
		System.out.println("Inserted f(a, b(c))");
		inspector.print(root, "c:/users/leaf/desktop/complex-trie_1.gv", false);
		
		root.insert(fd1_0);
		System.out.println("Inserted f(d, 1)");
		inspector.print(root, "c:/users/leaf/desktop/complex-trie_2.gv", false);
	}
}
