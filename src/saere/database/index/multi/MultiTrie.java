package saere.database.index.multi;

import saere.database.index.FunctorLabel;
import saere.database.index.HashTrie;
import saere.database.index.Label;
import saere.database.index.Trie;

// Extends StorageHashTrie because we are lazy... (cases like instr(id,...) and f(a(b))/f(a,b) may both occur)
public final class MultiTrie extends HashTrie {
	
	private Trie subtrie;
	
	public MultiTrie(Trie parent, Label label) {
		super(parent, label, null);
		assert label instanceof FunctorLabel : "Functor label expected";
		
		// XXX Temporary solution, more easy to implement
		subtrie = Trie.root();
	}
	
	public boolean multi() {
		return true;
	}
	
	public Trie getSubtrie() {
		return subtrie;
	}
	
	public void setSubtrie(Trie subtrie) {
		this.subtrie = subtrie;
	}
}
