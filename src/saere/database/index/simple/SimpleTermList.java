package saere.database.index.simple;

import saere.Term;

/**
 * A slim implementation of a term list that is kept very simple.
 * 
 * @author David Sullivan
 * @version 0.1, 9/22/2010
 */
public final class SimpleTermList {
	
	protected final Term term;
	protected SimpleTermList next;
	
	public SimpleTermList(Term term) {
		this.term = term;
		next = null;
	}
}
