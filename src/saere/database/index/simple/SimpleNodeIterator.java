package saere.database.index.simple;

import java.util.Iterator;
import java.util.NoSuchElementException;


/**
 * An iterator for a {@link SimpleTrie} that iterates over the {@link SimpleTrie} and all 
 * its descendants in a depth-first, from the left to the right manner.
 * 
 * @author David Sullivan
 * @version 0.2, 10/18/2010
 */
public class SimpleNodeIterator extends SimpleIteratorBase implements Iterator<SimpleTrie> {

	/**
	 * The next {@link SimpleTrie} node. It is set by {@link SimpleNodeIterator#findNext()} (only).
	 */
	private SimpleTrie next;
	
	/**
	 * Creates a new {@link SimpleNodeIterator} and finds the first <tt>next</tt>.
	 * 
	 * @param start The start node for the iteration.
	 */
	protected SimpleNodeIterator(SimpleTrie start) {
		super(start);
		next = current;
	}
	
	@Override
	public boolean hasNext() {
		return next != null;
	}

	@Override
	public SimpleTrie next() {
		if (hasNext()) {
			SimpleTrie oldNext = next;
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
