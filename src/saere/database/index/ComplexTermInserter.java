package saere.database.index;

import saere.Term;

/**
 * A {@link TermInserter} for <i>Complex Term Insertion</i> (CTI). 
 * {@link Trie}s created with this {@link TermInserter} have as much 
 * {@link Trie} nodes as required, but not more. However, the insertion process 
 * is a bit more <i>complex</i> (and may take more time).
 * 
 * @author David Sullivan
 * @version 0.3, 10/6/2010
 */
public class ComplexTermInserter extends TermInserter {
	
	@Override
	public Trie insert(TermStack stack, Term term) {
		//assert root != null : "root is null"; // why was that again?
		
		if (current.isRoot()) { 
			
			if (current.getFirstChild() == null) { // create the very first node and add term directly
				current.setFirstChild(new Trie(Label.makeLabel(stack.asArray()), current));
				current.getFirstChild().addTerm(term);
				return current.getFirstChild();
			} else { // move to child
				current = current.getFirstChild(); // set current for next insert()
				return insert(stack, term);
			}

		} else { // !root
			
			// how "much" does the current label matches with the current stack (state)
			int match = Matcher.match(current.getLabel(), stack);
			
			assert current.labelLength() >= match : "label length is smaller than match";
			
			if (match == current.labelLength()) { // complete match -> insert here or as child
				
				if (match < stack.size()) {
					
					// insert as child
					stack.pop(match);
					if (current.getFirstChild() == null) { // create first child and add term directly
						current.setFirstChild(new Trie(Label.makeLabel(stack.asArray()), current));
						current.getFirstChild().addTerm(term);
						return current.getFirstChild();
					} else { // move to child
						current = current.getFirstChild();
						return insert(stack, term);
					}
					
				} else { // match == stack.size()
					
					// insert here
					current.addTerm(term);
					return current;
				}
				
			} else if (match > 0) { // partial match
				
				// split...
				Label[] newLabels = current.getLabel().split(match - 1);
				current.setLabel(newLabels[0]);
				Trie mediator = new Trie(newLabels[1], current);
				
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
				return insert(stack, term);
				
			} else { // no match
				if (current.getNextSibling() == null) { // create first next sibling and add term directly
					current.setNextSibling(new Trie(Label.makeLabel(stack.asArray()), current.getParent()));
					current.getNextSibling().addTerm(term);
					return current.getNextSibling();
				} else { // move to next sibling
					current = current.getNextSibling();
					return insert(stack, term);
				}
			}
		}
	}
}
