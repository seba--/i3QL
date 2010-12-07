package saere.database.index;

import java.util.Iterator;



/**
 * Abstract base class for {@link Trie} iterators that contains common methods.
 * 
 * @author David Sullivan
 * @version 0.3, 11/9/2010
 */
public abstract class IteratorBase {
	
	/**
	 * The node from where the iteration begins. From the point of view of the
	 * iterator it works as a root. Only the <tt>start</tt> node and its 
	 * descendants are considered to be iterated.
	 */
	protected Trie start;
	
	/**
	 * The current position.
	 */
	protected Trie current;
	
	/**
	 * Creates a new {@link IteratorBase} and sets the <tt>current</tt> 
	 * and <tt>start</tt> fields.
	 * 
	 * @param start The start node for the iteration.
	 */
	protected IteratorBase(Trie start) {
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
		
		while (current.getNextSibling() == null && current != start) { // Don't go higher up than start
			goUp();
		}
		
		if (current != start) {
			current = current.getNextSibling(); // Go (directly) right
		} else {
			current = null; // We treat start as root (and a root has no siblings)
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
	 * This method can be used to avoid creating a new object.<br/>
	 * <br/>
	 * Obviously, this method should only be used if the iterator has finished, 
	 * i.e., if a call to {@link Iterator#hasNext()} will return <tt>false</tt>.
	 * 
	 * @param newStart The new start node from where the iteration begins.
	 */
	public void resetTo(Trie newStart) {
		assert current == null : "Tried to reset iterator in unfinished state";
		start = newStart;
		current = start;
	}
}
