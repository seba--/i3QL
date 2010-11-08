package saere.database.index.full;

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
 * @version 0.3, 9/22/2010
 */
public class FullTermIterator extends FullIteratorBase implements Iterator<Term> {
	
	/** The next term. It is set by {@link FullQueryTrieIterator#findNext()} (only). */
	protected Term next;
	
	/**
	 * Creates a new simple trie iterator <b>without intialization</b> (the first element is not found).
	 * @see FullIteratorBase#BaseTrieIterator()
	 */
	protected FullTermIterator() { /* empty */ }
	
	/**
	 * Creates a new simple trie iterater that treats the specified 
	 * <tt>start</tt> as root. Also, the first <tt>next</tt> is found.
	 * 
	 * @param start The start, i.e., the root for the iteration.
	 */
	protected FullTermIterator(FullTrie start) {
		super(start);
		init();
	}
	
	/**
	 * Initializes this iterator (used in the constructor and {@link #resetTo(FullTrie)}).
	 */
	private void init() {
		if (current != null && current.term != null) { // start already has a term
			next = current.term;
		} else { // start has a term
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
			
		// as long as we have nodes left and have no term list FIXME list == null should be unnecessary
		while (current != null /*&& current.term == null*/) {
			nextNode();
			if (current != null) {
				if (current.term != null) {
					next = current.term;
					break;
				} // else continue
			} else { // current == null
				break; // terminate
			}
		}				
	}

	@Override
	public void remove() {
		throw new UnsupportedOperationException();
	}
	
	@Override
	protected void resetTo(FullTrie newStart) {
		super.resetTo(newStart);
		init();
	}

}
