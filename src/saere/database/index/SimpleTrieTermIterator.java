package saere.database.index;

import java.util.Iterator;

import saere.Term;

/**
 * Trie term iterator that supports queries. A query is expressed by an array of
 * {@link Term}s. For term flattening the same {@link TermFlattener} as in the 
 * {@link Trie} class is used.<br/>
 * <br/>
 * <b>This iterator works only with {@link Trie}s that have been built with 
 * a {@link SimpleTermInserter}.</b>
 * 
 * @author David Sullivan
 * @version 0.2, 9/30/2010
 */
public class SimpleTrieTermIterator extends TrieTermIterator implements Iterator<Term> {

	private TermStack stack;
	private final TrieTermIterator subiterator;
	private boolean useSubiterator = false;
	
	/**
	 * Creates a new trie iterator that starts from <tt>start</tt> and 
	 * returns only terms that match the term represented by <tt>terms</tt>.
	 * 
	 * @param start The start trie, e.g., a functor.
	 * @param terms A query represented by an array of terms (atoms/variables).
	 */
	public SimpleTrieTermIterator(Trie start, Term ... terms) {
		this.start = start;
		current = start;
		
		assert terms.length > 0 : "No query specified";
		
		// create the one and only instance of the subiterator that'll be used
		subiterator = new TrieTermIterator(start);
		
		// break down terms to atoms/variables
		stack = new TermStack(Trie.getTermFlattener().flattenQuery(terms));
		
		// don't skip the first node
		if (stack.size() == 1 && match(stack)) {
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
			if (current.isRoot()) {
				current = current.getFirstChild();
				continue;
			}
			
			// Check if we need a subiterator (if it isn't already active).
			// We'll need one if an end point is reached, i.e., if the only 
			// remaining element in the stack matches this label.
			// This is very often the case if the current node is a leaf (so we 
			// use a 'whole' iterator for a single leaf only).
			if (!useSubiterator && stack.size() == 1 && match(stack)) {
				useSubiterator = true;
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
				if (match(stack)) {
					goDown(); // match, go down
				} else {
					goRight(); // no match, go right
				}
			}
		}
	}
	
	@Override
	protected void goUp() {
		stack.back();
		super.goUp();
	}
	
	@Override
	protected void goDown() {
		stack.pop();
		super.goDown();
	}
	
	/**
	 * We assume only {@link AtomLabel}s as the {@link SimpleTermInserter} 
	 * creates {@link Trie}s with {@link AtomLabel}s only. So the first element 
	 * of a {@link TermStack} (completely) matches or it doesn't.
	 * 
	 * @param stack The {@link TermStack} containing the query.
	 * @return <tt>true</tt> if the first element of the {@link TermStack} matches.
	 */
	protected boolean match(TermStack stack) {
		assert current.getLabel() != null : "Cannot match a root";
		
		Term peeked = stack.peek();
		if (peeked.isVariable() && !peeked.asVariable().isInstantiated()) {
			return true; // an unbound variable always matches, XXX Why check here? Shouldn't Label.same() do this?
		} else {
			return current.getLabel().match(stack.peek());
		}
	}
}
