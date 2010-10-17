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
 * @version 0.51, 10/17/2010
 */
public class ComplexTermInserter extends TrieBuilder {
	
	@Override
	public Trie insert(InsertStack stack, Term term) {
		
		Trie insertionNode = null; // the trie node where the specified term will be added
		int match;
		while (insertionNode == null) {
			
			if (current.isRoot()) { 
				
				if (current.getFirstChild() == null) { // create the very first node and add term directly
					current.setFirstChild(new Trie(new CompoundLabel(stack.asArray()), current));
					current.getFirstChild().addTerm(term);
					insertionNode = current.getFirstChild();
				} else { // move to child
					current = current.getFirstChild(); // set current for next insert()
				}

			} else { // !root
				
				// how "much" does the current label matches with the current stack (state)
				match = Matcher.match(current.getLabel(), stack);
				
				assert current.getLabelLength() >= match : "label length is smaller than match";
				
				if (match == current.getLabelLength()) { // complete match -> insert here or as child
					
					if (match < stack.size()) {
						
						// insert as child
						stack.pop(match);
						if (current.getFirstChild() == null) { // create first child and add term directly
							current.setFirstChild(new Trie(new CompoundLabel(stack.asArray()), current));
							current.getFirstChild().addTerm(term);
							insertionNode = current.getFirstChild();
						} else { // move to child
							current = current.getFirstChild();
						}
						
					} else { // match == stack.size()
						
						// insert here
						current.addTerm(term);
						insertionNode = current;
					}
					
				} else if (match > 0) { // partial match
					
					// split...
					Label newLabel = current.getLabel().split(match - 1);
					Trie mediator = new Trie(newLabel, current);
					
					// insert mediator
					if (current.getFirstChild() != null) {
						mediator.setFirstChild(current.getFirstChild());
						mediator.getFirstChild().setParent(mediator);
						
						// set mediator as parent for all children
						Trie child = mediator.getFirstChild();
						while (child != null) {
							child.setParent(mediator);
							child = child.getNextSibling();
						}
					}
					current.setFirstChild(mediator);
					
					// send term list to mediator
					mediator.setTermList(current.getTermList());
					current.setTermList(null);
					
					// and go on...
					stack.pop(match);
					current = mediator;
					
				} else { // no match
					if (current.getNextSibling() == null) { // create first next sibling and add term directly
						current.setNextSibling(new Trie(new CompoundLabel(stack.asArray()), current.getParent()));
						current.getNextSibling().addTerm(term);
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
	public Iterator<Term> iterator(Trie start, Term[] query) {
		return new ComplexTrieTermIterator(start, query);
	}
}
