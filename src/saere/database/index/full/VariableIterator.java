package saere.database.index.full;

import java.util.Iterator;
import java.util.NoSuchElementException;

import saere.Variable;
import saere.database.index.Matcher;

/**
 * A helper iterator for trie areas that are flattened compound terms which 
 * are covered by a single variable. The iterator returns trie nodes that are 
 * direct successors of such an area.
 * 
 * @author David Sullivan
 * @version 0.1, 11/3/2010
 */
public class VariableIterator extends FullIteratorBase implements Iterator<FullTrie> {
	
	private FullTrie next;
	private int stack; // NO! Different paths may have different stacks!
	private final Variable var; // the associated variable
	
	// The start node is always the atom/functor that matches the free variable.
	// It's not part of the iterated node set.
	public VariableIterator(FullTrie start, Variable var) {
		//super(start);
		next = null;
		stack = 0;
		
		// That's why we may need a root meta node (<root>)
		this.start = start.parent;
		current = this.start;
		
		this.var = var;
		
		findNext();
	}
	
	public Variable getVar() {
		return var;
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
		next = null;
		while (next == null && current != null) { // while next == null
			
			//System.out.println("current=" + current + " stack=" + stack);
			
			// Stack must be 0, but this node mustn't be our root one of the root's children
			if (stack == 0 && current.parent != start && current != start) {
				next = current;
				goRight(); // do so as if we couldn't go down, as if we'd reached a leaf
			} else {
				nextNode();
			}
		}
	}
	
	@Override
	public void goUp() {
		FullTrie up = current.parent;
		if (up != null && up.label == FullTermFlattener.CLOSE) {
			stack++; // Stack is already increased by 1 when we arrive
		} else if (current.label == FullTermFlattener.OPEN) {
			stack--;
		}
		
		super.goUp();
	}
	
	@Override
	public void goDown() {
		FullTrie down = current.firstChild;
		if (down != null && down.label == FullTermFlattener.OPEN) {
			stack++; // Stack is already increased by 1 when we arrive
		} else if (current.label == FullTermFlattener.CLOSE) {
			stack--;
		}
				
		super.goDown();
	}
	
	@Override
	public void goRight() {
		while (current.nextSibling == null && current != start) { // don't go higher up than start
			goUp();
		}
		
		if (current != start) {
			if (current.label == FullTermFlattener.OPEN) {
				stack--;
			} else if (current.label == FullTermFlattener.CLOSE) {
				//stack++;
			}
			current = current.nextSibling; // go (directly) right
		} else { // current == start
			current = null; // we treat start as root (and a root has no siblings), me march into the void
		}
	}

	@Override
	protected void resetTo(FullTrie newStart) {
		super.resetTo(newStart);
		stack = 0;
	}
	
	public FullTrie getStart() {
		return start;
	}
}
