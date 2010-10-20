package saere.database.index;

import java.util.Iterator;
import java.util.NoSuchElementException;

import saere.Term;

/**
 * A simple trie iterator that iterates over all terms in a trie. The 
 * iteration is done is a lazy way. That is, a new element is only 
 * searched for after a call to {@link Iterator#next()}. (Actually one step
 * ahead.)
 * 
 * @author David Sullivan
 * @version 0.4, 10/19/2010
 */
public class TrieTermIterator<T> extends TrieIteratorBase<T> implements Iterator<Term> {

	/** The term list of the current position. If it's not null, we iterate over this first... */
	protected TermList list;
	
	/** The next term. It is set by {@link SimpleTrieTermIterator#findNext()} (only). */
	protected Term next;
	
	/**
	 * Creates a new simple trie iterator <b>without intialization</b>.
	 */
	// For subclasses...
	protected TrieTermIterator() {
		
	}
	
	/**
	 * Creates a new simple trie iterater that treats the specified 
	 * <tt>start</tt> as root. Also, the first <tt>next</tt> is found.
	 * 
	 * @param start The start, i.e., the root for the iteration.
	 */
	protected TrieTermIterator(Trie<T> start) {
		super(start);
		init();
	}
	
	/**
	 * Initializes this iterator (used in the constructor and {@link #resetTo(Trie)}).
	 */
	private void init() {
		list = null;
		if (current != null) { // start already has term(s)
			if (current.hasTermList()) { // Rare case
				next = current.getTermList().getTerm();
				list = current.getTermList().getNext();
			} else { // Normal case
				next = current.getTerm(); // ... and list stays null
				if (next == null)
					findNext();
			}
			
		} else { // start has no term(s)
			findNext();
		}
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
		
		if (list != null) { // list processing mode
			next = list.getTerm();
			list = list.getNext();
		} else { // normal mode
			
			// as long as we have nodes left and have no term list FIXME list == null should be unnecessary
			while (current != null && list == null) { // XXX while (true) basically the same here?
				nextNode();
				if (current != null) {
					if (current.hasTermList()) {
						next = current.getTermList().getTerm();
						list = current.getTermList().getNext();
						break;
					} else {
						next = current.getTerm();
						if (next != null)
							break;
					} // else continue
				} else { // current == null
					break; // terminate
				}
			}				
		}
	}

	@Override
	public void remove() {
		throw new UnsupportedOperationException();
	}
	
	@Override
	protected void resetTo(Trie<T> newStart) {
		super.resetTo(newStart);
		init();
	}

}
