package saere.database.index.multi;

import saere.Term;
import saere.database.index.FunctorLabel;
import saere.database.index.Label;
import saere.database.index.StorageHashTrie;
import saere.database.index.Trie;

// Extends StorageHashTrie because we are lazy... (cases like instr(id,...) and f(a(b))/f(a,b) may both occur)
public final class MultiTrie extends StorageHashTrie {
	
	private Trie subtrie;
	
	public MultiTrie(Trie parent, Label label, Term term) {
		super(parent, label, null, term);
		assert label instanceof FunctorLabel : "Functor label expected";
		
		// XXX Temporary solution, more easy to implement
		subtrie = Trie.newRoot();
	}
	
	public boolean isMultiTrie() {
		return true;
	}
	
	public Trie getSubtrie() {
		return subtrie;
	}
	
	public void setSubtrie(Trie subtrie) {
		this.subtrie = subtrie;
	}
}
