package saere.database.index;

import saere.Term;

/**
 * A slim implementation of a term list.
 * 
 * @author David Sullivan
 * @version 0.2, 11/9/2010
 */
public final class TermList {
	
	private final Term term;
	private final TermList next;
	
	public TermList(Term term, TermList next) {
		this.term = term;
		this.next = next;
	}
	
	public Term term() {
		return term;
	}
	
	public TermList next() {
		return next;
	}
}
