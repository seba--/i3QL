package saere.database;

import saere.Term;

/**
 * A slim implementation of a term list that is kept very simple.
 * 
 * @author David Sullivan
 * @version $Id$
 */
public class TermList {
	
	private Term term;
	private Term next;
	
	public TermList(Term term) {
		this.term = term;
		next = null;
	}
	
	public Term getTerm() {
		return term;
	}
	
	public Term getNext() {
		return next;
	}
	
	public void setNext(Term next) {
		this.next = next;
	}
}
