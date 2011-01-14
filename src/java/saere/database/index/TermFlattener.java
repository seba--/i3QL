package saere.database.index;

import saere.Term;
import saere.database.profiling.Profiler;
import saere.database.profiling.Profiler.Mode;

/**
 * Common abstract base class for term flatteners. A {@link TermFlattener} 
 * defines how a {@link Term} is flattend (&quot;hashed&quot;) for storage and 
 * a query for retrieval.
 * 
 * @author David Sullivan
 * @version 0.5, 11/29/2010
 */
public abstract class TermFlattener {
	
	private static final Profiler PROFILER = Profiler.getInstance();
	
	/**
	 * Flattens a term. That is, creates a non-nested (no compound terms), 
	 * array-like representation of the specified term.
	 * 
	 * @param term The term to flatten.
	 * @return The flattened term representation.
	 */
	public abstract LabelStack flatten(Term term);
	
	/**
	 * Gets the arguments of the term as array of terms. If profiles should be 
	 * use the arguments are ordered accordingly.
	 * 
	 * @param term The term with arguments.
	 * @return The (eventually ordered) arguments of the term as array.
	 */
	protected Term[] getArgs(Term term) {
		assert term.isCompoundTerm() : "Specified term is not a compound term";
		
		if (PROFILER.mode() == Mode.USE) {
			return PROFILER.getOrderedArgs(term);
		} else {
			Term[] args = new Term[term.arity()];
			for (int i = 0; i < args.length; i++) {
				args[i] = term.arg(i);
			}
			return args;
 		}
	}
}
