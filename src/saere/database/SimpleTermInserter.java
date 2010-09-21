package saere.database;

import saere.Term;

// TODO Finish...
public class SimpleTermInserter extends TermInserter {
	
	@Override
	public Trie insert(TermStack ts, Term t) {
		assert root != null : "root is null";
		
		Term first = ts.peek();
		
		assert first != null && (first.isIntegerAtom() || first.isStringAtom() || first.isVariable()) : "Invalid first";
		
		if (current.isRoot()) { 
			
			// create the very first node
			if (current.getFirstChild() == null) { 
				current.setFirstChild(new Trie(Label.makeLabel(first), current));
			}
			
			current = current.getFirstChild(); // set current for next insert()
			return insert(ts, t);

		} else if (current.getLabel().match(first)) { // the labels match
			
			// remove the first atom/variable from stack
			ts.pop();
			
			// this must be the insertion node
			if (ts.size() == 0) {
				if (current.getTermList() == null) { // no list so far?
					current.setTermList(new TermList(t)); // create new term list
				} else { // add to tail...
					TermList last = current.getTermList();
					TermList termList = last;
					while (termList.getNext() != null) {
						last = termList;
						termList = termList.getNext();
					}
					last.setNext(new TermList(t));
				}
				
				return current;
			}

			// add to own subtrie
			if (current.getFirstChild() == null) {
				current.setFirstChild(new Trie(Label.makeLabel(ts.peek()), current));
			}
			
			current = current.getFirstChild(); // set current for next insert()
			return insert(ts, t);
			
		} else { // !root && !same
			
			// add to (a) sibling subtrie
			if (current.getNextSibling() == null) {
				current.setNextSibling(new Trie(Label.makeLabel(first), current.getParent()));
			}
			
			current = current.getNextSibling(); // move right for next insert()
			return insert(ts, t);
		}
	}

}
