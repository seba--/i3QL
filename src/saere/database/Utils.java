package saere.database;

import java.util.Calendar;

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
	 * Checks wether the specified term is a fact.
	 * 
	 * @param term The term to check.
	 * @return <tt>true</tt> if so.
	 */
	public static boolean isFact(Term term) {
		return !hasVariable(term);
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
	 * Checks wether the specified term is a free variable.
	 * 
	 * @param term The term to check.
	 * @return <tt>true</tt> if so.
	 */
	public static boolean isFreeVariable(Term term) {
		return term.isVariable() && !term.asVariable().isInstantiated();
	}
	
	/**
	 * Convenience method for queries that creates no output.
	 * 
	 * @param p A database predicate (e.g., <tt>instr/3</tt>)
	 * @param query The query.
	 */
	public static void queryNoPrint(DatabasePredicate p, Term query) {
		Solutions solutions = p.unify(query);
		while (solutions.next()) {}
	}

	/**
	 * Convenience method that summarizes query results.
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
	
	/**
	 * Convenience method that prints out query results.
	 * 
	 * @param p A database predicate (e.g., <tt>instr/3</tt>)
	 * @param query The query.
	 */
	public static void queryAndPrint(DatabasePredicate p, Term query) {
		System.out.print("Query " + termToString(query) + ": ");
		Stopwatch sw = new Stopwatch();
		Solutions solutions = p.unify(query);
		int counter = 0;
		while (solutions.next()) {
			System.out.println(termToString(query));
			counter++;
		}
		System.out.print(counter + " solutions found, ");
		sw.printElapsed("query");
	}
	
	/**
	 * Current timestamp as string.
	 * 
	 * @return A timestamp.
	 */
	public static String timestamp() {
		Calendar c = Calendar.getInstance();
		StringBuilder sb = new StringBuilder();
		
		sb.append(c.get(Calendar.YEAR));
		sb.append("-");
		sb.append((c.get(Calendar.MONTH) + 1));
		sb.append("-");
		sb.append(c.get(Calendar.DATE));
		sb.append("_");
		sb.append(c.get(Calendar.HOUR_OF_DAY));
		sb.append("-");
		sb.append(c.get(Calendar.MINUTE));
		sb.append("-");
		sb.append(c.get(Calendar.SECOND));
		
		return sb.toString();
	}
}
