package saere.database.index;

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
	public Atom[] flattenInsertion(Term term);
	
	/**
	 * Flattens a query that is represented by an array terms. The first 
	 * element of the array is the functor while additional elements of the 
	 * query are arguments. Usually, one cannot flatten a query by flatten each 
	 * argument for itself. This method takes care of creating a correct 
	 * flattened representation of a query.
	 * 
	 * @param terms The terms that represent a query (a term).
	 * @return The flattened version of the query.
	 */
	public Term[] flattenQuery(Term ... terms);
}
