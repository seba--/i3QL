package saere.database.index;

import java.util.Iterator;
import java.util.NoSuchElementException;

import saere.Term;

/**
 * A simple trie iterator that iterates over all terms in a trie. The iteration 
 * is done is a lazy way. That is, a new element is only searched for after a 
 * call to {@link Iterator#next()}. (One step ahead.)
 * 
 * @author David Sullivan
 * @version 0.5, 11/9/2010
 */
public class TermIterator extends IteratorBase implements Iterator<Term> {

	/** The term list of the current position. If it's not null, we iterate over this first... */
	protected TermList list;
	
	/** The next term. It is set by {@link ShallowSimpleQueryIterator#findNext()} (only). */
	protected Term next;
	
	/**
	 * Creates a new simple trie iterater that treats the specified 
	 * <tt>start</tt> as root. Also, the first <tt>next</tt> is found.
	 * 
	 * @param start The start, i.e., the root for the iteration.
	 */
	protected TermIterator(Trie start) {
		super(start);
		list = start.getTerms();
		findNext();
	}

	@Override
	public boolean hasNext() {
		return next != null;
	}

	@Override
	public Term next() {
		if (hasNext()) {
			Term oldNext = next;
			findNext();
			return oldNext;
		} else {
			throw new NoSuchElementException();
		}
	}
	
	@Override
	protected void findNext() {
		next = null;
		
		if (list != null) {
			// List processing mode
			next = list.term();
			list = list.next();
		} else {
			// Normal mode, as long as we have nodes left and have no next term (list)
			while (current != null && next == null) { 
				if (current.stores()) {
					list = current.getTerms();
					next = list.term();
					list = list.next();
				}
				nextNode();
			}
		}
	}

	@Override
	public void remove() {
		throw new UnsupportedOperationException();
	}
	
	@Override
	protected void resetTo(Trie newStart) {
		super.resetTo(newStart);
		list = newStart.getTerms();
		findNext();
	}

}
