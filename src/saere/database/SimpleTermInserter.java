package saere.database;

import saere.Term;

/**
 * The {@link SimpleTermInserter} uses for each element of a flattend term 
 * representation exactly one {@link Trie} node. That is, if the length of a 
 * flattened term repesenation is seven, the term will be found at node level 
 * seven (not counting the root).<br/>
 * <br/>
 * For example, the representation <code>[f, along, b, c]</code> will be 
 * stored/retrieved with four nodes with the labels <tt>f</tt>, <tt>along</tt>, 
 * <tt>b</tt> and <tt>c</tt> whereas the last node with label <tt>c</tt> at 
 * level four stores the term.
 * 
 * @author David Sullivan
 * @version 0.1, 9/22/2010
 */
public class SimpleTermInserter extends TermInserter {
	
	@Override
	public Trie insert(TermStack stack, Term term) {
		assert root != null : "root is null";
		
		Term first = stack.peek();
		
		assert first != null && (first.isIntegerAtom() || first.isStringAtom() || first.isVariable()) : "Invalid first";
		
		if (current.isRoot()) { 
			
			// create the very first node
			if (current.getFirstChild() == null) { 
				current.setFirstChild(new Trie(Label.makeLabel(first), current));
			}
			
			current = current.getFirstChild(); // set current for next insert()
			return insert(stack, term);

		} else if (current.getLabel().match(first)) { // the labels match
			
			// remove the first atom/variable from stack
			stack.pop();
			
			// this must be the insertion node
			if (stack.size() == 0) {
				current.addTerm(term);
				return current;
			
			} else { // more to process
				
				// add to own subtrie
				if (current.getFirstChild() == null) {
					current.setFirstChild(new Trie(Label.makeLabel(stack.peek()), current));
				}
				
				current = current.getFirstChild(); // set current for next insert()
				return insert(stack, term);		
			}

		} else { // !root && !same
			
			// add to (a) sibling subtrie
			if (current.getNextSibling() == null) {
				current.setNextSibling(new Trie(Label.makeLabel(first), current.getParent()));
			}
			
			current = current.getNextSibling(); // move right for next insert()
			return insert(stack, term);
		}
	}
}
