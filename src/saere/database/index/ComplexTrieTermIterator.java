package saere.database.index;

import java.util.Iterator;

import saere.Term;

public class ComplexTrieTermIterator extends TrieTermIterator implements Iterator<Term> {
	
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
	public ComplexTrieTermIterator(Trie start, Term ... terms) {
		super();
		this.start = start;
		current = start;
		
		assert terms.length > 0 : "No query specified";
		
		// create the one and only instance of the subiterator that'll be used
		subiterator = new TrieTermIterator(start);
		
		// break down terms to atoms/variables
		stack = new TermStack(Trie.getTermFlattener().flattenQuery(terms));
		
		// don't skip the first node
		if (!current.isRoot() && stack.size() == 1 && match(current.getLabel(), stack) <= stack.size()) {
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
			
			// Compute match now, as we'll need it anyway.
			// How "much" does the current label matches with the current stack (state)?
			int match = match(current.getLabel(), stack);
			
			// Check if we need a subiterator (if it isn't already active).
			// We'll need one if an end point is reached, i.e., if the only 
			// remaining element in the stack matches this label.
			// This is very often the case if the current node is a leaf (so we 
			// use a 'whole' iterator for a single leaf only).
			if (!useSubiterator && stack.size() == 1 && match > 0) {
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
				
				int labelLength = current.labelLength();
				
				if (match > 0) {
					
					// FIXME Problem with inner node subiterators (how to go on after iteration?)
					
					if (match == stack.size()) { // 1. end point reached? --> subiterator
						
						useSubiterator = true;
						subiterator.resetTo(current);
						
					} else if (match == labelLength) { // match < stack.size()
						
						//stack.pop(labelLength);
						nextNode(); // XXX or directly go down?
						
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
		stack.back(current.getParent().labelLength()); // new: labelLength() // FIXME! CANNOT WORK THAT WAY! The 'new' current's label has nothing to do with the label we used to go down!
		super.goUp();
	}
	
	@Override
	protected void goDown() {
		stack.pop(current.labelLength()); // new: labelLength() 
		super.goDown();
	}
	
	// doesn't work with the root (since its label is null)
	protected int match(Label label, TermStack stack) {
		assert label != null : "Cannot match with null label";
		
		if (stack.size() == 0)
			return 0;
		
		// handle variables
		Term peeked = stack.peek();
		if (peeked.isVariable() && !peeked.asVariable().isInstantiated()) { // free variable
			
			if (stack.size() > 1) {
				Term[] array = stack.asArray();
				int i = 1;
				peeked = array[i];
				while (peeked.isVariable() && !peeked.asVariable().isInstantiated() && i < array.length) {
					peeked = array[i++];
				}
				return i > label.length() ? label.length() : i;
			} else {
				return 1;
			}
			
		} else { // normal case, no variable(s)
			return label.match(stack.asArray());
		}
	}
}
