package saere.database.index.map;

import saere.Term;

/**
 * A slim implementation of a term list that is kept very simple.
 * 
 * @author David Sullivan
 * @version 0.1, 9/22/2010
 */
public final class TermList {
	
	protected final Term term;
	protected TermList next;
	
	public TermList(Term term) {
		this.term = term;
		next = null;
	}
}
