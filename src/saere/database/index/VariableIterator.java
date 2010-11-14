package saere.database.index;

/**
 * An iterator that yields the last nodes of a (compound) term complex.<br>
 * <br>
 * For example, if a tries stores the terms <tt>f(a(d),b)</tt>, <tt>f(a,b)</tt> 
 * and <tt>f(a(d,e),b)</tt> the iteratour would iterate <tt>d</tt> (from the 
 * first term), <tt>a</tt> (from the second term) and <tt>e</tt> (from the 
 * third term).
 * 
 * @author David Sullivan
 * @version 0.1, 14/11/2010
 */
public final class VariableIterator extends NodeIterator {

	private int counter;
	
	protected VariableIterator(Trie start) {
		super(null);
		
		// That's why we may need a root meta node (<root>)
		this.start = start.getParent();
		current = start;
		counter = current.getLabel().arity();
		findNext();
	}

	@Override
	protected void findNext() {
		next = null;
		while (next == null && current != null) {
			
			// Counter must be 0
			if (counter == 0) {
				next = current;
				goRight(); // Do so as if we couldn't go down, as if we'd reached a leaf
			} else {
				nextNode();
			}
		}
	}
	
	@Override
	public void goUp() {
		if (current.getLabel() != null && current.getLabel().arity() > 0) {
			counter -= current.getLabel().arity();
		}
		counter++;
		super.goUp();
	}
	
	@Override
	public void goDown() {
		Trie down = current.getFirstChild();
		if (down.getLabel() != null && down.getLabel().arity() > 0) {
			counter += down.getLabel().arity();
		}
		counter--;
		super.goDown();
	}
	
	@Override
	public void goRight() {
		while (current.getNextSibling() == null && current != start) { // don't go higher up than start
			goUp();
		}
		
		if (current != start) {
			if (current.getLabel().arity() > 0) {
				counter -= current.getLabel().arity();
			}
			current = current.getNextSibling();
			if (current.getLabel().arity() > 0) {
				counter += current.getLabel().arity();
			}
		} else {
			current = null;
		}
	}

	@Override
	protected void resetTo(Trie newStart) {
		super.resetTo(null);
		
		this.start = newStart.getParent();
		current = newStart;
		counter = current.getLabel().arity();
		findNext();
	}
	
	public Trie getStart() {
		return start;
	}
}
