package saere.database.index.unique;

import java.util.Iterator;

import saere.Atom;
import saere.Term;
import saere.database.index.Matcher;
import saere.database.index.QueryStack;
import saere.database.index.TermFlattener;

/**
 * Trie term iterator that supports queries. A query is expressed by an array of
 * {@link Term}s. For term flattening the same {@link TermFlattener} as in the 
 * {@link UniqueTrie} class is used.<br/>
 * <br/>
 * <b>This iterator works only with {@link UniqueTrie}s that have been built with 
 * a {@link SimpleTermInserter}.</b>
 * 
 * @author David Sullivan
 * @version 0.1, 10/26/2010
 */
public class UniqueQueryTrieIterator extends UniqueTermIterator implements Iterator<Term> {
	
	private final UniqueTermIterator subiterator;
	
	private QueryStack stack;
	private boolean useSubiterator = false;
	
	/**
	 * Creates a new trie iterator that starts from <tt>start</tt> and 
	 * returns only terms that match the term represented by <tt>terms</tt>.
	 * 
	 * @param start The start trie, e.g., a functor.
	 * @param terms A query represented by an array of terms (atoms/variables).
	 */
	public UniqueQueryTrieIterator(UniqueTrie start, QueryStack stack) {
		this.start = start;
		current = start;
		
		// create the one and only instance of the subiterator that'll be used
		subiterator = new UniqueTermIterator(start);
		
		// break down terms to atoms/variables
		this.stack = stack;
		
		// don't skip the first node
		if (current.label != null && stack.size() == 1 && Matcher.match(current.label, stack.peek())) {
			useSubiterator = true;
			if (subiterator.hasNext()) {
				next = subiterator.next();
			} else {
				useSubiterator = false;
				nextNode();
			}
		} else {
			findNext(); 
		}
	}
	
	@Override
	protected void findNext() {
		//assert current != null && !current.isRoot() : "Cannot start iterator from root"; // otherwise we always have to check if the current node is the root and skip it
		
		next = null;
		
		// as long as we haven't found a new next and are not at an end point, i.e., current is null
		while (next == null && current != null) { // (or break)
			
			// Even if the begin the search with root's first child, we may still arrive here with goRight().
			// Also, if we begin the search with root's first child, this child is seen as 'root', i.e., is looked at if it had no siblings (this is how subiterators work).
			if (current.parent == null) {
				current = current.firstChild;
				continue;
			}
			
			boolean match = Matcher.match(current.label, stack.peek());
			
			// Check if we need a subiterator (if it isn't already active).
			// We'll need one if an end point is reached, i.e., if the only 
			// remaining element in the stack matches this label.
			// This is very often the case if the current node is a leaf (so we 
			// use a 'whole' iterator for a single leaf only).
			if (!useSubiterator && stack.size() == 1 && match) {
				useSubiterator = true;
				subiterator.resetTo(current);
			}
			
			if (useSubiterator) { // subiteration mode
				if (subiterator.hasNext()) {
					next = subiterator.next();
					break; // = return
				} else {
					useSubiterator = false;
					
					// we iterated the subtrie, go right...
					while (current.nextSibling == null && current != start) { // don't go higher up than start
						current = current.parent; // go up
						stack.back();
					}
					
					if (current != start) {
						current = current.nextSibling; // go (directly) right
					} else { // current == start
						current = null; // we treat start as root (and a root has no siblings), me march into the void
					}
				}
			} else { // normal mode	
				if (match) {
					current = current.firstChild; // match, go down
					stack.pop();
				} else {
					
					// NEW: No match, check if there can be a match at all...
					Term peeked = stack.peek();
					Atom label = (peeked.isStringAtom()) ? peeked.asStringAtom() : peeked.asIntegerAtom();
					UniqueTrie searched = current.parent.getChild(label);
					if (searched != null && searched.siblingIndex > current.siblingIndex) { // XXX better not be a previous child! <-- can this happen?
						// jump directly (but we check match again!)
						current = searched; 
					} else {
						// NEW go up first!
						current = current.parent;
						stack.back();
						
						// no match, go right...
						while (current.nextSibling == null && current != start) { // don't go higher up than start
							current = current.parent; // go up
							stack.back();
						}
						
						if (current != start) {
							current = current.nextSibling; // go (directly) right
						} else { // current == start
							current = null; // we treat start as root (and a root has no siblings), me march into the void
						}
					}
				}
			}
		}
	}
}
