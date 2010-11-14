package saere.database;

import saere.CompoundTerm;
import saere.Solutions;
import saere.Term;
import saere.Variable;
import saere.database.predicate.DatabasePredicate;

/**
 * The waste dump...
 * 
 * @author Anonymous
 */
public final class Utils {
	
	private Utils() { /* empty */ }
	
	/**
	 * Creates a human-readable string representation of the specified 
	 * {@link Term}.
	 * 
	 * @param t The term.
	 * @return A string representation of the term.
	 */
	public static String termToString(Term t) {
		String s = "";
		if (!t.isVariable()) {
			s = t.functor().toString();
			if (t.isCompoundTerm()) {
				CompoundTerm ct = t.asCompoundTerm();
				s += "(";
				boolean first = true;
				for (int i = 0; i < t.arity(); i++) {
					s += first ? "" : ", ";
					first = false;
					s += termToString(ct.arg(i));
				}
				s += ")";
			}
		} else {
			Variable v = t.asVariable();
			if (v.isInstantiated()) {
				s = termToString(v.binding());
			} else {
				s = v.toString();
			}
		}
		
		return s;
	}
	
	/**
	 * Checks wether the specified term is or has at least one variable.
	 * 
	 * @param term The term to check.
	 * @return <tt>true</tt> if so.
	 */
	public static boolean hasVariable(Term term) {
		if (term.isVariable()) {
			return true;
		} else if (term.isCompoundTerm()) {
			CompoundTerm ct = term.asCompoundTerm();
			for (int i = 0; i < ct.arity(); i++) {
				if (hasVariable(ct.arg(i))) {
					return true;
				}
			}
		}
		
		return false;
	}
	
	/**
	 * Checks wether the specified term is or has at least one free (unbound) 
	 * variable.
	 * 
	 * @param term The term to check.
	 * @return <tt>true</tt> if so.
	 */
	public static boolean hasFreeVariable(Term term) {
		if (term.isVariable() && !term.asVariable().isInstantiated()) {
			return true;
		} else if (term.isCompoundTerm()) {
			CompoundTerm ct = term.asCompoundTerm();
			for (int i = 0; i < ct.arity(); i++) {
				if (hasFreeVariable(ct.arg(i))) {
					return true;
				}
			}
		}
		
		return false;
	}

	/**
	 * Convenience method that summarizes queries.
	 * 
	 * @param p A database predicate (e.g., <tt>instr/3</tt>)
	 * @param query The query.
	 */
	public static void query(DatabasePredicate p, Term query) {
		System.out.print("Query " + termToString(query) + ": ");
		Stopwatch sw = new Stopwatch();
		Solutions solutions = p.unify(query);
		int counter = 0;
		while (solutions.next()) {
			counter++;
		}
		System.out.print(counter + " solutions found, ");
		sw.printElapsed("query");
	}
}
