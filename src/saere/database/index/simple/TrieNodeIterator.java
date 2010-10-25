package saere.database.index.simple;

import java.util.Iterator;
import java.util.NoSuchElementException;


/**
 * An iterator for a {@link Trie} that iterates over the {@link Trie} and all 
 * its descendants in a depth-first, from the left to the right manner.
 * 
 * @author David Sullivan
 * @version 0.2, 10/18/2010
 */
public class TrieNodeIterator extends TrieIteratorBase implements Iterator<Trie> {

	/**
	 * The next {@link Trie} node. It is set by {@link TrieNodeIterator#findNext()} (only).
	 */
	private Trie next;
	
	/**
	 * Creates a new {@link TrieNodeIterator} and finds the first <tt>next</tt>.
	 * 
	 * @param start The start node for the iteration.
	 */
	protected TrieNodeIterator(Trie start) {
		super(start);
		next = current;
	}
	
	@Override
	public boolean hasNext() {
		return next != null;
	}

	@Override
	public Trie next() {
		if (hasNext()) {
			Trie oldNext = next;
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
