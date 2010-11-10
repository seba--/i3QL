package saere.database.index;

import java.util.Iterator;

import saere.Term;

/**
 * An iterator for {@link TermList}s.
 * 
 * @author David Sullivan
 * @version 0.1, 11/9/2010
 */
public final class TermListIterator implements Iterator<Term> {

	private TermList list;
	
	@Override
	public boolean hasNext() {
		return list.next() != null;
	}

	@Override
	public Term next() {
		Term term = list.term();
		list = list.next();
		return term;
	}

	@Override
	public void remove() {
		throw new UnsupportedOperationException();
	}
	
	/**
	 * Resets this iterator so that it begins a new iteration with the 
	 * specified list.<br>
	 * <br>
	 * This method can be used to avoid creating new iterator objects.
	 * 
	 * @param list The list to start a new iteration.
	 */
	public void reset(TermList list) {
		this.list = list;
	}
}
