package saere.database.index;

import java.util.Iterator;

import saere.Atom;
import saere.Term;

/**
 * A {@link TermIterator} that supports queries on complex {@link Trie}s 
 * (which have been created by a {@link ComplexTrieBuilder}).
 * 
 * @author David Sullivan
 * @version 0.3, 10/19/2010
 */
public class ComplexTrieTermIterator extends TermIterator<Atom[]> implements Iterator<Term> {
	
	private QueryStack stack;
	private final TermIterator<Atom[]> subiterator;
	private boolean useSubiterator = false;
	
	/**
	 * Creates a new trie iterator that starts from <tt>start</tt> and 
	 * returns only terms that match the term represented by <tt>terms</tt>.
	 * 
	 * @param start The start trie, e.g., a functor.
	 * @param terms A query represented by an array of terms (atoms/variables).
	 */
	public ComplexTrieTermIterator(Trie<Atom[]> start, QueryStack stack) {	
		this.start = current = start;
		this.stack = stack;
		
		// create the one and only instance of the subiterator that'll be used
		subiterator = new TermIterator<Atom[]>(start);
		
		// don't skip the first node
		if (current.getParent() != null && stack.size() == 1 && Matcher.match(current.getLabel(), stack.asArray()) == 1) {
			if (subiterator.hasNext()) {
				useSubiterator = true;
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
		next = null;
		
		// as long as we haven't found a new next and are not at an end point, i.e., current is null
		while (next == null && current != null) { // (or break)
			
			// Even if the begin the search with root's first child, we may still arrive here with goRight().
			// Also, if we begin the search with root's first child, this child is seen as 'root', i.e., is looked at if it had no siblings (this is how subiterators work).
			if (current.getParent() == null) { // root
				current = current.getFirstChild();
				continue;
			}
			
			// Compute match now, as we'll need it anyway.
			// How "much" does the current label matches with the current stack (state)?
			int match = Matcher.match(current.getLabel(), stack.asArray());
			
			// Check if we need a subiterator (if it isn't already active).
			// We'll need one if an end point is reached, i.e., if the only 
			// remaining element in the stack matches this label.
			// This is very often the case if the current node is a leaf (so we 
			// use a 'whole' iterator for a single leaf only).
			if (!useSubiterator && stack.size() == match) { // FIXME Actually stack.size() == match?!
				useSubiterator = true; // was && stack.size() == 1 && match > 0
				subiterator.resetTo(current);
			}
			
			if (useSubiterator) { // subiteration mode
				if (subiterator.hasNext()) {
					next = subiterator.next();
					break; // = return
				} else {
					useSubiterator = false;
					goRight(); // we iterated the subtrie, go right
				}
			} else { // normal mode	
				
				if (match > 0) { // any match
					
					if (match == stack.size()) { // end point reached, create subiterator
						useSubiterator = true;
						subiterator.resetTo(current);
					} else if (match == current.getLabel().length) { // match < stack.size()
						nextNode(); // XXX or goDown() directly?
					} else { // match < labelLength
						goRight(); // no full match, go right
					}
					
				} else {
					goRight(); // no match (at all), go right
				}
			}
		}
	}
	
	@Override
	protected void goUp() {
		if (current.getParent().getLabel() != null)
			stack.back(current.getParent().getLabel().length);
		super.goUp();
	}
	
	@Override
	protected void goDown() {
		stack.pop(current.getLabel().length);
		super.goDown();
	}
}
