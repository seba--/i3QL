package saere.database;

import saere.Solutions;
import saere.Term;
import saere.Variable;
import saere.database.predicate.DatabasePredicate;
import saere.meta.GenericCompoundTerm;

/**
 * The waste dump...
 * 
 * @author Anonymous
 */
public class Utils {

	public static final String DEBUG_DUMP = "c:/users/leaf/desktop/debug_dump/";
	
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
				System.out.println("arg" + i + " = " + termToString(terms[i]));
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
}
