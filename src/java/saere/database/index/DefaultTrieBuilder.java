package saere.database.index;

import java.util.Iterator;

import saere.Term;

/**
 * The {@link DefaultTrieBuilder} uses for each element of a flattend term 
 * representation exactly one {@link InnerNode} node. That is, if the length of a 
 * flattened term repesenation is seven, the term will be found at node level 
 * seven (not counting the root).<br/>
 * <br/>
 * For example, the representation <code>[f, along, b, c]</code> will be 
 * stored/retrieved with four nodes with the labels <tt>f</tt>, <tt>along</tt>, 
 * <tt>b</tt> and <tt>c</tt> whereas the last node with label <tt>c</tt> at 
 * level four stores the term.
 * 
 * @author David Sullivan
 * @version 0.31, 10/17/2010
 */
public final class DefaultTrieBuilder extends TrieBuilder {
	
	public DefaultTrieBuilder(TermFlattener flattener, int mapThreshold) {
		super(flattener, mapThreshold);
	}

	@Override
	public Trie insert(Term term, Trie start) {
		stack = flattener.flatten(term);
		current = start;
		
		Trie insertionNode = null;
		while (insertionNode == null) {
			Label peeked = stack.peek();
			
			/*
			 * We search for the child we need by label. There are mainly four cases:
			 * 1) No such child exists and we want to add the term here.
			 * -- We create a new storage trie with the desired label, add the term and return.
			 * 
			 * 2) No such child exists and we don't want to add the the term here.
			 * -- We create a new (normal) trie and continue the insertion process with it.
			 * 
			 * 3) A child with the label already exists and we want to add the term here.
			 * -- If the child can store terms (i.e., stores already one) we simply add the term.
			 * -- If the child doesn't store we replace the child with a new node that can store (either storage trie or storage hash trie).
			 * 
			 * 4) A child with the label already exists and we don't want to the term here.
			 * -- Continue insertion process with the child.
			 */
			Trie searched = getChild(current, peeked);
			if (searched == null) {
				// No such node with the label we require exists
				if (stack.size() == 1) {
					// We need a storage trie (leaf)
					if (noCollision) {
						searched = new SingleStorageLeaf(current, peeked, term);
					} else {
						searched = new MultiStorageLeaf(current, peeked, term);
					}
					addChild(current, searched);
					insertionNode = searched;
				} else {
					// We need a normal (inner) trie
					searched = new InnerNode(current, peeked);
					addChild(current, searched);
					
					// Go down
					current = searched;
					stack.pop();
				}
			} else {
				// A child with the label already exists
				
				// Example: 2x "load(reference,0)
				if (stack.size() == 1) {
					addTerm(searched, term);
					insertionNode = searched;
				} else {
					// We have a normal (inner) trie, go down
					current = searched;
					stack.pop();
				}
			}
		}
		
		return insertionNode;
	}

	@Override
	public Iterator<Term> iterator(Trie start, Term query) {
		if (flattener instanceof ShallowFlattener) {
			return new ShallowQueryIterator(this, start, flattener.flatten(query));
		} else if (flattener instanceof FullFlattener) {
			return new FullQueryIterator(this, start, flattener.flatten(query));
		} else {
			throw new UnsupportedOperationException("Unexpected term flattener");
		}
	}
	
	@Override
	public String toString() {
		return flattener.toString() + "-simple";
	}

	@Override
	public void remove(Term term, Trie root) {
		stack = flattener.flatten(term);
		current = root;
		
		while (true) { // XXX
			Label peeked = stack.peek();
			Trie searched = getChild(current, peeked);
			if (searched == null) {
				return;
			}
				
			if (stack.size() == 1) {
				if (searched.isSingleStorageLeaf() || searched.isMultiStorageLeaf()) {
					removeTerm(searched, term);
					trim(searched);
				}
				return;
			} else {
				current = searched;
				stack.pop();
			}
		}
	}
	
	private void trim(Trie start) {
		current = start;
		
		if (start.getTerms() == null && start.getTerm() == null) {
			current = start.getParent();
			removeChild(current, start);
			
			// As long as the current node has no child
			while (current.getParent().getFirstChild() == null && !current.isHashNode()) {
				
				removeChild(current.getParent(), current);
				current = current.getParent();
				
				// Don't remove the root
				if (current.isRoot()) {
					return;
				}
			}
		}
	}
}
