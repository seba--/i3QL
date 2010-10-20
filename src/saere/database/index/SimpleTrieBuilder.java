package saere.database.index;

import java.util.Iterator;

import saere.Atom;
import saere.Term;

/**
 * The {@link SimpleTrieBuilder} uses for each element of a flattend term 
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
public final class SimpleTrieBuilder extends TrieBuilder<Atom> {
		
	@Override
	public Trie<Atom> insert(Term term, Trie<Atom> start) {
		InsertStack stack = new InsertStack(flattener.flattenForInsertion(term));
		current = start;
		
		Trie<Atom> insertionNode = null; // the trie node where the specified term will be added
		while (insertionNode == null) {
			Atom first = stack.peek();
			
			if (current.getParent() == null) { // root 
				
				if (current.getFirstChild() == null) // create the very first node
					current.setFirstChild(new Trie<Atom>(current, first));
				current = current.getFirstChild();
				
			} else if (Matcher.match(current.getLabel(), first)) { // the labels match
				
				// remove the first atom/variable from stack
				stack.pop();
				
				// this must be the insertion node (and it already existed)
				if (stack.size() == 0) {
					
					if (current.getTerm() == null) { // The standard case, a node that stores a single term
						current.setTerm(term);
					} else { // A node that has to store multiple terms (collision)
						
						if (!current.hasTermList()) {
							
							// The new node has to replace the old one...
							TrieWithCollision<Atom> trie = new TrieWithCollision<Atom>(current.getParent(), current.getLabel());
							replace(current, trie);
							current = trie;
						} else { // The current node stores already more than one term (very rare)
							current.setTermList(new TermList(term));
						}	
					}
					
					insertionNode = current;
				
				} else { // more to process
					
					if (current.getFirstChild() == null) // add to own subtrie
						current.setFirstChild(new Trie<Atom>(current, stack.peek()));
					current = current.getFirstChild();
				}

			} else { // !root && !same
				
				if (current.getNextSibling() == null) // add to (a) sibling subtrie
					current.setNextSibling(new Trie<Atom>(current.getParent(), first));
				current = current.getNextSibling();
			}
		}
		
		return insertionNode;
	}

	@Override
	public boolean remove(Term term, Trie<Atom> start) {
		throw new UnsupportedOperationException("Not yet implemented");
	}

	@Override
	public Iterator<Term> iterator(Trie<Atom> start, Term[] query) {
		return new SimpleTrieTermIterator(start, new QueryStack(flattener.flattenForQuery(query)));
	}
	
	@Override
	public String toString() {
		return flattener.toString() + "-simple";
	}
}
