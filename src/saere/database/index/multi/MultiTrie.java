package saere.database.index.multi;

import saere.Term;
import saere.database.index.FunctorLabel;
import saere.database.index.Label;
import saere.database.index.Root;
import saere.database.index.StorageHashTrie;
import saere.database.index.InnerNode;

// Extends StorageHashTrie because we are lazy... (cases like instr(id,...) and f(a(b))/f(a,b) may both occur)
public final class MultiTrie extends StorageHashTrie {
	
	private Root subtrie;
	
	public MultiTrie(InnerNode parent, Label label, Term term) {
		super(parent, label, null, term);
		assert label instanceof FunctorLabel : "Functor label expected";
		
		// XXX Temporary solution, more easy to implement
		subtrie = new Root();
	}
	
	public boolean isMultiTrie() {
		return true;
	}
	
	public Root getSubtrie() {
		return subtrie;
	}
	
	public void setSubtrie(Root subtrie) {
		this.subtrie = subtrie;
	}
}
