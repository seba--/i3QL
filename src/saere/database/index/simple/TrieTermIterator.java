package saere.database.index.simple;

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
public class TrieTermIterator extends TrieIteratorBase implements Iterator<Term> {

	/** The term list of the current position. If it's not null, we iterate over this first... */
	protected TermList list;
	
	/** The next term. It is set by {@link SimpleTrieTermIterator#findNext()} (only). */
	protected Term next;
	
	/**
	 * Creates a new simple trie iterator <b>without intialization</b> (the first element is not found).
	 * @see TrieIteratorBase#BaseTrieIterator()
	 */
	protected TrieTermIterator() { /* empty */ }
	
	/**
	 * Creates a new simple trie iterater that treats the specified 
	 * <tt>start</tt> as root. Also, the first <tt>next</tt> is found.
	 * 
	 * @param start The start, i.e., the root for the iteration.
	 */
	protected TrieTermIterator(Trie start) {
		super(start);
		init();
	}
	
	/**
	 * Initializes this iterator (used in the constructor and {@link #resetTo(Trie)}).
	 */
	private void init() {
		list = null;
		if (current != null && current.termList != null) { // start already has term(s)
			next = current.termList.term;
			list = current.termList.next;
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
			next = list.term;
			list = list.next;
		} else { // normal mode
			
			// as long as we have nodes left and have no term list FIXME list == null should be unnecessary
			while (current != null && list == null) {
				nextNode();
				if (current != null) {
					if (current.termList != null) {
						next = current.termList.term;
						list = current.termList.next;
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
	protected void resetTo(Trie newStart) {
		super.resetTo(newStart);
		init();
	}

}
