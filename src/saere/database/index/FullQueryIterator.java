package saere.database.index;



/**
 * A query iterator for full simple tries. The {@link #findNext()} is exactly 
 * the same as {@link ShallowQueryIterator#findNext()} but the 
 * {@link #goUp()}, {@link #goDown()}, and {@link #goRight()} implementations 
 * differ.
 * 
 * @author David Sullivan
 * @version 0.2, 11/16/2010
 */
public class FullQueryIterator extends TermIterator {

	private static final VariableLabel FREE_VARIABLE = VariableLabel.VariableLabel();
	
	private final TrieBuilder builder;
	private final LabelStack stack;
	
	// The variable iterators (one for each free variable). The index stands for the position of a variable in the stack.
	private final VariableIterator[] varIters;
	
	public FullQueryIterator(TrieBuilder builder, Trie start, LabelStack stack) {
		this.builder = builder;
		this.start = current = start;
		this.stack = stack;
		
		// + 1 because if the stack is empty, the position is the array length
		varIters = new VariableIterator[stack.length() + 1];
		
		// Assign variable iterators to the index positions of free variables.
		// Hence, we create the objects for variable iterators only once.
		Label[] labels = stack.array();
		for (int i = 0; i < labels.length; i++) {
			if (labels[i] == FREE_VARIABLE) {
				varIters[i] = new VariableIterator();
			}
		}
		
		findNext();
	}

	@Override
	protected void findNext() {
		next = null;
		
		// Normal mode, as long as we have nodes left and have no next term (list)
		while (current != null && next == null) {
			
			if (current.getParent() == null) {
				current = current.getFirstChild();
				continue;
			}
			
			if (match()) {
				if (stack.size() == 1/* && current.isSingleStorageLeaf()*/) {
					next = current.getTerm();
				}
				nextNode();		
			} else {
				
				/*
				 * No match, check if there can be a match at all.
				 * If so, jump directly to the matching sibling.
				 * 
				 * However, we must also check if the searched node is a 
				 * right sibling of the current. Otherwise we might jump 
				 * back to a node that was already processed by the 
				 * iterator.
				 * 
				 * As a first child is always checked first, it can never 
				 * be the node we'll want to jump back.
				 */
				
				// Only if current is the first child! That is, we check only when we go down in the trie.
				if (current.getParent().getFirstChild() == current) {
					Trie searched = builder.getChild(current.getParent(), stack.peek());
					if (searched != null) {
						current = searched;
					} else {
						goUp();
						goRight();
					}
				} else {
					goUp();
					goRight();
				}
			}
		}
	}
	
	@Override
	protected void goUp() {
		
		// Checks wether we have a variable iterator for the current stack top 
		// (i.e., if the stack top is a free variable).
		VariableIterator varIter = varIters[stack.position()];
		if (stack.peek() == FREE_VARIABLE && varIters[stack.position()].last() == current) { // XXX Find a way to check that this is really our variable iterator
			current = varIter.root();
			stack.back();
		} else {
			stack.back();
			super.goUp();
		}
	}
	
	@Override
	protected void goDown() {
		// Check wether we are at a trie node that was not iterated by a variable iterator
		// (a value not null does also mean, that the stack top is a free variable).
		// Also check if the current trie's label is not a functor.
		// If all this applies we require a variable iterator...
		
		VariableIterator varIter = varIters[stack.position()];
		if (stack.peek() == FREE_VARIABLE && varIters[stack.position()].last() != current) {
			varIter = varIters[stack.position()];
			varIter.resetTo(current);
			current = varIter.next();
		} else {
			stack.pop();
			super.goDown();
		}
	}
	
	@Override
	protected void goRight() {
		Trie nextSibling = null;
		while (current != null && current != start && nextSibling == null) { // don't go higher up than start
			
			nextSibling = current.getNextSibling();
			
			// Check wether the current node has a sibling w.r.t its variable iterator
			// But only if the free variable on stack was not matched with the last node and we just returned a term
			if (stack.peek() == FREE_VARIABLE && stack.size() > 1) { // && arity > 0
				VariableIterator varIter = varIters[stack.position()];
				if (varIter.hasNext()) {
					nextSibling = varIter.next();
				}
			}
			
			if (nextSibling == null) {
				goUp();
			}
		}
		
		if (current == null)
			return;
		
		if (current != start) {
			current = nextSibling; // go (directly) right
		} else { // current == start
			current = null; // we treat start as root (and a root has no siblings), me march into the void
		}
	}
	
	@Override
	public void resetTo(Trie newStart) {
		throw new UnsupportedOperationException("Cannot reset a full simple query iterator");
	}
	
	private boolean match() {
		assert current.getLabel() != null : "Cannot match a root";
		return current.getLabel().sameAs(stack.peek()) || stack.peek() == FREE_VARIABLE;
	}
}
