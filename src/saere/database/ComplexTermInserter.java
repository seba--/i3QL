package saere.database;

import saere.Term;

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
			
			if (match == labelLength) { // complete match
				
				// insert here
				current.addTerm(term);
				return current;
				
			} else if (match > 0) { // partial match
				
				if (stack.size() < labelLength) { 
				
					/*
					 * E.g., insert [a, b](1) into [a, b, c](2): Split into 
					 * [a, b](3) and [c](4). Set the first child of (2) as 
					 * first child to (4) and add (4) as first child to (3). 
					 * Add the term from (1) to (3).
					 */
					
					Label[] newLabels = current.getLabel().split(match - 1);
					current.setLabel(newLabels[0]);
					Trie mediator = new Trie(newLabels[1], current);
					mediator.setFirstChild(current.getFirstChild());
					if (mediator.getFirstChild() != null)
						mediator.getFirstChild().setParent(mediator);
					mediator.setTermList(current.getTermList());
					current.setTermList(null);
					current.setFirstChild(mediator);
					current.addTerm(term);
					return current;
						
				} else {
					
					/*
					 * E.g., insert [f, a, x](1) into [f, a, b, c](2): Split 
					 * (2) into [f, a](3) and [b, c](4). Set the first child of 
					 * (2) as first child of (3). Set (3) as first child of (2).
					 * Set (4) as next sibling of (3).
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
