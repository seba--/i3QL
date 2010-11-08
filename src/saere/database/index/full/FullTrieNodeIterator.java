package saere.database.index.full;

import java.util.Iterator;
import java.util.NoSuchElementException;


/**
 * An iterator for a {@link FullTrie} that iterates over the {@link FullTrie} and all 
 * its descendants in a depth-first, from the left to the right manner.
 * 
 * @author David Sullivan
 * @version 0.2, 10/18/2010
 */
public class FullTrieNodeIterator extends FullIteratorBase implements Iterator<FullTrie> {

	/**
	 * The next {@link FullTrie} node. It is set by {@link FullTrieNodeIterator#findNext()} (only).
	 */
	private FullTrie next;
	
	/**
	 * Creates a new {@link FullTrieNodeIterator} and finds the first <tt>next</tt>.
	 * 
	 * @param start The start node for the iteration.
	 */
	protected FullTrieNodeIterator(FullTrie start) {
		super(start);
		next = current;
	}
	
	@Override
	public boolean hasNext() {
		return next != null;
	}

	@Override
	public FullTrie next() {
		if (hasNext()) {
			FullTrie oldNext = next;
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
