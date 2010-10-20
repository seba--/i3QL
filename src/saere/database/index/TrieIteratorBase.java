package saere.database.index;

import java.util.Iterator;



/**
 * Abstract base class for {@link Trie} iterators that contains common methods.
 * 
 * @author David Sullivan
 * @version 0.2, 9/30/2010
 */
public abstract class TrieIteratorBase<T> {
	
	/**
	 * The node from where the iteration begins. From the point of view of the
	 * iterator it works as root. Only the <tt>start</tt> node and its descendants
	 * are considered to be iterated.
	 */
	protected Trie<T> start;
	
	/**
	 * The current position.
	 */
	protected Trie<T> current;
	
	/**
	 * Creates a new {@link TrieIteratorBase} and <b>does not</b> set the <tt>current</tt> 
	 * and <tt>start</tt> fields. This constructor is to be used if an extending 
	 * class wants to initialize the {@link #start} and {@link #current} fields itself.
	 */
	protected TrieIteratorBase() {
		start = current = null;
	}
	
	/**
	 * Creates a new {@link TrieIteratorBase} and sets the <tt>current</tt> 
	 * and <tt>start</tt> fields.
	 * 
	 * @param start The start node for the iteration.
	 */
	protected TrieIteratorBase(Trie<T> start) {
		this.start = start;
		current = start;
	}
	
	/**
	 * Finds the next <tt>next</tt>.
	 */
	protected abstract void findNext();
	
	/**
	 * Moves the current position to the next node. The next node is determined 
	 * by a depth-first search from the left to the right. If no node is left, 
	 * <tt>null</tt> is returned.
	 */
	protected void nextNode() {
		assert current != null && start != null : "current or start is/are null";
		
		if (current.getFirstChild() != null) {
			goDown();
		} else {
			goRight();
		}
	}
	
	/**
	 * Moves the current position to the right at the first occuring chance. 
	 * This may be the next (right) sibling or the next subtrie, for example.
	 */
	protected void goRight() {
		assert current != null && start != null : "current or start is/are null";
		
		while (current.getNextSibling() == null && current != start) { // don't go higher up than start
			goUp();
		}
		
		if (current != start) {
			current = current.getNextSibling(); // go (directly) right
		} else { // current == start
			current = null; // we treat start as root (and a root has no siblings), me march into the void
		}
	}
	
	/**
	 * Moves the current position one step down, i.e., to its first child.
	 */
	protected void goDown() {
		assert current != null : "current is null";
		current = current.getFirstChild();
	}
	
	/**
	 * Moves the current position one step up, i.e., to its parent.
	 */
	protected void goUp() {
		assert current != null : "current is null";
		current = current.getParent();
	}
	
	/**
	 * Resets this iterator with the specified {@link Trie} as new start node. 
	 * This method can be used to avoid creating a new {@link TrieIteratorBase} 
	 * object.<br/>
	 * <br/>
	 * Obviously, this method should only be used if the iterator has finished, 
	 * i.e., if a call to {@link Iterator#hasNext()} will return <tt>false</tt>.
	 * 
	 * @param newStart The new start node from where the iteration begins.
	 */
	protected void resetTo(Trie<T> newStart) {
		//assert current == null : "Try to reset iterator in unfinished state"; // disable as it starts in an unfinished state
		start = newStart;
		current = start;
	}
}
