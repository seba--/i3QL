package saere.database;

import java.util.List;

import saere.Solutions;
import saere.Term;
import saere.Variable;
import saere.meta.GenericCompoundTerm;

public class Utils {

	public static String termToString(Term t) {
		String s = "";
		if (!t.isVariable()) {
			s = t.functor().toString();
			if (t instanceof GenericCompoundTerm) {
				s += "(";
				boolean first = true;
				for (int i = 0; i < t.arity(); i++) {
					s += first ? "" : ", ";
					first = false;
					s += termToString(((GenericCompoundTerm) t).arg(i));
				}
				s += ")";
			}
		} else {
			Variable v = (Variable) t;
			if (v.isInstantiated()) {
				s = termToString(v.binding());
			} else {
				s = "?";
			}
		}
		return s;
	}

	public static void query(DatabasePredicate p, Term... terms) {
		String query = termToString(DatabaseTermFactory.makeCompoundTerm(p.functor().toString(), terms)); // XXX ...
		System.out.println("Unification of " + p.toString() + " and " + query + ", Solutions:\n");
		Stopwatch sw = new Stopwatch();

		Solutions solutions = p.unify(terms);
		int counter = 0;
		while (solutions.next()) {
			for (int i = 0; i < terms.length; i++) {
				System.out.println("arg" + (i + 1) + " = " + termToString(terms[i]));
			}
			System.out.println();
			counter++;
		}
		System.out.print(counter + " Solutions found, ");
		sw.printElapsed("query");
		System.out.println();
	}
	
	public static void queryNoPrint(DatabasePredicate p, Term ... terms) {
		String query = termToString(DatabaseTermFactory.makeCompoundTerm(p.functor().toString(), terms)); // XXX ...
		System.out.println("Unification of " + p.toString() + " and " + query + ", Solutions:\n");
		Stopwatch sw = new Stopwatch();
		
		Solutions solutions = p.unify(terms);
		int counter = 0;
		while (solutions.next()) {
			counter++;
		}
		System.out.print(counter + " Solutions found, ");
		sw.printElapsed("query");
		System.out.println();
	}
	
	public static void printDB(Database db) {
		List<Term> terms = db.getFacts();
		for (Term term : terms) {
			System.out.println(termToString(term));
		}
	}
	
	public static void printTrieDB(Database_Trie tdb) {
		printTermTrie(tdb.getRoot(), 0);
	}
	
	private static void printTermTrie(Trie trie, int level) {
		for (int i = 0; i < level; i++) {
			System.out.print(" ");
		}
		String key = trie.isRoot() ? "<root>" : Utils.termToString(trie.getLabel());
		Term value = trie.getTerm();
		System.out.println(key + ":" + (value == null ? "null" : Utils.termToString(value)));
		Trie child = trie.getFirstChild();
		while (child != null) {
			printTermTrie(child, level + key.length());
			child = child.getNextSibling();
		}
	}
}
