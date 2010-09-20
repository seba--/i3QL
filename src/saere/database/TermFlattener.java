package saere.database;

import saere.Term;

/**
 * Common interface for term flatteners.
 * 
 * @author David Sullivan
 * @version $Id$
 */
public interface TermFlattener {
	
	/**
	 * Flattens a term. That is, creates a non-nested, array-like representation 
	 * of the specified term that consists only of atoms and variables.
	 * 
	 * @param term The term to flatten.
	 * @return The flattened term representation.
	 */
	public Term[] flatten(Term term);
}
