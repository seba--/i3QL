package saere.database.index.map;

import java.util.Iterator;

import saere.Atom;
import saere.Term;
import saere.database.index.InsertStack;
import saere.database.index.QueryStack;
import saere.database.index.ShallowFlattener;

/**
 * The {@link MapTrieBuilder} uses for each element of a flattend term 
 * representation exactly one {@link MapTrie} node. That is, if the length of a 
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
public final class MapTrieBuilder {
	
	private static final ShallowFlattener flattener = new ShallowFlattener();
	
	private MapTrie current;
		
	public MapTrie insert(Term term, MapTrie start) {
		InsertStack stack = new InsertStack(flattener.flatten(term));
		current = start;
		
		MapTrie insertionNode = null; // the trie node where the specified term will be added
		while (insertionNode == null) {
			Atom first = stack.peek();
			
			MapTrie child = current.getChild(first);
			if (child != null) { // Match...
				stack.pop();
				current = child;
				
				// End point reached?
				if (stack.size() == 0) {
					current.addTerm(term);
					insertionNode = current;
				}
			} else { // No such child...
				child = new MapTrie(first, current); // Constructor cares for everything
				//stack.pop();
				//current = child;
			}
		}
		
		return insertionNode;
	}

	public boolean remove(Term term, MapTrie start) {
		throw new UnsupportedOperationException("Not yet implemented");
	}

	public Iterator<MapTrie> nodeIterator(MapTrie start) {
		return new MapNodeIterator(start);
	}
	
	public Iterator<Term> iterator(MapTrie start) {
		return new MapTermIterator(start);
	}
	
	public Iterator<Term> iterator(MapTrie start, Term[] query) {
		return new MapQueryIterator(start, new QueryStack(flattener.flattenForQuery(query)));
	}
	
	@Override
	public String toString() {
		return flattener.toString() + "-simple";
	}
}
