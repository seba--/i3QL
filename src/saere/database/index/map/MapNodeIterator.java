package saere.database.index.map;

import java.util.Iterator;
import java.util.NoSuchElementException;


/**
 * An iterator for a {@link MapTrie} that iterates over the {@link MapTrie} and all 
 * its descendants in a depth-first, from the left to the right manner.
 * 
 * @author David Sullivan
 * @version 0.2, 10/18/2010
 */
public class MapNodeIterator extends MapIteratorBase implements Iterator<MapTrie> {

	/**
	 * The next {@link MapTrie} node. It is set by {@link MapNodeIterator#findNext()} (only).
	 */
	private MapTrie next;
	
	/**
	 * Creates a new {@link MapNodeIterator} and finds the first <tt>next</tt>.
	 * 
	 * @param start The start node for the iteration.
	 */
	protected MapNodeIterator(MapTrie start) {
		super(start);
		next = current;
	}
	
	@Override
	public boolean hasNext() {
		return next != null;
	}

	@Override
	public MapTrie next() {
		if (hasNext()) {
			MapTrie oldNext = next;
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
