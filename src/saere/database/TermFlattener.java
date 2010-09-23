package saere.database;

import saere.Atom;
import saere.Term;
import saere.Variable;

/**
 * Common interface for term flatteners. A {@link TermFlattener} defines how a 
 * {@link Term} is flattend (&quot;hashed&quot;) for storage and retrieval.
 * 
 * @author David Sullivan
 * @version 0.1, 9/22/2010
 */
public interface TermFlattener {
	
	/**
	 * Flattens a term. That is, creates a non-nested, array-like representation 
	 * of the specified term that consists only of {@link Atom}s and free {@link Variable}s.
	 * 
	 * @param term The term to flatten.
	 * @return The flattened term representation.
	 */
	public Term[] flatten(Term term);
}
