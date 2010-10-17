package saere.database.index;

import java.util.Iterator;

import saere.Atom;
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
 * @version 0.31, 10/17/2010
 */
public class SimpleTermInserter extends TrieBuilder {
	
	@Override
	public Trie insert(InsertStack stack, Term term) {
		
		Trie insertionNode = null; // the trie node where the specified term will be added
		while (insertionNode == null) {
			Atom first = stack.peek();
			
			if (current.isRoot()) { 
				
				if (current.getFirstChild() == null) // create the very first node
					current.setFirstChild(new Trie(new AtomLabel(first), current));
				current = current.getFirstChild();
				
			} else if (Matcher.match(current.getLabel(), first)) { // the labels match
				
				// remove the first atom/variable from stack
				stack.pop();
				
				// this must be the insertion node (and it already existed)
				if (stack.size() == 0) {
					
					current.addTerm(term);
					insertionNode = current;
				
				} else { // more to process
					
					if (current.getFirstChild() == null) // add to own subtrie
						current.setFirstChild(new Trie(new AtomLabel(stack.peek()), current));
					current = current.getFirstChild();
				}

			} else { // !root && !same
				
				if (current.getNextSibling() == null) // add to (a) sibling subtrie
					current.setNextSibling(new Trie(new AtomLabel(stack.peek()), current.getParent()));
				current = current.getNextSibling();
			}
		}
		
		return insertionNode;
	}

	@Override
	public Iterator<Term> iterator(Trie start, Term[] query) {
		return new SimpleTrieTermIterator(start, query);
	}
}
