package saere.database.index;


/**
 * An iterator that yields the last nodes of a (compound) term complex.<br>
 * <br>
 * For example, if a tries stores the terms <tt>f(a(d),b)</tt>, <tt>f(a,b)</tt> 
 * and <tt>f(a(d,e),b)</tt> the iterator would iterate <tt>d</tt> (from the 
 * first term), <tt>a</tt> (from the second term) and <tt>e</tt> (from the 
 * third term) if the variable iterator was initialized at <tt>X</tt> in 
 * <tt>f(X,b)</tt>.
 * 
 * @author David Sullivan
 * @version 0.1, 14/11/2010
 */
public final class VariableIterator extends NodeIterator {

	private int stack;
	private Trie last;
	
	/**
	 * Simply creates the object. No initialization.
	 */
	protected VariableIterator() {
		super(null);
	}
	
	/**
	 * Creates a variable iterator and initializes it for iteration.
	 * 
	 * @param start The first trie that is covered by a variable.
	 */
	protected VariableIterator(Trie start) {
		super(null);
		resetTo(start);
	}
	
	@Override
	protected void findNext() {
		last = next;
		next = null;
		while (next == null && current != null) {
			
			// Stack must be 0
			if (stack == 0) {
				next = current;
				goRight(); // Do so as if we couldn't go down, as if we'd reached a leaf
			} else {
				nextNode();
			}
		}
	}
	
	@Override
	protected void goUp() {
		if (current.getLabel() != null && current.getLabel().arity() > 0) {
			stack -= current.getLabel().arity();
		}
		stack++;
		super.goUp();
	}
	
	@Override
	protected void goDown() {
		// Everytime we go down we simply decrease the stack by 1.
		// If the node we'll arrive at will be a functor we increase the stack by its arity.
		Trie down = current.getFirstChild();
		if (down.getLabel() != null && down.getLabel().arity() > 0) {
			stack += down.getLabel().arity();
		}
		stack--;
		super.goDown();
	}
	
	@Override
	protected void goRight() {
		while (current.getNextSibling() == null && current != start) { // don't go higher up than start
			goUp();
		}
		
		if (current != start) {
			if (current.getLabel().arity() > 0) {
				stack -= current.getLabel().arity();
			}
			current = current.getNextSibling();
			if (current.getLabel().arity() > 0) {
				stack += current.getLabel().arity();
			}
		} else {
			current = null;
		}
	}

	@Override
	protected void resetTo(Trie newStart) {
		super.resetTo(null);
		assert newStart.getParent().getFirstChild() == newStart : "Start trie node is not a first child";
		
		this.start = newStart.getParent();
		current = newStart;
		stack = current.getLabel().arity();
		findNext();
	}
	
	/**
	 * Gets the trie node with which the current iteration was started.
	 * 
	 * @return The start trie node.
	 */
	protected Trie start() {
		return start;
	}
	
	/**
	 * The last trie node that was iterated.
	 * 
	 * @return The last trie node.
	 */
	protected Trie last() {
		return last;
	}
}
