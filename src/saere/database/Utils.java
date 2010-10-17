package saere.database;

import java.util.Arrays;

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
	 * Convenience method that prints out queries.
	 * 
	 * @param p A database predicate (e.g., <tt>instr/3</tt>)
	 * @param terms The query as {@link Term}<tt>[]</tt> array.
	 */
	public static void query(DatabasePredicate p, Term... terms) {
		
		System.out.println("Unification of " + p.toString() + " and " + Arrays.toString(terms) + ":\n");
		Stopwatch sw = new Stopwatch();

		Solutions solutions = p.unify(terms);
		int counter = 0;
		while (solutions.next()) {
			for (int i = 0; i < terms.length; i++) {
				System.out.println(terms[i] + " = " + termToString(terms[i]));
			}
			System.out.println();
			counter++;
		}
		System.out.print(counter + " solutions found, ");
		sw.printElapsed("query");
		System.out.println();
	}
	
	/**
	 * Convenience method that summarizes queries.
	 * 
	 * @param p A database predicate (e.g., <tt>instr/3</tt>)
	 * @param terms The query as {@link Term}<tt>[]</tt> array.
	 */
	public static void queryNoPrint(DatabasePredicate p, Term ... terms) {

		System.out.println("Unification of " + p.toString() + " and " + Arrays.toString(terms) + ":");
		Stopwatch sw = new Stopwatch();
		
		Solutions solutions = p.unify(terms);
		int counter = 0;
		while (solutions.next()) {
			counter++;
		}
		System.out.print(counter + " solutions found, ");
		sw.printElapsed("query");
		System.out.println();
	}
}
