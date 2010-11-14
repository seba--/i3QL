package saere.database.index;

import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedList;

public class FullSimpleQueryIterator extends TermIterator {

	private static final VariableLabel FREE_VARIABLE = VariableLabel.VariableLabel();
	
	private final TrieBuilder builder;
	private final LabelStack stack;
	private final HashMap<Integer, VariableIterator> varIters; // Integer stands the position of a variable in the stack
	private final Deque<Trie> lastIterateds; 
	
	public FullSimpleQueryIterator(TrieBuilder builder, Trie start, LabelStack stack) {
		this.builder = builder;
		this.start = current = start;
		this.stack = stack;
		varIters = new HashMap<Integer, VariableIterator>();
		lastIterateds = new LinkedList<Trie>();
		findNext();
	}

	@Override
	protected void findNext() {
		next = null;
		
		if (list != null) {
			// List processing mode
			next = list.term();
			list = list.next();
		} else {
			// Normal mode, as long as we have nodes left and have no next term (list)
			while (current != null && next == null) {
				
				if (current.getParent() == null) {
					current = current.getFirstChild();
					continue;
				}
				
				if (match()) {
					if (stack.size() == 1 && current.stores()) {
						list = current.getTerms();
						next = list.term();
						list = list.next();
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
	}
	
	@Override
	protected void goUp() {
		if (stack.peek() == FREE_VARIABLE) {
			VariableIterator iter = varIters.get(stack.size());
			if (iter != null) {
				current = iter.getStart();
				removeIterator();
				lastIterateds.poll();
				stack.back();
			} else {
				stack.back();
				super.goUp();
			}
			
		} else {
			stack.back();
			super.goUp();
		}
	}
	
	@Override
	protected void goDown() {
		Trie lastIterated = lastIterateds.peek();
		if (stack.peek() == FREE_VARIABLE && lastIterated != current && current.getLabel().arity() > 0) {
			VariableIterator iter = getIterator();
			current = iter.next();
			lastIterateds.push(current);
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
			if (stack.peek() == FREE_VARIABLE && stack.size() > 1) {
				VariableIterator iter = varIters.get(stack.size());
				if (iter != null && iter.hasNext()) {
					nextSibling = iter.next();
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
	
	/**
	 * Creates/gets a variable iterator for the current context.
	 * 
	 * @return An variable iterator for the current context.
	 */
	private VariableIterator getIterator() {
		VariableIterator varIter = varIters.get(stack.size());
		if (varIter == null) {
			varIter = new VariableIterator(current);
			varIters.put(stack.size(), varIter);
		}
		return varIter;
	}
	
	/**
	 * Removes the variable iterator for the current context.
	 */
	private void removeIterator() {
		varIters.remove(stack.size());
	}
	
	@Override
	protected void resetTo(Trie newStart) {
		throw new UnsupportedOperationException();
	}
	
	private boolean match() {
		assert current.getLabel() != null : "Cannot match a root";
		return current.getLabel().sameAs(stack.peek()) || stack.peek() == FREE_VARIABLE;
	}
}
