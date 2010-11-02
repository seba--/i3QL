package saere.database.index.unique;

import java.util.Iterator;
import java.util.NoSuchElementException;


/**
 * An iterator for a {@link UniqueTrie} that iterates over the {@link UniqueTrie} and all 
 * its descendants in a depth-first, from the left to the right manner.
 * 
 * @author David Sullivan
 * @version 0.2, 10/18/2010
 */
public class UniqueTrieNodeIterator extends UniqueIteratorBase implements Iterator<UniqueTrie> {

	/**
	 * The next {@link UniqueTrie} node. It is set by {@link UniqueTrieNodeIterator#findNext()} (only).
	 */
	private UniqueTrie next;
	
	/**
	 * Creates a new {@link UniqueTrieNodeIterator} and finds the first <tt>next</tt>.
	 * 
	 * @param start The start node for the iteration.
	 */
	protected UniqueTrieNodeIterator(UniqueTrie start) {
		super(start);
		next = current;
	}
	
	@Override
	public boolean hasNext() {
		return next != null;
	}

	@Override
	public UniqueTrie next() {
		if (hasNext()) {
			UniqueTrie oldNext = next;
			findNext();
			return oldNext;
		} else {
			throw new NoSuchElementException();
		}
	}

	@Override
	public void remove() {
		throw new UnsupportedOperationException();	
	}
	
	@Override
	protected void findNext() {
		nextNode();
		next = current;
	}
}
