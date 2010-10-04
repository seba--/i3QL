package saere.database.index;

import saere.Term;

// FIXME Seems to be buggy (iterators won't work correctly with it).
public class ComplexTermInserter extends TermInserter {
	
	@Override
	public Trie insert(TermStack stack, Term term) {
		assert root != null : "root is null"; // why was that again?
		
		if (current.isRoot()) { 
			
			// create the very first node
			if (current.getFirstChild() == null) { 
				current.setFirstChild(new Trie(Label.makeLabel(stack.asArray()), current));
			}
			
			current = current.getFirstChild(); // set current for next insert()
			return insert(stack, term);

		} else { // !root
			
			// how "much" does the current label matches with the current stack (state)
			int match = match(current.getLabel(), stack);
			int labelLength = current.labelLength();
			
			assert labelLength >= match : "label length is smaller than match";
			
			if (match == labelLength) { // complete match --> insert here or as child
				
				if (match < stack.size()) {
					
					// insert as child
					stack.pop(match);
					if (current.getFirstChild() == null) {
						current.setFirstChild(new Trie(Label.makeLabel(stack.asArray()), current));
					}
					current = current.getFirstChild();
					return insert(stack, term);
					
				} else {
					
					// insert here
					current.addTerm(term);
					return current;
				}
				
			} else if (match > 0) { // partial match
				
				
				
				if (stack.size() < labelLength) { 
				
					/*
					 * E.g., insert [a, b](1) into [a, b, c](2):
					 * Set label of (2) to [a, b].
					 * Create mediator(3) with label [c] and parent (2).
					 * XXX and so on...
					 */
					
					Label[] newLabels = current.getLabel().split(match - 1);
					current.setLabel(newLabels[0]);
					Trie mediator = new Trie(newLabels[1], current);
					mediator.setFirstChild(current.getFirstChild());
					if (mediator.getFirstChild() != null)
						mediator.getFirstChild().setParent(mediator);
					mediator.setTermList(current.getTermList());
					mediator.setNextSibling(current.getNextSibling()); 
					current.setNextSibling(null);
					Trie sibling = mediator.getNextSibling();
					while (sibling != null) { // and set new parent for all of them!
						sibling.setParent(current);
						sibling = sibling.getNextSibling();
					}
					current.setTermList(null);
					current.setFirstChild(mediator);
					current.addTerm(term);
					return current;
						
				} else { // stack.size() > labelLength && match != labelLength
					
					/*
					 * E.g., insert [f, a, b, c](1) into [f, a, x](2):
					 * Set label of (2) to [f, a].
					 * Create mediator(3) with label [x] and parent (2).
					 * Set first child of (1) as first child of (3).
					 * Make (3) the first child of (1).
					 * Set (1) as the parent of (3).
					 * Set (4) as next sibling of (3).
					 * Set the next sibling of (1) as the next silbing of (3) (and set new parent (1) for all).
					 * Set the next sibling of (1) to null.
					 * Continue insertion with popped stack at mediator...
					 */
					
					Label[] newLabels = current.getLabel().split(match - 1); // label should never ever (!root) be null, right?
					current.setLabel(newLabels[0]);
					
					// mediator
					Trie mediator = new Trie(newLabels[1], current);
					mediator.setFirstChild(current.getFirstChild());
					if (mediator.getFirstChild() != null)
						mediator.getFirstChild().setParent(mediator);
					mediator.setTermList(current.getTermList());
					current.setTermList(null);
					current.setFirstChild(mediator);
					mediator.setNextSibling(current.getNextSibling()); 
					current.setNextSibling(null);
					Trie sibling = mediator.getNextSibling();
					while (sibling != null) { // and set new parent for all of them!
						sibling.setParent(current);
						sibling = sibling.getNextSibling();
					}
					
					// continue insertion with mediator (which should get a new sibling)
					stack.pop(match);
					current = mediator;
					return insert(stack, term);
				}
				
			} else { // no match
				
				if (current.getNextSibling() == null) {
					
					// make trie with the rest of the stack as label
					current.setNextSibling(new Trie(Label.makeLabel(stack.asArray()), current.getParent()));
				}
				
				current = current.getNextSibling();
				return insert(stack, term);
			}
		}
	}
	
	private int match(Label label, TermStack stack) {
		return label.match(stack.asArray());
	}

}
