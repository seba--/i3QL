package saere.database;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Iterator;

import saere.IntegerAtom;
import saere.Solutions;
import saere.StringAtom;
import saere.Term;
import saere.Variable;
import saere.database.index.ComplexTermInserter;
import saere.database.index.RecursiveTermFlattener;
import saere.database.index.ShallowTermFlattener;
import saere.database.index.SimpleTermInserter;
import saere.database.index.TermFlattener;
import saere.database.index.TermInserter;
import saere.database.index.Trie;
import saere.database.predicate.DatabasePredicate;
import saere.database.predicate.Instr3;
import saere.database.profiling.TrieInspector;

public class Iterator_Lab {
	
	private static final TermInserter SIMPLE = new SimpleTermInserter();
	private static final TermInserter COMPLEX = new ComplexTermInserter();
	
	private static final TermFlattener SHALLOW = new ShallowTermFlattener();
	private static final TermFlattener RECURSIVE = new RecursiveTermFlattener();
	
	private static final Factbase FACTS = Factbase.getInstance();
	
	public static void main(String[] args) throws Exception {
		//experimentNodeIterator();
		//experimentIterator();
		//experimentSimpleIterator();
		//experimentPredicate();
		experimentComplexInserter();
		//experimentComplexIterator();
	}
	
	// seeeems to work
	private static void experimentNodeIterator() {
		Trie.setTermFlattener(SHALLOW);
		Trie.setTermInserter(SIMPLE);
		
		Trie root = new Trie();
		FACTS.drop();
		FACTS.read("../test/classfiles/HelloWorld.class");
		for (Term fact : FACTS.getFacts()) {
			root.insert(fact);
		}
		
		Iterator<Trie> iter = root.nodeIterator();
		while (iter.hasNext()) {
			System.out.println(iter.next());
		}
	}
	
	// also seems to work
	private static void experimentIterator() {
		Trie.setTermFlattener(SHALLOW);
		Trie.setTermInserter(SIMPLE);
		
		Trie root = new Trie();
		FACTS.drop();
		FACTS.read("../test/classfiles/HelloWorld.class");
		for (Term fact : FACTS.getFacts()) {
			root.insert(fact);
		}
		
		Iterator<Term> iter = root.iterator();
		while (iter.hasNext()) {
			System.out.println(Utils.termToString(iter.next()));
		}
	}
	
	// seems to work too
	private static void experimentSimpleIterator() {
		Trie.setTermFlattener(SHALLOW);
		Trie.setTermInserter(SIMPLE);
		
		Trie root = new Trie();
		FACTS.drop();
		FACTS.read("../test/classfiles/HelloWorld.class");
		for (Term fact : FACTS.getFacts()) {
			root.insert(fact);
		}
		
		Term instr = DatabaseTermFactory.makeStringAtom("instr");
		Term m_2 = DatabaseTermFactory.makeStringAtom("m_2");
		Term X = new Variable();
		Term i0 = DatabaseTermFactory.makeIntegerAtom(0);
		Term returnInstr = DatabaseTermFactory.makeStringAtom("return");
		Term invokeInstr = DatabaseTermFactory.makeStringAtom("invoke");
		Iterator<Term> iter = root.iterator(new Term[] { instr, X, i0, X });
		while (iter.hasNext()) {
			System.out.println(Utils.termToString(iter.next()));
		}
	}
	
	// works also
	private static void experimentPredicate() {
		Trie.setTermFlattener(RECURSIVE);
		Trie.setTermInserter(SIMPLE);
		
		FACTS.drop();
		FACTS.read("../test/classfiles/HelloWorld.class");
		Database database = TrieDatabase.getInstance();
		database.fill();
		
		DatabasePredicate instr3 = new Instr3();
		instr3.useTries();
		Variable X = new Variable();
		Variable Y = new Variable();
		Variable Z = new Variable();
		
		Solutions solutions = instr3.unify(X, Y, Z);
		while (solutions.next()) {
			System.out.println("X = " + Utils.termToString(X.binding()));
			System.out.println("Y = " + Utils.termToString(Y.binding()));
			System.out.println("Z = " + Utils.termToString(Z.binding()) + "\n");
		}
	}
	
	private static void experimentComplexInserter() throws IOException {
		Trie.setTermFlattener(RECURSIVE);
		Trie.setTermInserter(COMPLEX);
		
		FACTS.drop();
		FACTS.read("../test/classfiles/HelloWorld.class");
		Trie root = new Trie();
		OutputStream out = new FileOutputStream("c:/users/leaf/desktop/recursive_complex_hello-world.txt");
		int counter = 0;
		for (Term fact : FACTS.getFacts()) {
			counter++;
			if (counter > 7)
				break;
			
			out.write((Utils.termToString(fact) + "\n").getBytes());
			root.insert(fact);
		}
		out.close();
		
		TrieInspector inspector = new TrieInspector();
		inspector.print(root, "c:/users/leaf/desktop/recursive_complex_hello-world.gv", false);
		
		/*
		Iterator<Term> iter = root.iterator();
		while (iter.hasNext()) {
			System.out.println(Utils.termToString(iter.next()));
		}
		*/
	}
	
	private static void experimentComplexIterator() throws IOException {
		Trie.setTermFlattener(RECURSIVE);
		Trie.setTermInserter(COMPLEX);
		
		FACTS.drop();
		FACTS.read("../test/classfiles/HelloWorld.class");
		Trie root = new Trie();
		for (Term fact : FACTS.getFacts()) {
			root.insert(fact);
		}
		
		Term instr = DatabaseTermFactory.makeStringAtom("instr");
		Term m_1 = DatabaseTermFactory.makeStringAtom("m_1");
		Term i0 = DatabaseTermFactory.makeIntegerAtom(0);
		Term i1 = DatabaseTermFactory.makeIntegerAtom(1);
		Term _return = DatabaseTermFactory.makeStringAtom("return");
		Term invoke = DatabaseTermFactory.makeStringAtom("invoke");
		Term X = new Variable();
		
		Iterator<Term> iter = root.iterator(new Term [] { instr, X, i1 });
		while (iter.hasNext()) {
			System.out.println(Utils.termToString(iter.next()));
		}
	}
}
