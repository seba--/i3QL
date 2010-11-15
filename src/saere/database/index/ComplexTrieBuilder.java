package saere.database.index;

import java.util.Iterator;

import saere.Term;

/**
 * A {@link TrieBuilder} for <i>Complex Term Insertion</i> (CTI). 
 * {@link Trie}s created with this {@link TrieBuilder} have as much 
 * {@link Trie} nodes as required, but not more. However, the insertion process 
 * is a bit more <i>complex</i> (and may take more time).
 * 
 * @author David Sullivan
 * @version 0.6, 10/18/2010
 */
public class ComplexTrieBuilder extends TrieBuilder {
	
	public ComplexTrieBuilder(TermFlattener flattener, int mapThreshold) {
		super(flattener, mapThreshold);
	}
	
	@Override
	public Trie insert(Term term, Trie start) {
		current = start;
		stack = flattener.flatten(term);
		Trie insertionNode = null; // the trie node where the specified term will be added
		int match;
		while (insertionNode == null) {
			
			if (current.getParent() == null) { // root 
				
				if (current.getFirstChild() == null) { // create the very first node and add term directly
					current.setFirstChild(new StorageTrie(current, makeComplexLabel(), term));
					insertionNode = current.getFirstChild();
				} else { // move to child
					current = current.getFirstChild(); // set current for next insert()
				}

			} else { // !root
				
				// how "much" does the current label matches with the current stack (state)
				match = match();
				
				if (match == current.getLabel().length()) { // complete match -> insert here or as child
					
					if (match < stack.size()) {
						
						// insert as child
						stack.pop(match);
						if (current.getFirstChild() == null) { // create first child and add term directly
							current.setFirstChild(new StorageTrie(current, makeComplexLabel(), term));
							insertionNode = current.getFirstChild();
						} else { // move to child
							current = current.getFirstChild();
						}
						
					} else { // match == stack.size(), insert here...
						
						if (current.stores()) { // Already a storing node
							current.addTerm(term);
						} else {
							StorageTrie storageTrie = new StorageTrie(current.getParent(), makeComplexLabel(), term);
							replace(current, storageTrie);
							current = storageTrie;
						}
						
						insertionNode = current;
					}
					
				} else if (match > 0) { // partial match
					
					// split...
					Label newLabel = current.getLabel().split(match - 1);
					Trie mediator;
					TermList termList = current.getTerms();
					current.setTerms(null);
					
					if (current.hashes()) {
						mediator = new StorageHashTrie(current, newLabel, current.getLastChild(), null);
					} else {
						mediator = new StorageTrie(current, newLabel, null);
					}
					mediator.setTerms(termList);
					
					// insert mediator
					if (current.getFirstChild() != null) {
						mediator.setFirstChild(current.getFirstChild());
						
						if (current.hashes()) {
							mediator.setMap(current.getMap());
						}
						
						// set mediator as parent for all children
						Trie child = mediator.getFirstChild();
						while (child != null) {
							child.setParent(mediator);
							child = child.getNextSibling();
						}
					}
					current.setFirstChild(mediator);
					if (current.hashes()) {
						StorageTrie newCurrent = new StorageTrie(current.getParent(), current.getLabel(), null);
						replace(current, newCurrent);
						current = newCurrent;
					}
					
					// and go on...
					stack.pop(match);
					current = mediator;
					
				} else { // no match
					
					// Check wether we can have a full match
					if (current.getParent().getFirstChild() == current) {
						Trie searched = getChild(current, stack.peek());
						if (searched != null) {
							
						}
					}
					
					if (current.getNextSibling() == null) { // create first next sibling and add term directly
						current.setNextSibling(new StorageTrie(current.getParent(),makeComplexLabel(), term));
						insertionNode = current.getNextSibling();
					} else { // move to next sibling
						current = current.getNextSibling();
					}
				}
			}
			
		}
		
		return insertionNode;
	}

	@Override
	public boolean remove(Term term, Trie start) {
		throw new UnsupportedOperationException("Not yet implemented");
	}

	@Override
	public Iterator<Term> iterator(Trie start, Term query) {
		if (flattener instanceof ShallowFlattener) {
			//return new ShallowComplexQueryIterator(this, start, flattener.flatten(query));
			throw new UnsupportedOperationException("Not yet implemented");
		} else if (flattener instanceof FullFlattener) {
			throw new UnsupportedOperationException("Not implemented");
		} else {
			throw new UnsupportedOperationException("Unexpected term flattener");
		}
	}
	
	@Override
	public String toString() {
		return flattener.toString() + "-complex";
	}
	
	private int match() {
		Label[] currentLabels = current.getLabel().labels();
		Label[] stackLabels = stack.array();
		int offset = stack.position();
		int min = Math.min(currentLabels.length, stack.size());
		int i;
		for (i = 0; i < min; i++) {
			if (!currentLabels[i].sameAs(stackLabels[i + offset]))
				break;
		}
		
		return i;
	}
	
	/**
	 * Creates a complex label with the current stack state.
	 * 
	 * @return A complex label based on the current stack state.
	 */
	private ComplexLabel makeComplexLabel() {
		Label[] stackLabels = stack.array();
		int offset = stack.position();
		Label[] labels = new Label[stack.size()];
		for (int i = 0; i < labels.length; i++) {
			labels[i] = stackLabels[i + offset];
		}
		return ComplexLabel.ComplexLabel(labels);
	}
	
	private static Trie getChildWithPrefix(Trie parent, Label prefix) {
		Trie child = parent.getFirstChild();
		while (child != null) {
			if (prefix.sameAs(child.getLabel().labels()[0])) {
				return child;
			}
		}
		
		return null;
	}
	
	private void splitCurrent() {
		
	}
}
