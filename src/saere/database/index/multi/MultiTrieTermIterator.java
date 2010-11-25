package saere.database.index.multi;

import saere.database.index.TermIterator;
import saere.database.index.InnerNode;

public final class MultiTrieTermIterator extends TermIterator {
	
	public MultiTrieTermIterator(InnerNode start) {
		super(start);
	}
	
	@Override
	protected void findNext() {
		next = null;
		
		if (list != null) {
			// List processing mode
			next = list.term();
			list = list.next();
		} else {
			
			// Normal mode, as long as we have nodes left and have no next term (list)
			while (current != null && next == null) { 
				
				// Only the first functor nodes of the first dimension...
				if (current.getFirstChild() == null) {
					list = current.getTerms();
					next = list.term();
					//list = list.next();
					list = null;
					
					goRight();
				} else {
					nextNode(); // Move to next node anyway
				}
				
			}
		}
	}
}
