package saere.database.index.multi;

import saere.database.index.TermIterator;
import saere.database.index.Trie;

public final class MultiTrieTermIterator extends TermIterator {
	
	// XXX Must actually be a MultiTrieTermIterator too! With this we go only one dimension 'deeper'...
	private MultiTrieTermIterator subiterator; // Bad recursion!

	public MultiTrieTermIterator(Trie start) {
		super();
		this.start = start;
		current = start;
		findNext();
	}
	
	@Override
	protected void findNext() {
		next = null;
		
		if (subiterator != null && subiterator.hasNext()) {
			next = subiterator.next();
		} else if (list != null) {
			// List processing mode
			next = list.term();
			list = list.next();
		} else {
			// Normal mode, as long as we have nodes left and have no next term (list)
			while (current != null && next == null) { 
				
				// XXX The check if the term list is not null is necessary for complex tries where every node is storage trie from the beginning.
				if (current.stores() && current.getTerms() != null) {
					list = current.getTerms();
					next = list.term();
					list = list.next();
				} else if (current.multi() && current.getFirstChild() == null) {
					subiterator = new MultiTrieTermIterator(current.getSubtrie());
					if (subiterator.hasNext()) {
						next = subiterator.next();
					}
				}
				nextNode(); // Move to next node anyway
			}
		}
	}
	
	private class Subiterator extends TermIterator {
		
		public Subiterator() { /* empty */ }
	}
}
