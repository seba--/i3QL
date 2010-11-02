package saere.database.index.unique;

import java.util.Iterator;

import saere.Atom;
import saere.Term;
import saere.database.index.InsertStack;
import saere.database.index.QueryStack;
import saere.database.index.TermFlattener;

/**
 * The {@link UniqueTrieBuilder} uses for each element of a flattend term 
 * representation exactly one {@link UniqueTrie} node. That is, if the length of a 
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
public final class UniqueTrieBuilder {
	
	private static final TermFlattener flattener = new UniqueTermFlattener();
	
	private UniqueTrie current;
		
	public UniqueTrie insert(Term term, UniqueTrie start) {
		InsertStack stack = new InsertStack(flattener.flattenForInsertion(term));
		current = start;
		
		UniqueTrie insertionNode = null; // the trie node where the specified term will be added
		while (insertionNode == null) {
			Atom first = stack.peek();
			
			UniqueTrie child = current.getChild(first);
			if (child != null) { // Match...
				stack.pop();
				current = child;
				
				// End point reached?
				if (stack.size() == 0) {
					current.addTerm(term);
					insertionNode = current;
				}
			} else { // No such child...
				child = new UniqueTrie(first, current); // Constructor cares for everything
				//stack.pop();
				//current = child;
			}
		}
		
		return insertionNode;
	}

	public boolean remove(Term term, UniqueTrie start) {
		throw new UnsupportedOperationException("Not yet implemented");
	}

	public Iterator<UniqueTrie> nodeIterator(UniqueTrie start) {
		return new UniqueTrieNodeIterator(start);
	}
	
	public Iterator<Term> iterator(UniqueTrie start) {
		return new UniqueTermIterator(start);
	}
	
	public Iterator<Term> iterator(UniqueTrie start, Term[] query) {
		return new UniqueQueryTrieIterator(start, new QueryStack(flattener.flattenForQuery(query)));
	}
	
	@Override
	public String toString() {
		return flattener.toString() + "-simple";
	}
}
